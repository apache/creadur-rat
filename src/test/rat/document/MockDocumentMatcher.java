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
package rat.document;

import java.util.ArrayList;
import java.util.List;

public class MockDocumentMatcher implements IDocumentMatcher {

    public boolean returnValue = false;
    public List matches = new ArrayList();
    
    public MockDocumentMatcher() {}
    public MockDocumentMatcher(boolean returnValue) {
        this.returnValue = returnValue;
    }
    
    public boolean matches(IDocument document) {
        matches.add(document);
        return returnValue;
    }

}
