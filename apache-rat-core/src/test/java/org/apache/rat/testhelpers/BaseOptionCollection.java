package org.apache.rat.testhelpers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.ui.AbstractOptionCollection;

/**
 * An implementation of AbstractOptionCollection for testing.
 */
public final class BaseOptionCollection extends AbstractOptionCollection<BaseOption> {
    private final Map<Option, String> defaultOverrides = new HashMap<>();

    /**
     * Constructs a BaseOptionCollection without unsupportedOptions.
     */
    public BaseOptionCollection() {
        this(Collections.emptyList());
    }

    /**
     * Constructs a BaseOptionCollection with unsupportedOptions.
     */
    public BaseOptionCollection(Collection<Option> unsupportedOptions) {
        super(unsupportedOptions, new Options());
    }

    @Override
    protected Function<Option, BaseOption> getMapper() {
        return option -> new BaseOption(this, option);
    }


    @Override
    public Map<Option, String> defaultOverrides() {
        return defaultOverrides;
    }

    @Override
    public void addOverride(Option option, String value) {
        defaultOverrides.put(option, value);
    }
}
