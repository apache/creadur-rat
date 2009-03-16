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
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.rat.Defaults;
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
     * Maximum number of files with unapproved licenses.
     * @parameter expression="${rat.numUnapprovedLicenses}" default-value="0"
     */
    private int numUnapprovedLicenses;

    private ClaimStatistic getRawReport()
        throws MojoExecutionException, MojoFailureException
    {
        FileWriter fw = null;
        try
        {
            fw = new FileWriter( reportFile );
            final ClaimStatistic statistic = createReport( fw, Defaults.getDefaultStyleSheet() );
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
}
