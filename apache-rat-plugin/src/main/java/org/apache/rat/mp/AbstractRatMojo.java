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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
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

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.license.DeprecatedConfig;
import org.apache.rat.commandline.Arg;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.configuration.Format;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.MatcherReader;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.license.SimpleLicenseFamily;
import org.apache.rat.plugin.BaseRatMojo;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.DirectoryWalker;

import static java.lang.String.format;

/**
 * Abstract base class for Mojos, which are running Rat.
 */
public abstract class AbstractRatMojo extends BaseRatMojo {
    /** Report configuration for report */
    private ReportConfiguration reportConfiguration;
    /**
     * The base directory, in which to search for files.
     */
    @Parameter(property = "rat.basedir", defaultValue = "${basedir}", required = true)
    private File basedir;

    /**
     * Specifies the licenses to accept. By default, these are added to the default
     * licenses, unless you set &lt;addDefaultLicenseMatchers&gt; to false.  Arguments should be
     * file name of &lt;Configs&gt; file structure.
     * @deprecated Use &lt;config&gt;.
     * @since 0.8
     */
    @Parameter
    @Deprecated
    private String[] defaultLicenseFiles;

    /**
     * Specifies the additional licenses file.
     * @deprecated Use &lt;config&gt;.
     */
    @Parameter
    @Deprecated
    private String[] additionalLicenseFiles;

    /**
     * Whether to add the default list of licenses.
     * @deprecated Deprecated for removal since 0.17: Use &lt;configurationNoDefaults&gt; instead (note the change of state).
     */
    @Deprecated
    @Parameter(property = "rat.addDefaultLicenses", name = "addDefaultLicenses")
    public void setAddDefaultLicenses(final boolean addDefaultLicenses) {
        setNoDefaultLicenses(!addDefaultLicenses);
    }

    /**
     * Whether to add the default list of license matchers.
     * @deprecated Use &lt;config&gt;.
     */
    @Deprecated
    @Parameter(property = "rat.addDefaultLicenseMatchers")
    private boolean addDefaultLicenseMatchers;

    /** The list of approved licenses
     * @deprecated Use &lt;config&gt;.
     */
    @Deprecated
    @Parameter(required = false)
    private String[] approvedLicenses;

    /** The file of approved licenses
     * @deprecated Use &lt;config&gt;.
     */
    @Deprecated
    @Parameter(property = "rat.approvedFile")
    private String approvedLicenseFile;

    /**
     * Specifies the license families to accept.
     *
     * @since 0.8
     * @deprecated Use LicenseFamily section of configuration file.
     */
    @Deprecated
    @Parameter
    private SimpleLicenseFamily[] licenseFamilies;

    /** The list of license definitions.
     * @deprecated Deprecated for removal since 0.17: Use &lt;Config&gt; instead. See configuration file documentation.
     */
    @Deprecated
    @Parameter
    private Object[] licenses;

    /** The list of family definitions.
     * @deprecated Use &lt;Configs&gt;.
     */
    @Deprecated
    @Parameter
    private Family[] families;

    /**
     * Specifies the include files character set.
     * If ${project.build.sourceEncoding} is not set defaults to UTF-8.
     */
    @Parameter(property = "rat.includesFileCharset", defaultValue = "${project.build.sourceEncoding}")
    private String includesFileCharset;

    /**
     * Specifies the include files character set.
     * If ${project.build.sourceEncoding} is not set defaults to UTF-8.
     */
    @Parameter(property = "rat.excludesFileCharset", defaultValue = "${project.build.sourceEncoding}")
    private String excludesFileCharset;

