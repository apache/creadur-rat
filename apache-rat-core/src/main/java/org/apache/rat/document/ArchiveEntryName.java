package org.apache.rat.document;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class ArchiveEntryName extends DocumentName {

    private final DocumentName archiveFileName;

    private static DocumentName.Builder prepareBuilder(DocumentName archiveFileName, String archiveEntryName) {
        String root = archiveFileName.getName() + "#";
        FSInfo fsInfo = new FSInfo("/", true, Collections.singletonList(root));
        return DocumentName.builder(fsInfo)
                .setRoot(root)
                .setBaseName(root + "/")
                .setName(archiveEntryName);
    }
    public ArchiveEntryName(final DocumentName archiveFileName, final String archiveEntryName) {
        super(prepareBuilder(archiveFileName, archiveEntryName));
        this.archiveFileName = archiveFileName;
    }

    @Override
    public File asFile() {
        return archiveFileName.asFile();
    }

    @Override
    public Path asPath() {
        return Paths.get( archiveFileName.asPath().toString(), "#", super.asPath().toString());
    }

    @Override
    public DocumentName resolve(String child) {
        return new ArchiveEntryName(this.archiveFileName, super.resolve(child).localized());
    }

//    @Override
//    public String getName() {
//        return archiveFileName.getName(), localized());
//    }

    @Override
    public String getBaseName() {
        return archiveFileName.getName()+"#";
    }

//    @Override
//    public DocumentName getBaseDocumentName() {
//        return super.getBaseDocumentName();
//    }
//
//    @Override
//    public String getDirectorySeparator() {
//        return super.getDirectorySeparator();
//    }

    @Override
    boolean startsWithRootOrSeparator(String candidate, String root, String separator) {
        return super.startsWithRootOrSeparator(candidate, root, separator);
    }

    @Override
    public String localized() {
        return /*archiveFileName.localized() + "#" +*/ super.localized();
    }

    @Override
    public String localized(String dirSeparator) {
        String superLocal = super.localized(dirSeparator);
        superLocal = superLocal.substring(superLocal.lastIndexOf("#")+1);
        return archiveFileName.localized(dirSeparator) + "#" + superLocal;
    }

    @Override
    public String getShortName() {
        return super.getShortName();
    }

    @Override
    public boolean isCaseSensitive() {
        return super.isCaseSensitive();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int compareTo(DocumentName other) {
        return super.compareTo(other);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
