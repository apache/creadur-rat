package org.apache.rat.testhelpers;

import org.apache.rat.ui.AbstractOption;
import org.apache.rat.ui.ArgumentTracker;

public final class BaseOption  extends AbstractOption<BaseOption> {

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    BaseOption(final BaseOptionCollection collection, final org.apache.commons.cli.Option option) {
        super(collection, option, ArgumentTracker.extractKey(option));
    }

    @Override
    protected String cleanupName(final org.apache.commons.cli.Option option) {
        return ArgumentTracker.extractKey(option);
    }

    @Override
    public String getExample() {
        return "";
    }

    @Override
    public String getText() {
        return "";
    }
}
