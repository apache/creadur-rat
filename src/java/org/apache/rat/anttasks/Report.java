/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */ 
package org.apache.rat.anttasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;

import org.apache.rat.Defaults;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.RatReportFailedException;

/**
 * A basic Ant task that generates a report on all files specified by
 * the nested resource collection(s).
 *
 * <p>ILicenseMatcher(s) can be specified as nested elements as well.</p>
 */
public class Report extends Task {

    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;
    /**
     * The licenses we want to match on.
     */
    private ArrayList licenseMatchers = new ArrayList();
    
    private ArrayList licenseNames = new ArrayList();
    
    /**
     * Whether to add the default list of license matchers.
     */
    private boolean addDefaultLicenseMatchers = true;
    /**
     * Where to send the report.
     */
    private File reportFile;

    /**
     * Adds resources that will be checked.
     */
    public void add(ResourceCollection rc) {
        if (nestedResources == null) {
            nestedResources = new Union();
        }
        nestedResources.add(rc);
    }

    /**
     * Adds a license matcher.
     */
    public void add(IHeaderMatcher matcher) {
        licenseMatchers.add(matcher);
    }
    
    public void add(ILicenseFamily license) {
        licenseNames.add(license);
    }

    /**
     * Whether to add the default list of license matchers.
     */
    public void setAddDefaultLicenseMatchers(boolean b) {
        addDefaultLicenseMatchers = b;
    }

    /**
     * Where to send the report to.
     */
    public void setReportFile(File f) {
        reportFile = f;
    }

    /**
     * Generates the report.
     */
    public void execute() {
        validate();

        PrintWriter out = null;
        try {
            if (reportFile == null) {
                out = new PrintWriter(
                          new OutputStreamWriter(
                              new LogOutputStream(this, Project.MSG_INFO)
                              )
                          );
            } else {
                out = new PrintWriter(new FileWriter(reportFile));
            }
            createReport(out);
            out.flush();
        } catch (IOException ioex) {
            throw new BuildException(ioex);
        } catch (TransformerException e) {
            throw new BuildException(e);
        } catch (InterruptedException e) {
            throw new BuildException(e);
        } catch (RatReportFailedException e) {
            throw new BuildException(e);
        } finally {
            if (reportFile != null) {
                FileUtils.close(out);
            }
        }
    }

    /**
     * validates the task's configuration.
     */
    private void validate() {
        if (nestedResources == null) {
            throw new BuildException("You must specify at least one file to"
                                     + " create the report for.");
        }
        if (!addDefaultLicenseMatchers && licenseMatchers.size() == 0) {
            throw new BuildException("You must specify at least one license"
                                     + " matcher");
        }
    }

    /**
     * Writes the report to the given stream.
     * @throws InterruptedException 
     * @throws TransformerException 
     * @throws RatReportFailedException 
     */
    private void createReport(PrintWriter out) throws IOException, TransformerException, InterruptedException, RatReportFailedException {
        HeaderMatcherMultiplexer m = new HeaderMatcherMultiplexer(getLicenseMatchers());
        ResourceCollectionContainer rcElement =
            new ResourceCollectionContainer(nestedResources);
        rat.Report.report(out, rcElement, Defaults.getDefaultStyleSheet(), m, getApprovedLicenseNames());
    }

    /**
     * Flattens all nested matchers plus the default matchers (if
     * required) into a single array.
     */
    private IHeaderMatcher[] getLicenseMatchers() {
        IHeaderMatcher[] matchers = null;
        if (addDefaultLicenseMatchers) {
            int nestedSize = licenseMatchers.size();
            if (nestedSize == 0) {
                matchers = Defaults.DEFAULT_MATCHERS;
            } else {
                matchers = new IHeaderMatcher[Defaults.DEFAULT_MATCHERS.length
                                               + nestedSize];
                licenseMatchers.toArray(matchers);
                System.arraycopy(Defaults.DEFAULT_MATCHERS, 0, matchers,
                                 nestedSize, Defaults.DEFAULT_MATCHERS.length);
            }
        } else {
            matchers = (IHeaderMatcher[])
                licenseMatchers.toArray(new IHeaderMatcher[0]);
        }
        return matchers;
    }
    
    private ILicenseFamily[] getApprovedLicenseNames() {
        // TODO: add support for adding default licenses
        ILicenseFamily[] results = null;
        if (licenseNames.size() > 0) {
            results = (ILicenseFamily[]) licenseNames.toArray(new ILicenseFamily[0]);
        }
        return results;
    }
}
