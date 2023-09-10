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
package org.apache.rat.analysis.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.rat.Defaults;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.MockLocation;
import org.apache.rat.license.SimpleLicenseFamily;
import org.apache.rat.license.ILicenseFamily;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test to see if short form license information will be recognized correctly.
 *
 */
abstract public class AbstractMatcherTest {
    private static int NAME=0;
    private static int NOTES=1;
    private static int TEXT=2;
	protected MetaData data;
	protected static Collection<IHeaderMatcher> instrumented;
	private final String category;
	private final String name;
	private final String[][]targets;
    
    @BeforeClass
    public static void init() {
        Defaults.builder().build();
        instrumented = Defaults.getLicenses().stream().map( Instrumentation::wrap ).collect(Collectors.toList());
    }
    
    @AfterClass
    public static void shutdown() {
        Defaults.clear();
        instrumented = null;
    }
    
    protected AbstractMatcherTest(String cat, String name, String[][]targets) {
        this.category = ILicenseFamily.makeCategory(cat);
        this.name = name;
        this.targets = targets;
    }
    
	@Before
	public void setup() {
	    data = new MetaData();
	}
	
	protected static Collection<IHeaderMatcher> extractCategory(String category) {
	    String cat = ILicenseFamily.makeCategory(category);
	    List<IHeaderMatcher> matchers = new ArrayList<>();
	    instrumented.forEach( x -> x.extractMatcher(matchers::add, 
                target -> target.getFamilyCategory().equals(cat)));
	    if (matchers.isEmpty()) {
	        fail("No machers for category: "+category);
	    }
	    return matchers;
	}
	
