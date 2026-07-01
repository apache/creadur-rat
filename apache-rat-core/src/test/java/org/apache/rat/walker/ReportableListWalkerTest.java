package org.apache.rat.walker;

import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.Reportable;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportableListWalkerTest {

    private static DocumentName testName(final TestInfo testInfo) {
        String testName = testInfo.getTestMethod().isPresent() ? testInfo.getTestMethod().get().getName() : "unknownMethod";
        String baseName = testInfo.getTestClass().isPresent() ?
                testInfo.getTestClass().get().getName().replace(".", DocumentName.FSInfo.getDefault().dirSeparator())
                : "unknownClass";
        return DocumentName.builder()
                .setName(testName)
                .setBaseName(baseName)
                .build();
    }

    ReportableListWalker defaultUnderTest(final TestInfo testInfo) throws RatException {
        return ReportableListWalker.builder(testName(testInfo))
                .addReportable(new Reportable() {
                    @Override
                    public void run(RatReport report) throws RatException {
                        throw new RatException("Testing exception");
                    }

                    @Override
                    public DocumentName name() {
                        return DocumentName.builder().setName("exceptionThrowingReportable")
                                .setBaseName(testName(testInfo)).build();
                    }
                }).build();
    }
    @Test
    void ratExceptionDuringRun(TestInfo testInfo) throws RatException {
        TestingLog log = new TestingLog();

        try {
            DefaultLog.setInstance(log);
            ReportableListWalker underTest = defaultUnderTest(testInfo);
            underTest.run(null);
            assertThat(log.getCaptured()).contains("RatException: Testing exception");
            log.clear();
        } finally {
            DefaultLog.setInstance(null);
        }
    }

    @Test
    void runtimeExceptionDuringRun(TestInfo testInfo) throws RatException {
        ReportableListWalker underTest = ReportableListWalker.builder(testName(testInfo))
                .addReportable(new Reportable() {
                    @Override
                    public void run(RatReport report)  {
                        throw new RuntimeException("Runtime exception");
                    }

                    @Override
                    public DocumentName name() {
                        return DocumentName.builder().setName("exceptionThrowingReportable")
                                .setBaseName(testName(testInfo)).build();
                    }
                }).build();
        assertThatThrownBy(() -> underTest.run(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Runtime exception");
    }

    @Test
    void documentNameTest(TestInfo testInfo) throws RatException {
        ReportableListWalker underTest = defaultUnderTest(testInfo);
        DocumentName actual = underTest.name();
        assertThat(actual).isEqualTo(testName(testInfo));
    }

    @Test
    void resueBuilderFailsTest(TestInfo testInfo) throws RatException {
        ReportableListWalker.Builder builder = ReportableListWalker.builder(testName(testInfo))
                .addReportable(new Reportable() {
                    @Override
                    public void run(RatReport report) throws RatException {
                        throw new RatException("Testing exception");
                    }

                    @Override
                    public DocumentName name() {
                        return DocumentName.builder().setName("exceptionThrowingReportable")
                                .setBaseName(testName(testInfo)).build();
                    }
                });

        builder.build();
        assertThatThrownBy(builder::build)
                .isInstanceOf(RatException.class)
                .hasMessageContaining("Builder may only be used once");

    }

    @Test
    void emptyBuilderDoesNotFail(TestInfo testInfo) {
        ReportableListWalker.Builder builder = ReportableListWalker.builder(testName(testInfo));
        assertThatNoException().isThrownBy(builder::build);
    }
}
