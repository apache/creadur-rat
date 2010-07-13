package org.apache.rat.report;

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;


/**
 * Abstract base class for deriving implementations of {@link RatReport}.
 */
public abstract class AbstractReport implements RatReport {
    public void startReport() throws RatException {
        // Does nothing
    }

    public void report(Document document) throws RatException {
        // Does nothing
    }

    public void endReport() throws RatException {
        // Does nothing
    }
}
