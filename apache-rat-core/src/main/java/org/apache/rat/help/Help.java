package org.apache.rat.help;

import static java.lang.String.format;

import org.apache.commons.cli.Options;
import org.apache.rat.OptionCollection;

import java.io.PrintWriter;
import java.util.Map;
import java.util.function.Supplier;

public final class Help extends AbstractHelp {

    /**
     * An array of notes to go at the bottom of the help output
     */
    private static final String[] NOTES = {
            "Rat highlights possible issues.",
            "Rat reports require interpretation.",
            "Rat often requires some tuning before it runs well against a project.",
            "Rat relies on heuristics: it may miss issues"
    };


    public Help() {
        super();
    }

    /**
     * Print the usage to the specific PrintWriter.
     * @param writer the PrintWriter to output to.
     * @param opts The defined options.
     */
    public void printUsage(final PrintWriter writer, final Options opts) {
        String syntax = format("java -jar apache-rat/target/apache-rat-%s.jar [options] [DIR|ARCHIVE]", versionInfo.getVersion());
        helpFormatter.printHelp(writer, helpFormatter.getWidth(), syntax, header("Available options"), opts,
                helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(),
                header("Argument Types"), false);

        String argumentPadding = createPadding(helpFormatter.getLeftPadding() + HELP_PADDING);
        for (Map.Entry<String, Supplier<String>> argInfo : OptionCollection.getArgumentTypes().entrySet()) {
            writer.format("%n<%s>%n", argInfo.getKey());
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                    argumentPadding + argInfo.getValue().get());
        }


        writer.println(header("Notes"));
        int idx = 1;
        for (String note : NOTES) {
            writer.format("%d. %s%n", idx++, note);
        }

        writer.flush();
    }
}
