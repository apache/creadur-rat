package org.apache.rat.report.claim;

import org.apache.rat.report.claim.ISubject;


/**
 * Mock implementation of {@link ISubject}.
 */
public class MockSubject implements ISubject {
    private final String name;

    /**
     * Creates a new instance.
     */
    public MockSubject(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
