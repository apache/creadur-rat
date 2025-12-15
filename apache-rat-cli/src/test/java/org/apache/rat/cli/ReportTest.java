package org.apache.rat.cli;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.data.OptionTestDataProvider;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class ReportTest {
    @TempDir
    static Path testPath;

    private static final ReportTestDataProvider reportTestDataProvider = new ReportTestDataProvider();

    /**
     * This method is a known workaround for
     * {@link <a href="https://github.com/junit-team/junit5/issues/2811">junit 5 issue #2811</a> }.
     */
    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void cleanUp() {
        System.gc();
    }


    static Stream<Arguments> getTestData() {
        return reportTestDataProvider.getOptionTestMap().values().stream().map(testData ->
                Arguments.of(testData.getTestName(), testData));
        // TODO add help test
    }

    /**
     * A parameterized test for the options.
     * @param name The name of the test.
     */
    @ParameterizedTest( name = "{index} {0}")
    @MethodSource("getTestData")
    void testOptionsUpdateConfig(String name, TestData test) throws Exception {
        Path basePath = testPath.resolve(test.getTestName());
        FileUtils.mkDir(basePath.toFile());
        test.setupFiles(basePath);
        if (test.getExpectedException() != null) {
            assertThatThrownBy(() -> Report.generateReport(basePath.toFile(), test.getCommandLine(basePath.toString()))
                    ).hasMessageContaining(test.getExpectedException().getMessage());
        } else {
            Report.CLIOutput result = Report.generateReport(basePath.toFile(), test.getCommandLine(basePath.toString()));
            ValidatorData data = new ValidatorData(
                    result.output, result.configuration, basePath.toString());
            test.getValidator().accept(data);
        }
    }
}
