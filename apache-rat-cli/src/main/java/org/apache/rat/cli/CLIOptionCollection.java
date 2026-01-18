package org.apache.rat.cli;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.ui.AbstractOptionCollection;

public class CLIOptionCollection extends AbstractOptionCollection<CLIOption> {
    /** The Help option */
    static final Option HELP = new Option("?", "help", false, "Print help for the RAT command line interface and exit.");
    /** The additional options */
    static final Options ADDITIONAL_OPTIONS = new Options();

    static {
        ADDITIONAL_OPTIONS.addOption(HELP);
    }

    CLIOptionCollection() {
        super(Collections.emptyList(), ADDITIONAL_OPTIONS);
    }

    @Override
    public Function<Option, CLIOption> getMapper() {
        return option -> new CLIOption(this, option);
    }

    @Override
    protected Map<Option, String> defaultOverrides() {
        return Collections.emptyMap();
    }


    @Override
    public void addOverride(final Option option, final String value) {
        throw new UnsupportedOperationException();
    }
}
