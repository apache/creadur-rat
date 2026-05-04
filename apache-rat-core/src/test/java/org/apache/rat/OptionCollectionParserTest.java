package org.apache.rat;

import com.google.common.io.Files;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.rat.api.Document;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptionCollectionParserTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    static Path testPath;

    private TestOptionCollection optionCollection = new TestOptionCollection();
    private OptionCollectionParser underTest = new OptionCollectionParser(optionCollection);

    @Test
    void parseCommands() throws IOException, ParseException {
        String[] args = {"arg1", "arg2"};
        ArgumentContext ctxt = underTest.parseCommands(testPath.toFile(), args);
        assertThat(ctxt.getCommandLine().getArgList()).containsExactly(args);

        String[] cmds = new String[] {"--input-exclude-size", "5", "arg1", "arg2"};
        ctxt = underTest.parseCommands(testPath.toFile(), cmds);
        StringBuilder sb = new StringBuilder();
        ctxt.getConfiguration().reportExclusions(sb);
        assertThat(sb.toString()).contains("Excluding File size < 5 bytes.");
        assertThat(ctxt.getCommandLine().getArgList()).containsExactly(args);
    }

    @Test
    void getReportable() throws IOException {
        File dir1 = testPath.resolve("dir1").toFile();
        assertThat(underTest.getReportable(dir1, new ReportConfiguration())).isNull();

        assertThat(dir1.mkdir()).isTrue();
        ReportConfiguration reportConfiguration = new ReportConfiguration();

        File dir2 = testPath.resolve("dir2").toFile();
        reportConfiguration.addExcludedPatterns(List.of(dir2.getName()));
        assertThat(underTest.getReportable(dir2, reportConfiguration)).isNull();

        IReportable reportable = underTest.getReportable(dir1, new ReportConfiguration());
        assertThat(reportable).isInstanceOf(DirectoryWalker.class);

        File file1 = new File(dir1, "file1");
        try (FileOutputStream fos = new FileOutputStream(file1)) {
            fos.write("Hello world".getBytes(StandardCharsets.UTF_8));
        }
        reportable = underTest.getReportable(file1, new ReportConfiguration());
        assertThat(reportable).isInstanceOf(ArchiveWalker.class);
    }

    static class TestOption extends UIOption<TestOption> {

        /**
         * Constructor.
         *
         * @param optionCollection
         * @param option           The CLI option
         */
        protected <C extends UIOptionCollection<TestOption>> TestOption(C optionCollection, Option option) {
            super(optionCollection, option, new CasedString(CasedString.StringCase.CAMEL, option.getKey()));
        }

        @Override
        protected String cleanupName(Option option) {
            return "clean" + option.toString();
        }

        @Override
        public String getExample() {
            return "example " + option.toString();
        }

        @Override
        public String getText() {
            return "text for " + option.toString();
        }
    }

    class TestOptionCollection extends UIOptionCollection<TestOption> {
        /**
         * Construct the UIOptionCollection from the builder.
         */
        protected TestOptionCollection() {
            super(new TestCollectionBuilder());
        }

        static class TestCollectionBuilder extends UIOptionCollection.Builder<TestOption, TestCollectionBuilder> {
            TestCollectionBuilder() {
                super(TestOption::new);
            }
        }
    }
}
