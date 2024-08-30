package org.apache.rat.config.exclusion.fileProcessors;

import org.apache.rat.config.exclusion.ExclusionUtils;

import java.io.File;
import java.util.List;

public class GitFileProcessor extends DescendingFileProcessor {

    public GitFileProcessor() {
        super(".gitignore", "#");
    }

    private String mapFileName(File dir, String fileName) {
        String pattern = fileName;
        String prefix = "";
        if (pattern.startsWith("!")) {
            prefix = "!";
            pattern = pattern.substring(1);
        }
        if (pattern.endsWith("/")) {
            pattern = pattern + "**";
        }
        return prefix + new File(dir, pattern).getPath();
    }

    protected List<String> process(File f) {
        final File dir = f.getParentFile();
        return ExclusionUtils.asIterator(f, commentFilter)
                .map(s -> mapFileName(dir, s)).toList();
    }
}
