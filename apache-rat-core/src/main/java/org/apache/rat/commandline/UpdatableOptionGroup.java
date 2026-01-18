package org.apache.rat.commandline;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

/**
 * An implementation of Apache Commons CLI OptionGroup that allows options to be removed (disabled).
 */
public class UpdatableOptionGroup extends OptionGroup {
    /** The set of options to remove */
    private final Set<Option> removedOptions = new HashSet<>();

    /**
     * Disable an option in the group.
     * @param option The option to disable.
     */
    public final void disableOption(final Option option) {
        removedOptions.add(option);
    }

    /**
     * Reset the group so that all disabled options are re-enabled.
     */
    public void reset() {
        removedOptions.clear();
    }

    @Override
    public Collection<Option> getOptions() {
        return super.getOptions().stream().filter(opt -> !removedOptions.contains(opt)).toList();
    }

    @Override
    public UpdatableOptionGroup addOption(final Option option) {
        super.addOption(option);
        return this;
    }
}
