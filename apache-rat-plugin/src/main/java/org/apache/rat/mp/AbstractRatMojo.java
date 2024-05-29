/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.mp;

import static org.apache.rat.mp.util.ExclusionHelper.addEclipseDefaults;
import static org.apache.rat.mp.util.ExclusionHelper.addIdeaDefaults;
import static org.apache.rat.mp.util.ExclusionHelper.addMavenDefaults;
import static org.apache.rat.mp.util.ExclusionHelper.addPlexusAndScmDefaults;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.license.DeprecatedConfig;
import org.apache.rat.config.SourceCodeManagementSystems;
import org.apache.rat.configuration.Format;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.MatcherReader;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.license.SimpleLicenseFamily;
import org.apache.rat.mp.util.ScmIgnoreParser;
import org.apache.rat.mp.util.ignore.GlobIgnoreMatcher;
import org.apache.rat.mp.util.ignore.IgnoreMatcher;
import org.apache.rat.mp.util.ignore.IgnoringDirectoryScanner;
import org.apache.rat.report.IReportable;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.plugin.BaseRatMojo;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Abstract base class for Mojos, which are running Rat.
 */
public abstract class AbstractRatMojo extends BaseRatMojo {

    /**
     * The base directory, in which to search for files.
     */
    @Parameter(property = "rat.basedir", defaultValue = "${basedir}", required = true)
    private File basedir;

    /**
     * Specifies the licenses to accept. By default, these are added to the default
     * licenses, unless you set {@link #addDefaultLicenseMatchers} to false.
     *
     * @since 0.8
     */
    @Parameter
    private String[] defaultLicenseFiles;

    @Parameter
    private String[] additionalLicenseFiles;

    /**
     * Whether to add the default list of licenses.
     * @deprecated use noDefaultLicenses (note the change of state)
     */
    @Deprecated
    @Parameter(property = "rat.addDefaultLicenses", name="addDefaultLicenses")
    public void setAddDefaultLicenses(boolean addDefaultLicenses) {
        setNoDefaultLicenses(!addDefaultLicenses);
    }

    /**
     * Whether to add the default list of license matchers.
     */
    @Parameter(property = "rat.addDefaultLicenseMatchers")
    private boolean addDefaultLicenseMatchers;


    @Parameter(required = false)
    private String[] approvedLicenses;

    @Parameter(property = "rat.approvedFile")
    private String approvedLicenseFile;
    
    /**
     * Specifies the license families to accept.
     *
     * @since 0.8
     * @deprecated use LicenseFamily section of configuration file.
     */
    @Deprecated // remove in v1.0
    @Parameter
    private SimpleLicenseFamily[] licenseFamilies;
    

    @Parameter
    private Object[] licenses;

    /** The list of families defined in the pom */
    @Parameter
    private Family[] families;

    /**
     * Specifies files, which are included in the report. By default, all files are
     * included.
     */
    @Parameter
    private String[] includes;

    /**
     * Specifies a file, from which to read includes. Basically, an alternative to
     * specifying the includes as a list.
     */
    @Parameter(property = "rat.includesFile")
    private String includesFile;

    /**
     * Specifies the include files character set. Defaults
     * to @code{${project.build.sourceEncoding}), or @code{UTF8}.
     */
    @Parameter(property = "rat.includesFileCharset", defaultValue = "${project.build.sourceEncoding}")
    private String includesFileCharset;

    private List<String> excludesList = new ArrayList<>();

    // for testing
    List<String> getExcludes() {
        return excludesList;
    }
    /**
     * Specifies files, which are excluded in the report. By default, no files are
     * excluded.
     * @deprecated use exclude
     */
    @Deprecated
    @Parameter
    public void setExcludes(String[] excludes) {
        this.excludesList.addAll(Arrays.asList(excludes));
    }

    @Override
    @Parameter(property = "rat.exclude")
    public void setExclude(String exclude) {
        excludesList.add(exclude);
    }

    private List<String> excludesFileList = new ArrayList<>();
    // for testing
    List<String> getExcludesFile() {
        return excludesFileList;
    }

    /**
     * Specifies a file, from which to read excludes. Basically, an alternative to
     * specifying the excludes as a list. The excludesFile is assumed to be using
     * the UFT8 character set.
     * @deprecated use excludeFile
     */
    @Deprecated
    @Parameter(property = "rat.excludesFile")
    public void setExcludesFile(String excludeFile) {
        excludesFileList.add(excludeFile);
    }

