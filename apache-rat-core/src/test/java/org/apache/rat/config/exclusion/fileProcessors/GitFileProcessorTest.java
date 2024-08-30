package org.apache.rat.config.exclusion.fileProcessors;

import org.apache.rat.utils.iterator.WrappedIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitFileProcessorTest extends AbstractIgnoreProcessorTest {

    @Test
    public void processExampleFileTest() throws IOException {
        String[] lines = {
                "# somethings",
                "!thingone", "thing*", System.lineSeparator(),
                "# some fish",
                "**/fish", "*_fish",
                "# some colorful directories",
                "red/", "blue/*/"};

        List<String> expected = WrappedIterator.create(Arrays.asList("thing*", "**/fish", "*_fish", "red/**", "blue/*/**").iterator())
                .map(s -> new File(baseDir, s).getPath()).toList();
        expected.add(0, "!"+new File(baseDir, "thingone").getPath());
        // "thingone",
        writeFile(".gitignore", Arrays.asList(lines));

        GitFileProcessor processor = new GitFileProcessor();
        List<String> actual = processor.apply(baseDir.toString());
        assertEquals(expected, actual);
    }
}