    /**
     * Whether to use the default excludes when scanning for files. The default
     * excludes are:
     * <ul>
     * <li>meta data files for source code management / revision control systems,
     * see {@link org.apache.rat.config.exclusion.StandardCollection}</li>
     * <li>temporary files used by Maven, see
     * <a href="#useMavenDefaultExcludes">useMavenDefaultExcludes</a></li>
     * <li>configuration files for Eclipse, see
     * <a href="#useEclipseDefaultExcludes">useEclipseDefaultExcludes</a></li>
     * <li>configuration files for IDEA, see
     * <a href="#useIdeaDefaultExcludes">useIdeaDefaultExcludes</a></li>
     * </ul>
     * @deprecated When set to true specifies that the STANDARD_PATTERNS are excluded, as are
     * the STANDARD_SCMS patterns. Use the various InputExclude and InputInclude elements to
     * explicitly specify what to include or exclude.
     */
    @Parameter(property = "rat.useDefaultExcludes", defaultValue = "true")
    @Deprecated
    private boolean useDefaultExcludes;

    /**
     * Whether to use the Maven specific default excludes when scanning for files.
     * Maven specific default excludes are given by the constant
     * MAVEN_DEFAULT_EXCLUDES: The <code>target</code> directory, the
     * <code>cobertura.ser</code> file, and so on.
     * @deprecated When set to true specifies that the MAVEN patterns are excluded.
     * Use "inputIncludeStd MAVEN" to override.
     */
    @Parameter(property = "rat.useMavenDefaultExcludes", defaultValue = "true")
    @Deprecated
    private boolean useMavenDefaultExcludes;

    /**
     * Whether to parse source code management system (SCM) ignore files and use
     * their contents as excludes. At the moment this works for the following SCMs:
     *
     * @see org.apache.rat.config.exclusion.StandardCollection
     * @deprecated When set to true specifies that the STANDARD_SCMS exclusion file
     * processors are used to exclude files and directories (e.g. ".gitignore" or ".hgignore").
     * Use "inputIncludeStd STANDARD_SCMS" to override.
     */
    @Parameter(property = "rat.parseSCMIgnoresAsExcludes", defaultValue = "true")
    @Deprecated
    private boolean parseSCMIgnoresAsExcludes;

    /**
     * Whether to use the Eclipse specific default excludes when scanning for files.
     * Eclipse specific default excludes are given by the constant
     * ECLIPSE_DEFAULT_EXCLUDES: The <code>.classpath</code> and
     * <code>.project</code> files, the <code>.settings</code> directory, and so on.
     * @deprecated When set to true specifies that the ECLIPSE patterns are excluded.
     * Use "inputIncludeStd ECLIPSE" to override.
     */
    @Parameter(property = "rat.useEclipseDefaultExcludes", defaultValue = "true")
    @Deprecated
    private boolean useEclipseDefaultExcludes;

