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
package org.apache.rat;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.OrMatcher;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.IReportable;

/**
 * A configuration object is used by the frontend to invoke the {@link Report}.
 * Basically, the sole purpose of the frontends is to create the configuration
 * and invoke the {@link Report}.
 */
public class ReportConfiguration implements AutoCloseable {
    private List<IHeaderMatcher> headerMatcher = new ArrayList<>();
    private List<ILicenseFamily> approvedLicenseNames = new ArrayList<>();
    private boolean addingLicenses;
    private boolean addingLicensesForced;
    private String copyrightMessage;
    private boolean approveDefaultLicenses = true;
    private OutputStream out = null;
    private boolean styleReport = true;
    private InputStream styleSheet = null;
    private IReportable reportable = null;
    private FilenameFilter inputFileFilter = null;

    public FilenameFilter getInputFileFilter() {
        return inputFileFilter;
    }

    public void setInputFileFilter(FilenameFilter inputFileFilter) {
        this.inputFileFilter = inputFileFilter;
    }

    public IReportable getReportable() {
        return reportable;
    }

    public void setReportable(IReportable reportable) {
        this.reportable = reportable;
    }

    public InputStream getStyleSheet() {
        return styleSheet;
    }

    public void setStyleSheet(InputStream styleSheet) {
        this.styleSheet = styleSheet;
    }

    public boolean isStyleReport() {
        return styleReport;
    }

    public void setStyleReport(boolean styleReport) {
        this.styleReport = styleReport;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    /**
     * Returns the output stream. If no stream has been set returns System.out.
     * 
     * @return The output stream to write to.
     */
    public OutputStream getOutput() {
        return out == null ? System.out : out;
    }

    /**
     * Returns a PrintWriter that wraps the output stream.
     * 
     * @return A PrintWriter that wraps the output stream.
     */
    public PrintWriter getWriter() {
        return new PrintWriter(new OutputStreamWriter(getOutput(), Charset.forName("UTF-8")));
    }

    /**
     * @return whether default licenses shall be approved by default.
     */
    public boolean isApproveDefaultLicenses() {
        return approveDefaultLicenses;
    }

    public void setApproveDefaultLicenses(boolean approveDefaultLicenses) {
        this.approveDefaultLicenses = approveDefaultLicenses;
    }

    /**
     * Returns the header matcher.
     *
     * @return the header matcher.
     */
    public IHeaderMatcher getHeaderMatcher() {
        if (headerMatcher.isEmpty()) {
            return null;
        }
        if (headerMatcher.size() == 1) {
            return headerMatcher.get(0);
        }
        return new OrMatcher("Report matchers", headerMatcher);
    }

    /**
     * Sets the header matcher.
     *
     * @param headerMatcher header matcher.
     */
    public void addHeaderMatcher(IHeaderMatcher headerMatcher) {
        this.headerMatcher.add(headerMatcher);
    }

    /**
     * Returns the set of approved license names.
     *
     * @return the set of approved license names.
     */
    public ILicenseFamily[] getApprovedLicenseNames() {
        return approvedLicenseNames.toArray(new ILicenseFamily[approvedLicenseNames.size()]);
    }

    /**
     * Adds to the set of approved license names.
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseNames(ILicenseFamily[] approvedLicenseNames) {
        addApprovedLicenseNames(Arrays.asList(approvedLicenseNames));
    }

    /**
     * Adds a license to the list of approved license names.
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseName(ILicenseFamily approvedLicenseName) {
        approvedLicenseNames.add(approvedLicenseName);
    }

    /**
     * Adds to the set of approved license names (convenience).
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseNames(List<ILicenseFamily> approvedLicenseNames) {
        this.approvedLicenseNames.addAll(approvedLicenseNames);
    }

    /**
     * @return If Rat is adding license headers: Returns the optional copyright
     * message. This value is ignored, if no license headers are added.
     * @see #isAddingLicenses()
     */
    public String getCopyrightMessage() {
        return copyrightMessage;
    }

    /**
     * If Rat is adding license headers: Sets the optional copyright message. This
     * value is ignored, if no license headers are added.
     *
     * @param copyrightMessage message to set.
     * @see #setAddingLicenses(boolean)
     */
    public void setCopyrightMessage(String copyrightMessage) {
        this.copyrightMessage = copyrightMessage;
    }

    /**
     * @return If Rat is adding license headers: Returns, whether adding license
     * headers is enforced. This value is ignored, if no license headers are added.
     * @see #isAddingLicenses()
     */
    public boolean isAddingLicensesForced() {
        return addingLicensesForced;
    }

    /**
     * If Rat is adding license headers: Sets, whether adding license headers is
     * enforced. This value is ignored, if no license headers are added.
     *
     * @param addingLicensesForced enable/disable forcibly adding licenses.
     * @see #isAddingLicenses()
     */
    public void setAddingLicensesForced(boolean addingLicensesForced) {
        this.addingLicensesForced = addingLicensesForced;
    }

    /**
     * @return Returns, whether Rat should add missing license headers.
     * @see #isAddingLicensesForced()
     * @see #getCopyrightMessage()
     */
    public boolean isAddingLicenses() {
        return addingLicenses;
    }

    /**
     * Returns, whether Rat should add missing license headers.
     *
     * @param addingLicenses enables/disables adding of licenses.
     * @see #setAddingLicensesForced(boolean)
     * @see #setCopyrightMessage(String)
     */
    public void setAddingLicenses(boolean addingLicenses) {
        this.addingLicenses = addingLicenses;
    }

    public void validate(Consumer<String> logger) throws ConfigurationException {
        if (reportable == null) {
            throw new ConfigurationException("Reportable may not be null");
        }
        if (headerMatcher.size() == 0) {
            throw new ConfigurationException("You must specify at least one license" + " matcher");
        }
        if (styleSheet != null && !isStyleReport()) {
            logger.accept("Ignoring stylesheet '%s' because styling is not selected");
        }
    }

    @Override
    public void close() {
        if (styleSheet != null) {
            try {
                styleSheet.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                styleSheet = null;
            }
        }
        if (out != null) {
            if (out != System.out && out != System.err) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            out = null;
        }
    }
}
