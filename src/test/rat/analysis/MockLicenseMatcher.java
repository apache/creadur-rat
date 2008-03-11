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
package rat.analysis;

import java.util.ArrayList;
import java.util.List;

import rat.report.claim.IClaimReporter;

public class MockLicenseMatcher implements IHeaderMatcher {

	public final List lines = new ArrayList();
	public int resets = 0;
    public boolean result = true;
	
	public boolean match(String subject, String line, IClaimReporter reporter) {
		lines.add(line);
		return result;  
	}

	public void reset() {
		resets++;
	}

}