    @Override
    public void setExcludeFile(String file) {
        excludesFileList.add(file);
    }

    /**
     * Specifies the include files character set. Defaults
     * to @code{${project.build.sourceEncoding}), or @code{UTF8}.
     */
    @Parameter(property = "rat.excludesFileCharset", defaultValue = "${project.build.sourceEncoding}")
    private String excludesFileCharset;

    /**
     * Whether to use the default excludes when scanning for files. The default
     * excludes are:
     * <ul>
     * <li>meta data files for source code management / revision control systems,
     * see {@link SourceCodeManagementSystems}</li>
     * <li>temporary files used by Maven, see
     * <a href="#useMavenDefaultExcludes">useMavenDefaultExcludes</a></li>
     * <li>configuration files for Eclipse, see
     * <a href="#useEclipseDefaultExcludes">useEclipseDefaultExcludes</a></li>
     * <li>configuration files for IDEA, see
     * <a href="#useIdeaDefaultExcludes">useIdeaDefaultExcludes</a></li>
     * </ul>
     */
    @Parameter(property = "rat.useDefaultExcludes", defaultValue = "true")
    private boolean useDefaultExcludes;

    /**
     * Whether to use the Maven specific default excludes when scanning for files.
     * Maven specific default excludes are given by the constant
     * MAVEN_DEFAULT_EXCLUDES: The <code>target</code> directory, the
     * <code>cobertura.ser</code> file, and so on.
     */
    @Parameter(property = "rat.useMavenDefaultExcludes", defaultValue = "true")
    private boolean useMavenDefaultExcludes;

    /**
     * Whether to parse source code management system (SCM) ignore files and use
     * their contents as excludes. At the moment this works for the following SCMs:
     *
     * @see org.apache.rat.config.SourceCodeManagementSystems
     */
    @Parameter(property = "rat.parseSCMIgnoresAsExcludes", defaultValue = "true")
    private boolean parseSCMIgnoresAsExcludes;

    /**
     * Whether to use the Eclipse specific default excludes when scanning for files.
     * Eclipse specific default excludes are given by the constant
     * ECLIPSE_DEFAULT_EXCLUDES: The <code>.classpath</code> and
     * <code>.project</code> files, the <code>.settings</code> directory, and so on.
     */
    @Parameter(property = "rat.useEclipseDefaultExcludes", defaultValue = "true")
    private boolean useEclipseDefaultExcludes;

    /**
     * Whether to use the IDEA specific default excludes when scanning for files.
     * IDEA specific default excludes are given by the constant
     * IDEA_DEFAULT_EXCLUDES: The <code>*.iml</code>, <code>*.ipr</code> and
     * <code>*.iws</code> files and the <code>.idea</code> directory.
     */
    @Parameter(property = "rat.useIdeaDefaultExcludes", defaultValue = "true")
    private boolean useIdeaDefaultExcludes;

    /**
     * Whether to exclude subprojects. This is recommended, if you want a separate
     * apache-rat-plugin report for each subproject.
     */
    @Parameter(property = "rat.excludeSubprojects", defaultValue = "true")
    private boolean excludeSubProjects;

