package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.text.WordUtils;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.FileWriter;
import org.apache.rat.CasedString.StringCase;

/**
 * A simple tool to convert CLI options  to Maven and Ant format
 */
public class Naming {

    private Naming() {}

    private static final String INDENT="    ";

    /**
     * Creates the documentation.  Writes to the output specified by the -o or --out option.  Defaults to System.out.
     * @param args the arguments.  Try --help for help.
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        Naming naming = new Naming();
        Options options = Report.buildOptions();
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("maven")) {
                naming.generateMavenClass(options);
                return;
            }
            if (args[0].equalsIgnoreCase("maven")) {
                naming.generateAntClass(options);
                return;
            }
        }

        try (CSVPrinter printer = new CSVPrinter(new FileWriter("nameMap.csv"), CSVFormat.DEFAULT)) {
            printer.printRecord("CLI", "Maven", "Ant", "Description");
            for (Option option : options.getOptions()) {
                if (option.getLongOpt() != null) {
                    CasedString opt = new CasedString(StringCase.Kebab, option.getLongOpt());
                    printer.printRecord(opt, naming.mavenName("", option, opt), naming.antName(option, opt), option.getDescription());
                }
            }
        }
    }

    private static String quote(String s) {
        return String.format("\"%s\"", s);
    }

    private void generateAntClass(Options options) {

    }

    private String asLongArg(Option option) {
        return quote("--"+option.getLongOpt());
    }


    private void generateMavenClass(Options options) throws IOException {
        try (FileWriter writer = new FileWriter("BaseRatMojo.java")) {
            writer.append("/* do not edit, generated file */").append(System.lineSeparator())
                    .append("package org.apache.rat.mp;").append(System.lineSeparator())
                    .append("import org.apache.maven.plugin.AbstractMojo;").append(System.lineSeparator())
                    .append("import org.apache.maven.plugins.annotations.Parameter;").append(System.lineSeparator())
                    .append("import java.util.ArrayList;").append(System.lineSeparator())
                    .append("import java.util.List;").append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("public abstract class BaseRatMojo extends AbstractMojo { ").append(System.lineSeparator())
                    .append(INDENT).append("private final List<String> args = new ArrayList<>();").append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(INDENT).append("protected BaseRatMojo() {}").append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(INDENT).append("protected List<String> args() { return this.args; }").append(System.lineSeparator());

            for (Option option : options.getOptions()) {
                if (option.getLongOpt() != null) {
                    CasedString name = new CasedString(StringCase.Kebab, option.getLongOpt());
                    writeMvnComment(writer, option);
                    writer.append(mavenName(INDENT,option, name)).append(" {").append(System.lineSeparator());
                    writeMvnBody(writer, option, name);
                    writer.append(INDENT).append("}").append(System.lineSeparator()).append(System.lineSeparator());
                }
            }
            writer.append("}");
        }
    }

    private void writeMvnComment(FileWriter writer, Option option) throws IOException {
        writer.append(INDENT).append("/* ").append(System.lineSeparator())
                .append(INDENT).append(" * ").append(option.getDescription()).append(System.lineSeparator());
        if (option.isDeprecated()) {
            writer.append(INDENT).append(" * ").append(option.getDeprecated().toString()).append(System.lineSeparator());
        }
        writer.append(INDENT).append(" */").append(System.lineSeparator());
    }

    private void writeMvnBody(FileWriter writer, Option option, CasedString name) throws IOException {
        String varName = WordUtils.uncapitalize(name.toCase(StringCase.Camel));
        String longArg = asLongArg(option);
        if (option.hasArg()) {
            writer.append(INDENT).append(INDENT).append("args.add(").append(longArg).append(");").append(System.lineSeparator())
            .append(INDENT).append(INDENT).append("args.add(").append(varName).append(");").append(System.lineSeparator());
        } else {
            writer.append(INDENT).append(INDENT).append("if (").append(varName).append(") {").append(System.lineSeparator())
            .append(INDENT).append(INDENT).append(INDENT).append("args.add(").append(longArg).append(");").append(System.lineSeparator())
                    .append(INDENT).append(INDENT).append("} else {").append(System.lineSeparator())
                    .append(INDENT).append(INDENT).append(INDENT).append("args.remove(").append(longArg).append(");").append(System.lineSeparator())
                    .append(INDENT).append(INDENT).append("}").append(System.lineSeparator());
        }
    }

    private String mavenName(String indent, Option option, CasedString name) {
        StringBuilder sb = new StringBuilder();
        if (option.isDeprecated()) {
            sb.append(indent).append("@Deprecated").append(System.lineSeparator());
        }
        sb.append(indent).append("@Parameter(property = ")
        .append(quote("rat."+WordUtils.uncapitalize(name.toCase(StringCase.Camel))));
             if (option.isRequired()) {
                sb.append(" required = true");
            }
        sb.append(")").append(System.lineSeparator())
                .append(indent)
                .append("public void ")
        .append(option.hasArgs() ? "add" : "set" )
                .append(WordUtils.capitalize(name.toCase(StringCase.Camel)))
                .append("(")
                .append(option.hasArg() ? "String " : "boolean ")
                .append(WordUtils.uncapitalize(name.toCase(StringCase.Camel)))
                .append(")");
        return sb.toString();
    }

    private String antName(Option option, CasedString name) {
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
}
