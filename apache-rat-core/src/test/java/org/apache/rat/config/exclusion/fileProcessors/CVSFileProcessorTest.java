package org.apache.rat.config.exclusion.fileProcessors;

import org.apache.rat.utils.iterator.WrappedIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CVSFileProcessorTest extends AbstractIgnoreProcessorTest {

    @Test
    public void processExampleFileTest() throws IOException {
        String[] lines = {
                "thingone thingtwo", System.lineSeparator(), "one_fish", "two_fish", "", "red_* blue_*"};

        List<String> expected = WrappedIterator.create(Arrays.asList("thingone", "thingtwo", "one_fish", "two_fish", "red_*", "blue_*").iterator())
                .map(s -> new File(baseDir, s).getPath()).toList();

        writeFile(".cvsignore", Arrays.asList(lines));

        CVSFileProcessor processor = new CVSFileProcessor();
        List<String> actual = processor.apply(baseDir.toString());
        assertEquals(expected, actual);
    }
}
