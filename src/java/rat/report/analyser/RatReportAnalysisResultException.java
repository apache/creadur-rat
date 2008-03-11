/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat.report.analyser;

import rat.document.RatDocumentAnalysisException;
import rat.report.RatReportFailedException;

public class RatReportAnalysisResultException extends RatDocumentAnalysisException {
    
    private static final long serialVersionUID = 4018716722707721989L;
    private static final String MESSAGE = "Could not report results of analysis";
    
    public RatReportAnalysisResultException() {
        super(MESSAGE);
    }

    public RatReportAnalysisResultException(RatReportFailedException cause) {
        super(MESSAGE, cause);
    }

    public RatReportAnalysisResultException(String msg, RatReportFailedException cause) {
        super(msg, cause);
    }
}
