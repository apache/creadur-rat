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
package org.apache.rat.walker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.rat.utils.DefaultLog;

/**
 * A Reportable that walks a list of IReportables and executes the run on each.
 */
public final class IReportableListWalker implements IReportable {
    /** The document name for this walker */
    private final DocumentName documentName;
    /** The list of reportables for this walker */
    private final List<IReportable> reportables;

    /**
     * Create a builder for the list walker.
     * @param name the name of the walker.
     * @return the builder.
     */
    public static Builder builder(final DocumentName name) {
        return new Builder(name);
    }

    /**
     * Construct the builder.
     * @param builder for the reportable.
     */
    private IReportableListWalker(final Builder builder) {
        this.documentName = builder.documentName;
        this.reportables = builder.reportables;
    }

    @Override
    public void run(final RatReport report) throws RatException {
        for (IReportable reportable : reportables) {
            try {
                reportable.run(report);
            } catch (RatException e) {
                DefaultLog.getInstance().error("Error processing " + reportable.getName(), e);
            }
        }
    }

    @Override
    public DocumentName getName() {
        return documentName;
    }

    public static final class Builder {
        /** The document name for the walker */
        private final DocumentName documentName;
        /** The list of IReportable objecs to exectue */
        private List<IReportable> reportables = new ArrayList<>();

        /**
         * Constructs the builder.
         * @param name the name of the walker being built.
         */
        private Builder(final DocumentName name) {
            Objects.requireNonNull(name, "Document name must not be null");
            this.documentName = name;
        }

        /**
         * Add a reportable to the list of reportables to run.
         * @param reportable the reportable to run.
         * @return this.
         */
        public Builder addReportable(final IReportable reportable) {
            this.reportables.add(reportable);
            return this;
        }

        /**
         * Build the reportable.
         * @return the reportable.
         * @throws RatException on error.
         */
        public IReportable build() throws RatException {
            if (reportables == null) {
                throw new RatException("Builder may only be used once");
            }
            IReportable result = new IReportableListWalker(this);
            this.reportables = null;
            return result;
        }
    }
}
