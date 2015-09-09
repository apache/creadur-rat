/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.internal.project.IsolatedAntBuilder

class RatTask extends DefaultTask {

    boolean failOnError = true
    boolean verbose = false

    boolean xmlOutput = true
    boolean htmlOutput = true
    boolean plainOutput = false

    @Input
    String inputDir = '.'

    @Input
    List<String> excludes = ['**/.gradle/**']

    @OutputDirectory
    File reportDir = project.file( project.buildDir.canonicalPath + '/reports/rat' )

    @InputFiles
    Set<File> getInputFiles() {
        project.fileTree(dir: inputDir, excludes: excludes).files
    }

    @TaskAction
    def rat() {
        if( !reportDir.exists() ) {
            reportDir.mkdirs()
        }
        def mainReport = null
        def errorCount = -1
        if( plainOutput ) {
            mainReport = generatePlainReport()
            errorCount = countUnaprovedUnknownLicensesFromPlain( mainReport )
        }
        def xmlReport = null
        if( xmlOutput || htmlOutput ) {
            xmlReport = generateXmlReport()
            mainReport = mainReport ?: xmlReport
            errorCount = countUnaprovedUnknownLicenses( xmlReport )
        }
        if( xmlReport != null && htmlOutput ) {
            mainReport = generateHtmlReport( xmlReport )
        }
        if( xmlReport && !xmlOutput ) {
            xmlReport.delete()
        }
        if( failOnError && errorCount > 0 ) {
            throw new GradleException(
                "Found $errorCount files with unapproved/unknown licenses. See ${mainReport.toURI()}"
            )
        }
    }

    def generatePlainReport() {
        def plainReport = new File( reportDir, 'rat-report.txt' )
        def antBuilder = services.get( IsolatedAntBuilder )
        def ratClasspath = project.configurations.rat
        antBuilder.withClasspath( ratClasspath ).execute {
            ant.taskdef( resource: 'org/apache/rat/anttasks/antlib.xml' )
            ant.report( format: 'plain', reportFile: plainReport.absolutePath ) {
                fileset( dir: inputDir ) {
                    patternset {
                        excludes.each { exclude( name: it ) }
                    }
                }
            }
        }
        project.logger.info "Rat TXT report: ${plainReport.toURI()}"
        return plainReport
    }

    def countUnaprovedUnknownLicensesFromPlain( plainReport ) {
        def errorCount = 0
        plainReport.readLines().each { line ->
            if( line.endsWith( 'Unknown Licenses' ) ) {
                errorCount = line.substring( 0, line.indexOf( ' ' ) ).toInteger()
            }
        }
        return errorCount
    }

    def generateXmlReport() {
        def xmlReport = new File( reportDir , 'rat-report.xml' )
        def antBuilder = services.get( IsolatedAntBuilder )
        def ratClasspath = project.configurations.rat
        antBuilder.withClasspath( ratClasspath ).execute {
            ant.taskdef( resource: 'org/apache/rat/anttasks/antlib.xml' )
            ant.report( format: 'xml', reportFile: xmlReport.absolutePath ) {
                fileset( dir: inputDir ) {
                    patternset {
                        excludes.each { exclude( name: it ) }
                    }
                }
            }
        }
        project.logger.info "Rat XML report: ${xmlReport.toURI()}"
        return xmlReport
    }

    def countUnaprovedUnknownLicenses( xmlReport ) {
        def ratXml = new XmlParser().parse( xmlReport )
        def errorCount = 0
        ratXml.resource.each { resource ->
            if( resource.'license-approval'.@name[0] == "false" ) {
                def log = 'Unapproved/unknown license: ' + resource.@name
                if( verbose ) println( log )
                else  project.logger.debug( log )
                errorCount++
            }
        }
        return errorCount
    }

    def generateHtmlReport( xmlReport ) {
        def htmlReport = new File( reportDir, 'index.html' )
        def stylesheet = project.file( project.buildDir.canonicalPath + '/tmp/rat/stylesheet.xsl' )
        stylesheet.parentFile.mkdirs()
        stylesheet.text = this.getClass().getResource( 'apache-rat-output-to-html.xsl' ).text
        def antBuilder = services.get( IsolatedAntBuilder )
        def ratClasspath = project.configurations.rat
        antBuilder.withClasspath( ratClasspath ).execute {
            ant.xslt(
                in: xmlReport.absolutePath,
                style: stylesheet.absolutePath,
                out: htmlReport.absolutePath,
                classpath: ratClasspath
            )
        }
        project.logger.info "Rat HTML report: ${htmlReport.toURI()}"
        return htmlReport
    }

}
