package org.apache.rat.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.UpdatableOptionGroup;

/**
 *
 * @param <T> the AbstractOption implementation.
 */
public abstract class AbstractOptionCollection<T extends AbstractOption<T>> {

    /** The collection of unsupported options */
    protected final Collection<Option> unsupportedOptions;
    /** The additional options for the specific UI.  Used for documentation generation */
    protected final Options additionalOptions;
    /**
     * Create an Instance.
     * @param unsupportedOptions the collection of options that are not supported.
     */
    protected AbstractOptionCollection(final Collection<Option> unsupportedOptions, final Options additionalOptions) {
        this.unsupportedOptions = unsupportedOptions;
        this.additionalOptions = additionalOptions;
    }

    /**
     * Gets the collection of unsupported Options.
     * @return the Options comprised for the unsupported options.
     */
    public final Options getUnsupportedOptions() {
        Options options = new Options();
        unsupportedOptions.forEach(options::addOption);
        return options;
    }

    /**
     * Get a mapping function from an Apache Commons cli Option to the AbstractOption implementation.
     * @return a mapping function from an Apache Commons cli Option to the AbstractOption implementation.
     */
    protected abstract Function<Option, T> getMapper();

    /**
     * Creates an AbstractOption instance ({@code T}) from an Apache Commons cli Option.
     * @param option the option to build the instance from.
     * @return an AbstractOption instance from an Apache Commons cli Option.
     */
    public final T getMappedOption(final Option option) {
        return getMapper().apply(option);
    }

    /**
     * Gets an Apache Commons cli Options that contains all the Apache Commons cli Options that are understood by this collection.
     * @return an Apache Commons cli Options that contains all the Apache Commons cli Options that are understood by this collection.
     */
    public final Options getOptions() {
        Options result = new Options();
        Options argOptions = Arg.getOptions();
        Set<UpdatableOptionGroup> optionGroups = new HashSet<>();
        for (Option option : argOptions.getOptions()) {
            optionGroups.add((UpdatableOptionGroup) argOptions.getOptionGroup(option));
        }
        for (Option option : unsupportedOptions) {
            UpdatableOptionGroup group = (UpdatableOptionGroup) argOptions.getOptionGroup(option);
            if (group != null) {
                group.disableOption(option);
            }
        }
        optionGroups.forEach(result::addOptionGroup);
        return result.addOptions(additionalOptions());
    }

    /**
     * Gets the Stream of AbstractOption implementations  understood by this collection.
     * @return the Stream of AbstractOption implementations understood by this collection.
     */
    public final Stream<T> getMappedOptions() {
        return getOptions().getOptions().stream().map(getMapper());
    }

    /**
     * Gets a map client option name to specified AbstractOption implementation.
     * @return a map client option name to specified AbstractOption implementation
     */
    public Map<String, T> getOptionMap() {
        Map<String, T> result = new TreeMap<>();
        getMappedOptions().forEach(mappedOption -> result.put(ArgumentTracker.extractKey(mappedOption.getOption()), mappedOption));
        return result;
    }

    /**
     * Gets the additional options understood by this collection.
     * @return the additional options understood by this collection.
     */
    public final Options additionalOptions() {
        return additionalOptions;
    }

    /**
     * Gets the map of default overrides.
     * @return the default overrides.
     */
    protected abstract Map<Option, String> defaultOverrides();

    /**
     * Specifies the default value for the option.
     * @param option the option to override.
     * @param value the value to use as the default.
     */
    public abstract void addOverride(Option option, String value);

    /**
     * Adds overrides the default value for all options in the Arg.
     * @param arg the arg to override.
     * @param value the value to set as the default value.
     */
    public void addOverride(final Arg arg, final String value) {
        arg.group().getOptions().forEach(option -> this.addOverride(option, value));
    }

    /**
     * Gets the default value for the option.
     * @param option the option to lookup.
     * @return the default value or {@code null} if not set.
     */
    public final String getDefaultValue(final Option option) {
        String override = defaultOverrides().get(option);
        if (override == null) {
            Arg arg = Arg.findArg(option);
            if (arg != null) {
                override = arg.defaultValue();
            }
        }
        return override;
    }
}