	@Test
	public void testMatchProcessing() throws RatHeaderAnalysisException, IOException {
        Collection<IHeaderMatcher> matchers = extractCategory(category);
        for (IHeaderMatcher matcher : matchers) {
            for (String[] target : targets) {
                if (processText(matcher, target[TEXT])) {
                    assertEquals(matcher.toString(), category, data.get(MetaData.RAT_URL_HEADER_CATEGORY).getValue());
                    assertEquals(matcher.toString(), category, data.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY).getValue());
                    assertEquals(matcher.toString(), target[NOTES], data.get(MetaData.RAT_URL_HEADER_SAMPLE).getValue());
                    assertEquals(matcher.toString(), name, data.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME).getValue());
                    data.clear();
                }
                matcher.reset();
            }
        }
        List<String> notMatched = new ArrayList<>();
        List<String> notReset = new ArrayList<>();
        for (IHeaderMatcher matcher : matchers) {
            if (!(matcher instanceof Instrumentation))
            {
                fail( "Matcher was not instrumented: "+matcher);
            } else {
                Instrumentation instrumented = (Instrumentation) matcher;
                if (!instrumented.wasMatched())
                {
                    notMatched.add( instrumented.toString() );
                }
                if (!instrumented.wasReset()) {
                    notReset.add( instrumented.toString() );
                }
            }
        }
        if (!(notMatched.isEmpty() && notReset.isEmpty())) {
            StringBuilder failMsg = new StringBuilder();
            if (! notMatched.isEmpty()) {
                failMsg.append("The following matchers were not matched:\n");
                notMatched.forEach( t -> failMsg.append("\t").append(t).append("\n"));
                failMsg.append("\n");
            }
            if (! notReset.isEmpty()) {
                failMsg.append("The following matchers were not reset:\n");
                notReset.forEach( t -> failMsg.append("\t").append(t).append("\n"));
                failMsg.append("\n");
            }
            fail(failMsg.toString());
        }
	}
	
	private boolean processText(IHeaderMatcher matcher, String text) throws IOException, RatHeaderAnalysisException {
	    BufferedReader in = new BufferedReader(new StringReader(text));
        String line = in.readLine();
        boolean found = false;
        while (line != null) {
            found |= matcher.matches(line);
            line = in.readLine();
        }
        return found;
	}
	@Test
	public void testEmbeddedStrings() throws RatHeaderAnalysisException, IOException {
	    String formats[] = { "%s", "now is not the time %s for copyright",
	        "#%s", "##%s", "## %s", "##%s##", "## %s ##", "/*%s*/","/* %s */"};

        Collection<IHeaderMatcher> matchers = extractCategory(category);
        for (String[] target : targets) {
            for (String fmt : formats) {
                boolean found = false;
                for (IHeaderMatcher matcher : matchers) {
                    found |= processText(matcher, String.format(fmt, target[TEXT]));
                    matcher.reset();
                }
                assertTrue( String.format( "%s %s did not match pattern '%s' for target string %s", category, name, fmt, target[NAME]), found);
            }
        }
	}
	
	@Test
	public void testReportFamily() {
	    Collection<IHeaderMatcher> matchers = extractCategory(category);
	    ILicenseFamily lst[] = { null }; 
	    for (IHeaderMatcher matcher : matchers) {
	        matcher.reportFamily(x -> lst[0] = x);
	        assertEquals( "Error with "+matcher.getId(), category, lst[0].getFamilyCategory());
	        assertEquals( "Error with "+matcher.getId(), name, lst[0].getFamilyName());
	    }
	}
	
	//void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator);
	@Test
    public void testExtractMatcher() {
	    
        Collection<IHeaderMatcher> matchers = extractCategory(category);
        Collection<IHeaderMatcher> found = new ArrayList<>();
        for (IHeaderMatcher matcher : matchers) {
            matcher.extractMatcher( found::add,  target -> target.getFamilyCategory().equals(category));
        }
        assertEquals( matchers.size(), found.size());
        
        found.clear();
        for (IHeaderMatcher matcher : matchers) {
            matcher.extractMatcher( found::add,  target -> target.getFamilyCategory().equals("not a valid category"));
        }
        assertEquals( 0, found.size());
    }
	
	public static class Instrumentation implements IHeaderMatcher {
        private IHeaderMatcher wrapped;
        private int resetCount = 0;
        private int matchCount = 0;
        private int matchCalled = 0;
        
	    public int getMatchCalled() {
            return matchCalled;
        }

        public IHeaderMatcher getWrapped() {
	        return wrapped;
	    }

	    @Override
	    public String toString() {
	        return getId();
	    }
	    
	    @Override
	    public String getId() {
	        return String.format("[Instrumented] %s", wrapped.getId());
	    }

	    public boolean wasReset() {
	        return resetCount > 0;
	    }

	    public boolean wasMatched() {
	        return matchCount > 0;
	    }

	    public void setWrapped(IHeaderMatcher wrapped) {
	        this.wrapped = wrapped;
	    }

	    public int getResetCount() {
	        return resetCount;
	    }

	    public void setResetCount(int resetCount) {
	        this.resetCount = resetCount;
	    }

	    public int getMatchCount() {
	        return matchCount;
	    }

	    public void setMatchCount(int matchCount) {
	        this.matchCount = matchCount;
	    }

	    static Instrumentation wrap(IHeaderMatcher target) {
	        if (target instanceof Instrumentation) {
	            return (Instrumentation) target;
	        }
	        return new Instrumentation(target);
	    }

	    private Instrumentation(IHeaderMatcher target) {
	        this.wrapped = target;
	    }

	    @Override
	    public void reset() {
	        resetCount++;
	        wrapped.reset();
	    }

	    @Override
	    public boolean matches(String line) throws RatHeaderAnalysisException {
	        matchCalled++;
	        if (wrapped.matches(line)) {
	            matchCount++;
	            return true;
	        }
	        return false;
	    }

	    @Override
	    public void reportFamily(Consumer<ILicenseFamily> consumer) {
	        wrapped.reportFamily(consumer);
	    }

	    @Override
	    public void extractMatcher(Consumer<IHeaderMatcher> matchers, Predicate<ILicenseFamily> comparator) {
	        Collection<IHeaderMatcher> captured = new ArrayList<>();
	        wrapped.extractMatcher(captured::add, comparator);
	        captured.stream().map(Instrumentation::wrap).forEach(matchers::accept);
	    }

	}

}