    /**
     * Will skip the plugin execution, e.g. for technical builds that do not take
     * license compliance into account.
     *
     * @since 0.11
     */
    @Parameter(property = "rat.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Holds the maven-internal project to allow resolution of artifact properties
     * during mojo runs.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    /**
     * @return Returns the Maven project.
     */
    protected MavenProject getProject() {
        return project;
    }

    protected Defaults.Builder getDefaultsBuilder() {
        Defaults.Builder result = Defaults.builder();
        if (defaultLicenseFiles != null) {
            for (String defaultLicenseFile : defaultLicenseFiles) {
                try {
                    result.add(defaultLicenseFile);
                } catch (MalformedURLException e) {
                    throw new ConfigurationException(defaultLicenseFile + " is not a valid license file", e);
                }
            }
        }
        return result;
    }
    
    @Deprecated // remove this for version 1.0
    private Stream<License> getLicenses() {
        if (licenses == null) {
            return Stream.empty();
        }
        return Arrays.stream(licenses).filter( s -> {return s instanceof License;}).map(License.class::cast);
    }

    @Deprecated // remove this for version 1.0
    private Stream<DeprecatedConfig> getDeprecatedConfigs() {
        if (licenses == null) {
            return Stream.empty();
        }
        return Arrays.stream(licenses).filter( s -> {return s instanceof DeprecatedConfig;}).map(DeprecatedConfig.class::cast);
    }
    
    @Deprecated // remove this for version 1.0
    private void reportDeprecatedProcessing()
    {
        if (getDeprecatedConfigs().findAny().isPresent()) {
            Log log = getLog();
            log.warn("Configuration uses deprecated configuration.  Please upgrade to v0.17 configuration options");
        }
    }
    
    @Deprecated // remove this for version 1.0
    private void processLicenseFamilies(ReportConfiguration config) {
        List<ILicenseFamily> families = getDeprecatedConfigs().map(DeprecatedConfig::getLicenseFamily).filter(Objects::nonNull).collect(Collectors.toList());
        if (licenseFamilies != null) {
            for (SimpleLicenseFamily slf : licenseFamilies) {
                if (StringUtils.isBlank(slf.getFamilyCategory())) {
                    families.stream().filter( f -> f.getFamilyName().equalsIgnoreCase(slf.getFamilyName())).findFirst()
                    .ifPresent(config::addApprovedLicenseCategory);
                } else {
                    config.addApprovedLicenseCategory(ILicenseFamily.builder().setLicenseFamilyCategory(slf.getFamilyCategory())
                    .setLicenseFamilyName(StringUtils.defaultIfBlank(slf.getFamilyName(), slf.getFamilyCategory()))
                    .build());
                }
            }
        }
    }
    
    private org.apache.rat.utils.Log makeLog() {
        return new org.apache.rat.utils.Log() {
            final Log log = getLog();
            @Override
            public void log(Level level, String msg) {
                switch (level)
                {
                case DEBUG:
                    log.debug(msg);
                    break;
                case INFO:
                    log.info(msg);;
                    break;
                case WARN:
                    log.warn(msg);
                    break;
                case ERROR:
                    log.error(msg);
                    break;
                case OFF:
                    break;
            }
            }};
    }
    
    protected ReportConfiguration getConfiguration() throws MojoExecutionException {
        DefaultLog.setInstance(makeLog());
        try {
            ReportConfiguration config = Report.parseCommands(args().toArray(new String[0]),
                    o -> getLog().warn("Help option not supported"),
                    true);
            reportDeprecatedProcessing();

            if (additionalLicenseFiles != null) {
                for (String licenseFile : additionalLicenseFiles) {
                    try {
                        URL url = new File(licenseFile).toURI().toURL();
                        Format fmt = Format.fromName(licenseFile);
                        MatcherReader mReader = fmt.matcherReader();
                        if (mReader != null) {
                            mReader.addMatchers(url);
                        }
                        LicenseReader lReader = fmt.licenseReader();
                        if (lReader != null) {
                            lReader.addLicenses(url);
                            config.addLicenses(lReader.readLicenses());
                            config.addApprovedLicenseCategories(lReader.approvedLicenseId());
                        }
                    } catch (MalformedURLException e) {
                        throw new ConfigurationException(licenseFile + " is not a valid license file", e);
                    }
                }
            }
            if (families != null || getDeprecatedConfigs().findAny().isPresent()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s license families loaded from pom", families.length));
                }
                Consumer<ILicenseFamily> logger = log.isDebugEnabled() ? (l) -> log.debug(String.format("Family: %s", l))
                        : (l) -> {
                };

                Consumer<ILicenseFamily> process = logger.andThen(config::addFamily);
                getDeprecatedConfigs().map(DeprecatedConfig::getLicenseFamily).filter(Objects::nonNull).forEach(process);
                if (families != null) { // TODO remove if check in v1.0
                    Arrays.stream(families).map(Family::build).forEach(process);
                }
            }

            processLicenseFamilies(config);

            if (approvedLicenses != null && approvedLicenses.length > 0) {
                Arrays.stream(approvedLicenses).forEach(config::addApprovedLicenseCategory);
            }

            if (licenses != null) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s licenses loaded from pom", licenses.length));
                }
                Consumer<ILicense> logger = log.isDebugEnabled() ? (l) -> log.debug(String.format("License: %s", l))
                        : (l) -> {
                };
                Consumer<ILicense> addApproved = (approvedLicenses == null || approvedLicenses.length == 0)
                        ? (l) -> config.addApprovedLicenseCategory(l.getLicenseFamily())
                        : (l) -> {
                };

                Consumer<ILicense> process = logger.andThen(config::addLicense).andThen(addApproved);
                SortedSet<ILicenseFamily> families = config.getLicenseFamilies(LicenseFilter.ALL);
                getDeprecatedConfigs().map(DeprecatedConfig::getLicense).filter(Objects::nonNull)
                        .map(x -> x.setLicenseFamilies(families).build()).forEach(process);
                getLicenses().map(x -> x.build(families)).forEach(process);
            }

