package org.apache.rat.commandline;

import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FSInfoTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.io.File;
import java.util.List;

import static org.apache.rat.document.FSInfoTest.WINDOWS;
import static org.assertj.core.api.Assertions.assertThat;

public class ConvertersTest {

    private static final DocumentName.FSInfo[] TEST_SUITE = FSInfoTest.TEST_SUITE;

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void convertFileNameTest(DocumentName.FSInfo fsInfo) {
        String root = fsInfo.equals(WINDOWS) ? "D:\\" : fsInfo.roots()[0];
        DocumentName workingDirectory = DocumentName.builder(fsInfo).setName("foo").setRoot(root).setBaseName("").build();
        Converters.FILE_CONVERTER.setWorkingDirectory(workingDirectory);

        for (String fn : List.of("rat.txt", "./rat.txt", "/rat.txt")) {
            assertThat(Converters.FILE_CONVERTER.apply(fn).getName()).as(fn).isEqualTo(workingDirectory.getRoot() + fsInfo.mkPath("foo", "rat.txt"));
        }

        for (String fn : List.of("target/rat.txt", "./target/rat.txt", "/target/rat.txt")) {
            assertThat(Converters.FILE_CONVERTER.apply(fn).getName()).as(fn).isEqualTo(workingDirectory.getRoot() + fsInfo.mkPath("foo", "target", "rat.txt"));
        }
    }

}
