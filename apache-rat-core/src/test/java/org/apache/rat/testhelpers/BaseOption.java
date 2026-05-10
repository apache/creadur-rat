
package org.apache.rat.testhelpers;

import org.apache.commons.cli.Option;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;

public final class BaseOption extends UIOption<BaseOption> {
    BaseOption(final UIOptionCollection<BaseOption> collection, Option option) {
        super(collection, option, new CasedString(CasedString.StringCase.KEBAB, ArgumentTracker.extractKey(option)));
    }
    protected String cleanupName(Option option) {
        return ArgumentTracker.extractKey(option);
    }

    public String getExample() {
        return "";
    }

    public String getText() {
        return "";
    }
}
