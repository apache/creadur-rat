package org.apache.rat.mp;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.report.claim.ClaimStatistic;

/**
 * Run Rat to perform a violation check.
 *
 */
@Mojo (name = "check", defaultPhase = LifecyclePhase.VALIDATE)
public class RatCheckMojo extends AbstractRatMojo
{
    /**
     * Where to store the report.
     *
     */
    @Parameter (property = "rat.outputFile", defaultValue = "${project.build.directory}/rat.txt")
    private File reportFile;

    /**
     * Output style of the report. Use "plain" (the default) for a plain text
     * report or "xml" for the raw XML report. Alternatively you can give the
     * path of an XSL transformation that will be applied on the raw XML to
     * produce the report written to the output file.
     *
     */
    @Parameter(property = "rat.outputStyle", defaultValue = "plain")
    private String reportStyle;

    /**
     * Maximum number of files with unapproved licenses.
     */
    @Parameter(property = "rat.numUnapprovedLicenses", defaultValue = "0")
    private int numUnapprovedLicenses;

    /**
     * Whether to add license headers; possible values are
     * {@code forced}, {@code true}, and {@code false} (default).
     *
     */
    @Parameter(property = "rat.addLicenseHeaders", defaultValue = "false")
    private String addLicenseHeaders;

    /**
     * Copyright message to add to license headers. This option is
     * ignored, unless {@code addLicenseHeaders} is set to {@code true},
     * or {@code forced}.
     *
     */
    @Parameter(property = "rat.copyrightMessage")
    private String copyrightMessage;

    /**
     * Will ignore rat errors and display a log message if any.
     * Its use is NOT RECOMMENDED, but quite convenient on occasion.
     * @since 0.9
     */
    @Parameter(property = "rat.ignoreErrors", defaultValue = "false")
    private boolean ignoreErrors;

    private ClaimStatistic getRawReport()
        throws MojoExecutionException, MojoFailureException
    {
        FileWriter fw = null;
        try
        {
            fw = new FileWriter( reportFile );
            final ClaimStatistic statistic = createReport( fw, getStyleSheet() );
            fw.close();
            fw = null;
            return statistic;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.close();
                }
                catch ( Throwable t )
                {
                    /* Ignore me */
                }
            }
        }
    }

    /**
     * Returns the XSL stylesheet to be used for formatting the report.
     *
     * @see #reportStyle
     * @return report stylesheet, or <code>null</code> for raw XML
     * @throws MojoExecutionException if the stylesheet can not be found
     */
    private InputStream getStyleSheet() throws MojoExecutionException {
        if ( reportStyle == null || reportStyle.equals( "plain" ) )
        {
            return Defaults.getPlainStyleSheet();
        }
        else if ( reportStyle.equals( "xml" ) )
        {
            return null;
        }
        else
        {
            try
            {
                return new FileInputStream( reportStyle );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoExecutionException(
                        "Unable to find report stylesheet: " + reportStyle, e );
            }
        }
    }

    /**
     * Invoked by Maven to execute the Mojo.
     *
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             Another error occurred while executing the plugin.
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File parent = reportFile.getParentFile();
        if(!parent.mkdirs() && !parent.isDirectory()) {
            throw new MojoExecutionException("Could not create report parent directory " + parent);
        }

        final ClaimStatistic report = getRawReport();
        check( report );
    }

    protected void check( ClaimStatistic statistics )
        throws MojoFailureException
    {
        getLog().info("Rat check: Summary of files. Unapproved: " + statistics.getNumUnApproved() + " unknown: " + statistics.getNumUnknown() + " generated: " + statistics.getNumGenerated() + " approved: " + statistics.getNumApproved() + " licence.");
        if ( numUnapprovedLicenses < statistics.getNumUnApproved() )
        {
            final String seeReport = " See RAT report in: " + reportFile;
            if ( !ignoreErrors )
            {
                throw new RatCheckException( "Too many files with unapproved license: " + statistics.getNumUnApproved() + seeReport);
            }
            else
            {
                getLog().warn( "Rat check: " + statistics.getNumUnApproved() + " files with unapproved licenses." + seeReport);
            }
        }
    }

    @Override
    protected ReportConfiguration getConfiguration()
            throws MojoFailureException, MojoExecutionException {
        final ReportConfiguration configuration = super.getConfiguration();
        if ("forced".equals(addLicenseHeaders)) {
            configuration.setAddingLicenses(true);
            configuration.setAddingLicensesForced(true);
            configuration.setCopyrightMessage(copyrightMessage);
        } else if ("true".equals(addLicenseHeaders)) {
            configuration.setAddingLicenses(true);
            configuration.setCopyrightMessage(copyrightMessage);
        } else if ("false".equals(addLicenseHeaders)) {
            // Nothing to do
        } else {
            throw new MojoFailureException("Invalid value for addLicenseHeaders: Expected forced|true|false, got "
                    + addLicenseHeaders);
        }
        return configuration;
    }
}