    /**
     * Whether to use the IDEA specific default excludes when scanning for files.
     * IDEA specific default excludes are given by the constant
     * IDEA_DEFAULT_EXCLUDES: The <code>*.iml</code>, <code>*.ipr</code> and
     * <code>*.iws</code> files and the <code>.idea</code> directory.
     * @deprecated When set to true specifies that the IDEA patterns are excluded.
     * Use "inputIncludeStd IDEA" to override.
     */
    @Deprecated
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
     * @return the Maven project.
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
        return Arrays.stream(licenses).filter(s -> s instanceof License).map(License.class::cast);
    }

    @Deprecated // remove this for version 1.0
    private Stream<DeprecatedConfig> getDeprecatedConfigs() {
        if (licenses == null) {
            return Stream.empty();
        }
        return Arrays.stream(licenses).filter(s -> s instanceof DeprecatedConfig).map(DeprecatedConfig.class::cast);
    }

    @Deprecated // remove this for version 1.0
    private void reportDeprecatedProcessing() {
        if (getDeprecatedConfigs().findAny().isPresent()) {
            DefaultLog.getInstance().warn("Configuration uses deprecated configuration. You need to upgrade to v0.17 configuration options.");
        }
    }

    @Deprecated // remove this for version 1.0
    private void processLicenseFamilies(final ReportConfiguration config) {
        List<ILicenseFamily> families = getDeprecatedConfigs().map(DeprecatedConfig::getLicenseFamily).filter(Objects::nonNull).collect(Collectors.toList());
        if (licenseFamilies != null) {
            for (SimpleLicenseFamily slf : licenseFamilies) {
                if (StringUtils.isBlank(slf.getFamilyCategory())) {
                    families.stream().filter(f -> f.getFamilyName().equalsIgnoreCase(slf.getFamilyName())).findFirst()
                    .ifPresent(config::addApprovedLicenseCategory);
                } else {
                    config.addApprovedLicenseCategory(ILicenseFamily.builder().setLicenseFamilyCategory(slf.getFamilyCategory())
                    .setLicenseFamilyName(StringUtils.defaultIfBlank(slf.getFamilyName(), slf.getFamilyCategory()))
                    .build());
                }
            }
        }
    }

    /**
     * Reads values for the Arg.
     *
     * @param arg The Arg to get the values for.
     * @return The list of values or an empty list.
     */
    protected List<String> getValues(final Arg arg) {
        List<String> result = new ArrayList<>();
        for (Option option : arg.group().getOptions()) {
            if (option.getLongOpt() != null) {
                List<String> args = getArg(option.getLongOpt());
                if (args != null) {
                    result.addAll(args);
                }
            }
        }
        return result;
    }

    /**
     * Removes all values for an Arg.
     * @param arg The arg to remove values for.
     */
    protected void removeKey(final Arg arg) {
        for (Option option : arg.group().getOptions()) {
            if (option.getLongOpt() != null) {
                removeArg(option.getLongOpt());
            }
        }
    }

    private org.apache.rat.utils.Log makeLog() {
        return new org.apache.rat.utils.Log() {
            private final org.apache.maven.plugin.logging.Log log = getLog();

            @Override
            public Level getLevel() {
                if (log.isDebugEnabled()) {
                    return Level.DEBUG;
                }
                if (log.isInfoEnabled()) {
                    return Level.INFO;
                }
                if (log.isWarnEnabled()) {
                    return Level.WARN;
                }
                if (log.isErrorEnabled()) {
                    return Level.ERROR;
                }
                return Level.OFF;
            }

            @Override
            public void log(final Level level, final String message, final Throwable throwable) {
                switch (level) {
                    case DEBUG:
                        if (throwable != null) {
                            log.debug(message, throwable);
                        } else {
                            log.debug(message);
                        }
                        break;
                    case INFO:
                        if (throwable != null) {
                            log.info(message, throwable);
                        } else {
                            log.info(message);
                        }
                        break;
                    case WARN:
                        if (throwable != null) {
                            log.warn(message, throwable);
                        } else {
                            log.warn(message);
                        }
                        break;
                    case ERROR:
                        if (throwable != null) {
                            log.error(message, throwable);
                        } else {
                            log.error(message);
                        }
                        break;
                    case OFF:
                        break;
                }
            }

            @Override
            public void log(final Level level, final String msg) {
                switch (level) {
                    case DEBUG:
                        log.debug(msg);
                        break;
                    case INFO:
                        log.info(msg);
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
            }
        };
    }

    private void setIncludeExclude() {

        if (excludeSubProjects && project != null && project.getModules() != null) {
            List<String> subModules = new ArrayList<>();
            project.getModules().forEach(s -> subModules.add(format("%s/**", s)));
            setInputExcludes(subModules.toArray(new String[0]));
        }

        List<String> values = getValues(Arg.EXCLUDE);
        if (values.isEmpty() && useDefaultExcludes) {
            DefaultLog.getInstance().debug("Adding plexus default exclusions...");
            setInputExcludes(StandardCollection.STANDARD_PATTERNS.patterns().toArray(new String[0]));

            DefaultLog.getInstance().debug("Adding SCM default exclusions...");
            setInputExcludes(StandardCollection.STANDARD_SCMS.patterns().toArray(new String[0]));
        }

        if (useMavenDefaultExcludes) {
            setInputExcludeStd(StandardCollection.MAVEN.name());
        }
        if (useEclipseDefaultExcludes) {
            setInputExcludeStd(StandardCollection.ECLIPSE.name());
        }
        if (useIdeaDefaultExcludes) {
            setInputExcludeStd(StandardCollection.IDEA.name());
        }

        if (parseSCMIgnoresAsExcludes) {
            setInputExcludeParsedScm(StandardCollection.STANDARD_SCMS.name());
        }
    }

    protected ReportConfiguration getConfiguration() throws MojoExecutionException {
        if (reportConfiguration == null) {
            DefaultLog.setInstance(makeLog());
            try {
                Log log = DefaultLog.getInstance();
                if (super.getLog().isDebugEnabled()) {
                    log.debug("Start BaseRatMojo Configuration options");
                    for (Map.Entry<String, List<String>> entry : args.entrySet()) {
                        log.debug(format(" * %s %s", entry.getKey(), String.join(", ", entry.getValue())));
                    }
                    log.debug("End BaseRatMojo Configuration options");
                }

                boolean helpLicenses = !getValues(Arg.HELP_LICENSES).isEmpty();
                removeKey(Arg.HELP_LICENSES);

                setIncludeExclude();

                ReportConfiguration config = OptionCollection.parseCommands(args().toArray(new String[0]),
                        o -> getLog().warn("Help option not supported"),
                        true);
                reportDeprecatedProcessing();

                if (additionalLicenseFiles != null) {
                    for (String licenseFile : additionalLicenseFiles) {
                        URI uri = new File(licenseFile).toURI();
                        Format fmt = Format.from(licenseFile);
                        MatcherReader mReader = fmt.matcherReader();
                        if (mReader != null) {
                            mReader.addMatchers(uri);
                        }
                        LicenseReader lReader = fmt.licenseReader();
                        if (lReader != null) {
                            lReader.addLicenses(uri);
                            config.addLicenses(lReader.readLicenses());
                            config.addApprovedLicenseCategories(lReader.approvedLicenseId());
                        }
                    }
                }
                if (families != null || getDeprecatedConfigs().findAny().isPresent()) {
                    if (super.getLog().isDebugEnabled()) {
                        log.debug(format("%s license families loaded from pom", families.length));
                    }
                    Consumer<ILicenseFamily> logger = super.getLog().isDebugEnabled() ? l -> log.debug(format("Family: %s", l))
                            : l -> {
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
                    if (super.getLog().isDebugEnabled()) {
                        log.debug(format("%s licenses loaded from pom", licenses.length));
                    }
                    Consumer<ILicense> logger = super.getLog().isDebugEnabled() ? l -> log.debug(format("License: %s", l))
                            : l -> {
                    };
                    Consumer<ILicense> addApproved = (approvedLicenses == null || approvedLicenses.length == 0)
                            ? l -> config.addApprovedLicenseCategory(l.getLicenseFamily())
                            : l -> {
                    };

                    Consumer<ILicense> process = logger.andThen(config::addLicense).andThen(addApproved);
                    SortedSet<ILicenseFamily> families = config.getLicenseFamilies(LicenseFilter.ALL);
                    getDeprecatedConfigs().map(DeprecatedConfig::getLicense).filter(Objects::nonNull)
                            .map(x -> x.setLicenseFamilies(families).build()).forEach(process);
                    getLicenses().map(x -> x.build(families)).forEach(process);
                }
                DocumentName dirName = new DocumentName(basedir);
                config.addSource(new DirectoryWalker(new FileDocument(dirName, basedir, config.getNameMatcher(dirName))));

                if (helpLicenses) {
                    new org.apache.rat.help.Licenses(config, new PrintWriter(log.asWriter())).printHelp();
                }
                reportConfiguration = config;
            } catch (IOException e) {
                throw new MojoExecutionException(e);
            }
        }
        return reportConfiguration;
    }

    protected void logLicenses(final Collection<ILicense> licenses) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("The following " + licenses.size() + " licenses are activated:");
            for (ILicense license : licenses) {
                getLog().debug("* " + license.toString());
            }
        }
    }
}
