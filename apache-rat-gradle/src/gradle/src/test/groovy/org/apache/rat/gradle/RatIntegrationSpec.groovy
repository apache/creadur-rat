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

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

/**
 * Rat IntegrationSpec.
 */
class RatIntegrationSpec extends IntegrationSpec {

    def 'success'() {
        setup:
        fork = true
        def inputDir = buildFile.parentFile.absolutePath.replaceAll('\\\\', '/')
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'org.apache.rat'
            rat {
                verbose = true
                inputDir = '$inputDir'
                excludes = [
                    'build.gradle', 'settings.gradle', 'build/**', '.gradle/**', '.gradle-test-kit/**',
                    'no-license-file.txt'
                ]
            }
        """.stripIndent()
        createFile( 'no-license-file.txt' ).text = 'Nothing here.'

        when:
        ExecutionResult result = runTasksSuccessfully( 'check' )

        then:
        wasExecuted( 'rat' )
        fileExists( 'build/reports/rat/rat-report.xml' )
        fileExists( 'build/reports/rat/index.html' )
    }

    def 'do not fail but report errors when rat.failOnError is false'() {
        setup:
        fork = true
        def inputDir = buildFile.parentFile.absolutePath.replaceAll('\\\\', '/')
        buildFile << """
            apply plugin: 'org.apache.rat'
            rat {
                verbose = true
                inputDir = '$inputDir'
                failOnError = false
                excludes = [
                    'build.gradle', 'settings.gradle', 'build/**', '.gradle/**', '.gradle-test-kit/**'
                ]
            }
        """.stripIndent()
        createFile( 'no-license-file.txt' ).text = 'Nothing here.'

        when:
        ExecutionResult result = runTasksSuccessfully( 'rat' )

        then:
        wasExecuted( 'rat' )
        fileExists( 'build/reports/rat/rat-report.xml' )
        fileExists( 'build/reports/rat/index.html' )
    }

    def 'fail the build when finding a file with unapproved/unknown license'() {
        setup:
        fork = true
        def inputDir = buildFile.parentFile.absolutePath.replaceAll('\\\\', '/')
        buildFile << """
            apply plugin: 'org.apache.rat'
            rat {
                verbose = true
                inputDir = '$inputDir'
                excludes = [
                    'build.gradle', 'settings.gradle', 'build/**', '.gradle/**', '.gradle-test-kit/**'
                ]
            }
        """.stripIndent()
        createFile( 'no-license-file.txt' ).text = 'Nothing here.'

        when:
        ExecutionResult result = runTasksWithFailure( 'rat' )

        then:
        wasExecuted( 'rat' )
        fileExists( 'build/reports/rat/rat-report.xml' )
        fileExists( 'build/reports/rat/index.html' )
    }

    def 'success on custom reportPath'() {
        setup:
        fork = true
        def inputDir = buildFile.parentFile.absolutePath.replaceAll('\\\\', '/')
        buildFile << """
            apply plugin: 'java'
            apply plugin: 'org.apache.rat'
            rat {
                verbose = true
                inputDir = '$inputDir'
                excludes = [
                    'build.gradle', 'settings.gradle', 'build/**', '.gradle/**', '.gradle-test-kit/**',
                    'no-license-file.txt'
                ]
                reportDir = file( 'build/reports/rat-custom' )
            }
        """.stripIndent()
        createFile( 'no-license-file.txt' ).text = 'Nothing here.'

        when:
        ExecutionResult result = runTasksSuccessfully( 'check' )

        then:
        wasExecuted( 'rat' )
        fileExists( 'build/reports/rat-custom/rat-report.xml' )
        fileExists( 'build/reports/rat-custom/index.html' )
    }

}
