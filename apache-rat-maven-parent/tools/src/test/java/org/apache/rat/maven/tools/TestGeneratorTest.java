package org.apache.rat.maven.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;
import org.apache.rat.maven.MavenOption;
import org.apache.rat.maven.MavenOptionCollection;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TestGeneratorTest {

    @TempDir
    private Path testPath;
    private TestGenerator testGenerator;
    private MavenOptionCollection optionCollection = new  MavenOptionCollection();
    TestGeneratorTest() {}

    @BeforeEach
    void setup() throws IOException {
        testGenerator = new TestGenerator("package.name", testPath.resolve("resources").toString(), testPath.resolve("source").toString());
    }

    private List<TestData> testsFor(Option option) {
        Set<TestData> testDataList = new ReportTestDataProvider().getOptionTests(optionCollection);
        return testDataList.stream().filter(td -> td.getOption() == null ? option == null : td.getOption().equals(option)).toList();
    }

    @ParameterizedTest
    @MethodSource("optionsSource")
    void test(MavenOption mavenOption) throws IOException {
        List<TestData> testDataList = testsFor(mavenOption.getOption());
        for (final TestData testData : testDataList) {
            String pomText = testGenerator.buildPom(optionCollection, testData);
            TextUtils.assertContains("<" + mavenOption.getName(), pomText);
        }
    }

    static List<MavenOption> optionsSource() {
        return new MavenOptionCollection().getMappedOptions().toList();
    }
}
