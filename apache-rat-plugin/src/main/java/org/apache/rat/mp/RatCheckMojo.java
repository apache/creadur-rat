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
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.report.claim.ClaimStatistic;

/**
 * Run RAT to perform a violation check.
 * 
 * @goal check
 * @phase verify
 */
public class RatCheckMojo extends AbstractRatMojo
{
    /**
     * Where to store the report.
     * 
     * @parameter expression="${rat.outputFile}" default-value="${project.build.directory}/rat.txt"
     */
    private File reportFile;

    /**
     * Output style of the report. Use "plain" (the default) for a plain text
     * report or "xml" for the raw XML report. Alternatively you can give the
     * path of an XSL transformation that will be applied on the raw XML to
     * produce the report written to the output file. 
     * 
     * @parameter expression="${rat.outputStyle}" default-value="plain"
     */
    private String reportStyle;

    /**
     * Maximum number of files with unapproved licenses.
     * @parameter expression="${rat.numUnapprovedLicenses}" default-value="0"
     */
    private int numUnapprovedLicenses;

    /**
     * Whether to add license headers; possible values are
     * {@code forced}, {@code true}, and {@code false} (default).
     *
     * @parameter expression="${rat.addLicenseHeaders}" default-value="false"
     */
    private String addLicenseHeaders;

    /**
     * Copyright message to add to license headers. This option is
     * ignored, unless {@code addLicenseHeaders} is set to {@code true},
     * or {@code forced}.
     *
     * @parameter expression="${rat.copyrightMessage}"
     */
    private String copyrightMessage;

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
        parent.mkdirs();

        final ClaimStatistic report = getRawReport();
        check( report );
    }

    protected void check( ClaimStatistic statistics )
        throws MojoFailureException
    {
        if ( numUnapprovedLicenses < statistics.getNumUnApproved() )
        {
            throw new RatCheckException( "Too many unapproved licenses: " + statistics.getNumUnApproved() );
        }
    }

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
