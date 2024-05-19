package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A simple tool to convert CLI options  to Maven and Ant format
 */
public class Naming {

    private Naming() {}

    /**
     * Creates the documentation.  Writes to the output specified by the -o or --out option.  Defaults to System.out.
     * @param args the arguments.  Try --help for help.
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        Options options = Report.buildOptions();
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("nameMap.csv"), CSVFormat.DEFAULT)) {
            printer.printRecord("CLI", "Maven", "Ant", "Description");
            for (Option option : options.getOptions()) {
                if (option.getLongOpt() != null) {
                    CasedString opt = new CasedString(StringCase.Kebab, option.getLongOpt());
                    printer.printRecord(opt, mavenName(option, opt), antName(option, opt), option.getDescription());
                }
            }
        }
    }

    private static String quote(String s) {
        return String.format("\"%s\"", s);
    }
    private static String mavenName(Option option, CasedString name) {
        StringBuilder sb = new StringBuilder();
        if (option.isDeprecated()) {
            sb.append("@Deprecated").append(System.lineSeparator());
        }
        sb.append("@Parameter(property = ")
        .append(quote("rat."+WordUtils.uncapitalize(name.toCase(StringCase.Camel))));
             if (option.isRequired()) {
                sb.append(" required = true");
            }
        sb.append(")").append(System.lineSeparator())
                .append("public void ")
        .append(option.hasArgs() ? "add" : "set" )
                .append(WordUtils.capitalize(name.toCase(StringCase.Camel)))
                .append("(")
                .append(option.hasArg() ? "String " : "boolean ")
                .append(WordUtils.uncapitalize(name.toCase(StringCase.Camel)))
                .append(")");
        return sb.toString();
    }

    private static String antName(Option option, CasedString name) {
        StringBuilder sb = new StringBuilder();
        if (option.isDeprecated()) {
            sb.append("@Deprecated").append(System.lineSeparator());
        }
        sb.append(option.hasArgs() ? "add" : "set" )
        .append(WordUtils.capitalize(name.toCase(StringCase.Camel)))
        .append("(")
        .append(option.hasArg() ? "String " : "boolean ")
        .append(WordUtils.uncapitalize(name.toCase(StringCase.Camel)))
        .append(")");
        return sb.toString();
    }

    static Function<String[],String> camelJoiner = a -> {
        StringBuilder sb = new StringBuilder(a[0]);

        for (int i=1;i<a.length;i++) {
            sb.append(WordUtils.capitalize(a[i]));
        }
        return sb.toString();
    };

    enum StringCase {Camel(Character::isUpperCase, true,  camelJoiner),
        Snake(c -> c =='_', false, a -> String.join("_", a)),
        Kebab(c -> c == '-', false ,a -> String.join("-", a));

        private final Predicate<Character> splitter;
        private final boolean preserveSplit;
        private final Function<String[],String> joiner;

        StringCase(final Predicate<Character> splitter, final boolean preserveSplit, final Function<String[],String> joiner) {
            this.splitter = splitter;
            this.preserveSplit = preserveSplit;
            this.joiner = joiner;
        }
    }

    private static class CasedString {
        private String string;
        private StringCase stringCase;

        public CasedString(StringCase stringCase, String string) {
            this.string = string;
            this.stringCase = stringCase;
        }

        private String[] split() {
            List<String> lst = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (char c : string.toCharArray())
            {
                if (stringCase.splitter.test(c)) {
                    if (sb.length() > 0) {
                        lst.add(sb.toString());
                        sb.setLength(0);
                    }
                    if (stringCase.preserveSplit) {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
            if (sb.length() > 0) {
                lst.add(sb.toString());
            }
            return lst.toArray(new String[lst.size()]);
        }

        public String toCase(StringCase stringCase) {
            if (stringCase == this.stringCase) {
                return string;
            }
            return stringCase.joiner.apply(split());
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
