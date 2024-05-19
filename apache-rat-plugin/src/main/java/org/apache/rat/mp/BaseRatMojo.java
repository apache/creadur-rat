/* do not edit, generated file */
package org.apache.rat.mp;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.Option;

public abstract class BaseRatMojo extends AbstractMojo { 
    private final List<String> args = new ArrayList<>();

    protected BaseRatMojo() {}

    protected List<String> args() { return this.args; }

    /* 
     * Specifies the level of detail in ARCHIVE file reporting. (default is NOTIFICATION)
     */
    @Parameter(property = "rat.archive")
    public void setArchive(String archive) {
        args.add("--archive");
        args.add(archive);
    }

    /* 
     * Specifies the level of detail in STANDARD file reporting. (default is ABSENCE)
     */
    @Parameter(property = "rat.standard")
    public void setStandard(String standard) {
        args.add("--standard");
        args.add(standard);
    }

    /* 
     * If set do not update the files but generate the reports.
     */
    @Parameter(property = "rat.dryRun")
    public void setDryRun(boolean dryRun) {
        if (dryRun) {
            args.add("--dry-run");
        } else {
            args.remove("--dry-run");
        }
    }

    /* 
     * List the defined license families (default is NONE). Valid options are: ALL, APPROVED, NONE
     */
    @Parameter(property = "rat.listFamilies")
    public void setListFamilies(String listFamilies) {
        args.add("--list-families");
        args.add(listFamilies);
    }

    /* 
     * List the defined licenses (default is NONE). Valid options are: ALL, APPROVED, NONE
     */
    @Parameter(property = "rat.listLicenses")
    public void setListLicenses(String listLicenses) {
        args.add("--list-licenses");
        args.add(listLicenses);
    }

    /* 
     * Print help for the RAT command line interface and exit.
     */
    @Parameter(property = "rat.help")
    public void setHelp(boolean help) {
        if (help) {
            args.add("--help");
        } else {
            args.remove("--help");
        }
    }

    /* 
     * Define the output file where to write a report to (default is System.out).
     */
    @Parameter(property = "rat.out")
    public void setOut(String out) {
        args.add("--out");
        args.add(out);
    }

    /* 
     * Ignore default configuration. By default all approved default licenses are used
     */
    @Parameter(property = "rat.noDefaultLicenses")
    public void setNoDefaultLicenses(boolean noDefaultLicenses) {
        if (noDefaultLicenses) {
            args.add("--no-default-licenses");
        } else {
            args.remove("--no-default-licenses");
        }
    }

    /* 
     * File names or URLs for license definitions.  May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.
     */
    @Parameter(property = "rat.licenses")
    public void addLicenses(String licenses) {
        args.add("--licenses");
        args.add(licenses);
    }

    /* 
     * Scan hidden directories
     */
    @Parameter(property = "rat.scanHiddenDirectories")
    public void setScanHiddenDirectories(boolean scanHiddenDirectories) {
        if (scanHiddenDirectories) {
            args.add("--scan-hidden-directories");
        } else {
            args.remove("--scan-hidden-directories");
        }
    }

    /* 
     * Add the default license header to any file with an unknown license that is not in the exclusion list. By default new files will be created with the license header, to force the modification of existing files use the --force option.
     */
    @Parameter(property = "rat.addLicense")
    public void setAddLicense(boolean addLicense) {
        if (addLicense) {
            args.add("--addLicense");
        } else {
            args.remove("--addLicense");
        }
    }

    /* 
     * Forces any changes in files to be written directly to the source files (i.e. new files are not created).  Only valid with --addLicense
     */
    @Parameter(property = "rat.force")
    public void setForce(boolean force) {
        if (force) {
            args.add("--force");
        } else {
            args.remove("--force");
        }
    }

    /* 
     * The copyright message to use in the license headers, usually in the form of "Copyright 2008 Foo".  Only valid with --addLicense
     */
    @Parameter(property = "rat.copyright")
    public void setCopyright(String copyright) {
        args.add("--copyright");
        args.add(copyright);
    }

    /* 
     * Excludes files matching wildcard <Expression>. May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.
     */
    @Parameter(property = "rat.exclude")
    public void addExclude(String exclude) {
        args.add("--exclude");
        args.add(exclude);
    }

    /* 
     * Excludes files matching regular expression in the input file.
     */
    @Parameter(property = "rat.excludeFile")
    public void setExcludeFile(String excludeFile) {
        args.add("--exclude-file");
        args.add(excludeFile);
    }

    /* 
     * [Deprecated for removal since 0.17: Use '--'] Used to indicate end of list when using --exclude.
     * Deprecated for removal since 0.17: Use '--'
     */
    @Deprecated
    @Parameter(property = "rat.dir")
    public void setDir(String dir) {
        args.add("--dir");
        args.add(dir);
    }

    /* 
     * sets the log level.
     */
    @Parameter(property = "rat.logLevel")
    public void setLogLevel(String logLevel) {
        args.add("--log-level");
        args.add(logLevel);
    }

    /* 
     * Output the report in raw XML format.  Not compatible with -s
     */
    @Parameter(property = "rat.xml")
    public void setXml(boolean xml) {
        if (xml) {
            args.add("--xml");
        } else {
            args.remove("--xml");
        }
    }

    /* 
     * XSLT stylesheet to use when creating the report.  Not compatible with -x. Either an external xsl file may be specified or one of the internal named sheets: plain-rat (default), missing-headers, or unapproved-licenses
     */
    @Parameter(property = "rat.stylesheet")
    public void setStylesheet(String stylesheet) {
        args.add("--stylesheet");
        args.add(stylesheet);
    }

}