            config.setReportable(getReportable());
            return config;
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

    protected void logLicenses(Collection<ILicense> licenses) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("The following " + licenses.size() + " licenses are activated:");
            for (ILicense license : licenses) {
                getLog().debug("* " + license.toString());
            }
        }
    }

    /**
     * Creates an iterator over the files to check.
     *
     * @return A container of files, which are being checked.
     * @throws MojoExecutionException in case of errors. I/O errors result in
     * UndeclaredThrowableExceptions.
     */
    private IReportable getReportable() throws MojoExecutionException {
        final IgnoringDirectoryScanner ds = new IgnoringDirectoryScanner();
        ds.setBasedir(basedir);
        setExcludes(ds);
        setIncludes(ds);
        ds.scan();
        whenDebuggingLogExcludedFiles(ds);
        final String[] files = ds.getIncludedFiles();
        logAboutIncludedFiles(files);
        try {
            return new FilesReportable(basedir, files);
        } catch (final IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private void logAboutIncludedFiles(final String[] files) {
        if (files.length == 0) {
            getLog().warn("No resources included.");
        } else {
            getLog().debug(files.length + " resources included");
            if (getLog().isDebugEnabled()) {
                for (final String resource : files) {
                    getLog().debug(" - included " + resource);
                }
            }
        }
    }

    private void whenDebuggingLogExcludedFiles(final DirectoryScanner ds) {
        if (getLog().isDebugEnabled()) {
            final String[] excludedFiles = ds.getExcludedFiles();
            if (excludedFiles.length == 0) {
                getLog().debug("No excluded resources.");
            } else {
                getLog().debug("Excluded " + excludedFiles.length + " resources:");
                for (final String resource : excludedFiles) {
                    getLog().debug(" - excluded " + resource);
                }
            }
        }
    }

    private void setIncludes(DirectoryScanner ds) throws MojoExecutionException {
        if ((includes != null && includes.length > 0) || includesFile != null) {
            final List<String> includeList = new ArrayList<>();
            if (includes != null) {
                includeList.addAll(Arrays.asList(includes));
            }
            if (includesFile != null) {
                final String charset = includesFileCharset == null ? "UTF8" : includesFileCharset;
                final File f = new File(includesFile);
                if (!f.isFile()) {
                    getLog().error("IncludesFile not found: " + f.getAbsolutePath());
                } else {
                    getLog().debug("Includes loaded from file " + includesFile + ", using character set " + charset);
                }
                includeList.addAll(getPatternsFromFile(f, charset));
            }
            ds.setIncludes(includeList.toArray(new String[includeList.size()]));
        }
    }

    private List<String> getPatternsFromFile(File pFile, String pCharset) throws MojoExecutionException {
        InputStream is = null;
        BufferedInputStream bis = null;
        Reader r = null;
        BufferedReader br = null;
        Throwable th = null;
        final List<String> patterns = new ArrayList<>();
        try {
            is = Files.newInputStream(pFile.toPath());
            bis = new BufferedInputStream(is);
            r = new InputStreamReader(bis, pCharset);
            br = new BufferedReader(r);
            for (;;) {
                final String s = br.readLine();
                if (s == null) {
                    break;
                }
                patterns.add(s);
            }
            br.close();
            br = null;
            r.close();
            r = null;
            bis.close();
            bis = null;
            is.close();
            is = null;
        } catch (Throwable t) {
            th = t;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable t) {
                    if (th == null) {
                        th = t;
                    }
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (Throwable t) {
                    if (th == null) {
                        th = t;
                    }
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (Throwable t) {
                    if (th == null) {
                        th = t;
                    }
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable t) {
                    if (th == null) {
                        th = t;
                    }
                }
            }
        }
        if (th != null) {
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            if (th instanceof Error) {
                throw (Error) th;
            }
            throw new MojoExecutionException(th.getMessage(), th);
        }
        return patterns;
    }

    private void setExcludes(IgnoringDirectoryScanner ds) throws MojoExecutionException {
        final List<IgnoreMatcher> ignoreMatchers = mergeDefaultExclusions();
        if (!excludesList.isEmpty()) {
            getLog().debug("No excludes explicitly specified.");
        } else {
            getLog().debug(excludesList.size() + " explicit excludes.");
            for (final String exclude : excludesList) {
                getLog().debug("Exclude: " + exclude);
            }
        }

        final List<String> globExcludes = new ArrayList<>();
        for (IgnoreMatcher ignoreMatcher : ignoreMatchers) {
            if (ignoreMatcher instanceof GlobIgnoreMatcher) {
                // The glob matching we do via the DirectoryScanner
                globExcludes.addAll(((GlobIgnoreMatcher) ignoreMatcher).getExclusionLines());
            } else {
                // All others (git) are used directly
                ds.addIgnoreMatcher(ignoreMatcher);
            }
        }

        globExcludes.addAll(excludesList);
        if (!globExcludes.isEmpty()) {
            final String[] allExcludes = globExcludes.toArray(new String[globExcludes.size()]);
            ds.setExcludes(allExcludes);
        }
    }

    private List<IgnoreMatcher> mergeDefaultExclusions() throws MojoExecutionException {
        List<IgnoreMatcher> ignoreMatchers = new ArrayList<>();

        final GlobIgnoreMatcher basicRules = new GlobIgnoreMatcher();

        basicRules.addRules(addPlexusAndScmDefaults(getLog(), useDefaultExcludes));
        basicRules.addRules(addMavenDefaults(getLog(), useMavenDefaultExcludes));
        basicRules.addRules(addEclipseDefaults(getLog(), useEclipseDefaultExcludes));
        basicRules.addRules(addIdeaDefaults(getLog(), useIdeaDefaultExcludes));

        if (parseSCMIgnoresAsExcludes) {
            getLog().debug("Will parse SCM ignores for exclusions...");
            ignoreMatchers.addAll(ScmIgnoreParser.getExclusionsFromSCM(getLog(), project.getBasedir()));
            getLog().debug("Finished adding exclusions from SCM ignore files.");
        }

        if (excludeSubProjects && project != null && project.getModules() != null) {
            for (final String moduleSubPath : project.getModules()) {
                if (new File(basedir, moduleSubPath).isDirectory()) {
                    basicRules.addRule(moduleSubPath + "/**/*");
                } else {
                    basicRules.addRule(StringUtils.substringBeforeLast(moduleSubPath, "/") + "/**/*");
                }
            }
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Finished creating list of implicit excludes.");
            if (basicRules.getExclusionLines().isEmpty() && ignoreMatchers.isEmpty()) {
                getLog().debug("No excludes implicitly specified.");
            } else {
                if (!basicRules.getExclusionLines().isEmpty()) {
                    getLog().debug(basicRules.getExclusionLines().size() + " implicit excludes.");
                    for (final String exclude : basicRules.getExclusionLines()) {
                        getLog().debug("Implicit exclude: " + exclude);
                    }
                }
                for (IgnoreMatcher ignoreMatcher : ignoreMatchers) {
                    if (ignoreMatcher instanceof GlobIgnoreMatcher) {
                        GlobIgnoreMatcher globIgnoreMatcher = (GlobIgnoreMatcher) ignoreMatcher;
                        if (!globIgnoreMatcher.getExclusionLines().isEmpty()) {
                            getLog().debug(globIgnoreMatcher.getExclusionLines().size() + " implicit excludes from SCM.");
                            for (final String exclude : globIgnoreMatcher.getExclusionLines()) {
                                getLog().debug("Implicit exclude: " + exclude);
                            }
                        }
                    } else {
                        getLog().debug("Implicit exclude: \n" + ignoreMatcher);
                    }
                }
            }
        }
        if (!excludesFileList.isEmpty()) {
            for (String fileName : excludesFileList) {
                final File f = new File(fileName);
                if (!f.isFile()) {
                    getLog().error("Excludes file not found: " + f.getAbsolutePath());
                }
                if (!f.canRead()) {
                    getLog().error("Excludes file not readable: " + f.getAbsolutePath());
                }
                final String charset = excludesFileCharset == null ? "UTF8" : excludesFileCharset;
                getLog().debug("Loading excludes from file " + f + ", using character set " + charset);
                basicRules.addRules(getPatternsFromFile(f, charset));
            }
        }

        if (!basicRules.isEmpty()) {
            ignoreMatchers.add(basicRules);
        }

        return ignoreMatchers;
    }
}
