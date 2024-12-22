package org.apache.rat.config.exclusion.fileProcessors;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.document.DocumentName;

public class AbstractBuilder extends MatcherSet.Builder {

    /**
     * Constructor.
     * @param fileName The name of the file to process.
     * @param commentPrefix the comment prefix
     */
    protected AbstractBuilder(final String fileName, final String commentPrefix) {
        this(fileName, commentPrefix == null ? null : Collections.singletonList(commentPrefix));
    }

    /**
     * Constructor.
     * @param fileName name of the file to process
     * @param commentPrefixes a collection of comment prefixes.
     */
    protected AbstractBuilder(final String fileName, final Iterable<String> commentPrefixes) {
        super(fileName, commentPrefixes);
    }

    /**
     * Moves properly formatted file names includes, excludes into the proper
     * {@link #included} and {@link #excluded} DocumentMatchers.  This differs from the parent implementation
     * in that patterns that match the process are excluded.
     * @param documentName the nome of the document being processed.
     * @param iterable the list of properly formatted include and excludes from the input.
     */
    protected void segregateProcessResult(final DocumentName documentName, List<String> iterable) {
        Set<String> included = new HashSet<>();
        Set<String> excluded = new HashSet<>();
        segregateList(excluded, included, iterable);
        addExcluded(documentName, excluded);
        addIncluded(documentName, included);
    }
}
