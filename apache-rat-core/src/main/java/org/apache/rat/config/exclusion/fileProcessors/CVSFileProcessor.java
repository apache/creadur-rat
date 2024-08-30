package org.apache.rat.config.exclusion.fileProcessors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.ExclusionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


public class CVSFileProcessor extends DescendingFileProcessor {
    public CVSFileProcessor() {
        super(".cvsignore", (String)null);
    }

    protected List<String> process(File f) {
        final File dir = f.getParentFile();
        List<String> result = new ArrayList<String>();
        Iterator<String> iter = ExclusionUtils.asIterator(f, StringUtils::isNotBlank);
        while (iter.hasNext()) {
            String line = iter.next();
            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.add(new File(dir, part).getPath());
                }
            }
        }
        return result;
    }
}
