package org.apache.rat.mp;

import java.io.File;
import java.io.IOException;

import org.apache.rat.api.RatException;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;

/**
 * Implementation of IReportable that traverses over a set of files.
 */
class FilesReportable implements IReportable {
    private final File basedir;

    private final String[] files;

    FilesReportable(File basedir, String[] files) throws IOException {
        final File currentDir = new File(System.getProperty("user.dir")).getCanonicalFile();
        final File f = basedir.getCanonicalFile();
        if (currentDir.equals(f)) {
            this.basedir = null;
        } else {
            this.basedir = basedir;
        }
        this.files = files;
    }

    @Override
    public void run(RatReport report) throws RatException {
        for (String file : files) {
            report.report(new FileDocument(new File(basedir, file)));
        }
    }
}
