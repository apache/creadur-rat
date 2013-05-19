package org.apache.rat.mp;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.rat.Defaults;
import org.apache.rat.Report;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.api.RatException;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.claim.ClaimStatistic;
import org.codehaus.plexus.util.DirectoryScanner;

import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Abstract base class for Mojos, which are running Rat.
 */
public abstract class AbstractRatMojo extends AbstractMojo
{
    /**
     * The Maven specific default excludes.
     */
    public static final String[] MAVEN_DEFAULT_EXCLUDES = new String[] {
        "target/**/*", "cobertura.ser", "release.properties",
        "pom.xml.releaseBackup"
    };

    /**
     * The Eclipse specific default excludes.
     */
    public static final String[] ECLIPSE_DEFAULT_EXCLUDES = new String[] { ".classpath", ".project", ".settings/**/*" };

    /**
     * The IDEA specific default excludes.
     */
    public static final String[] IDEA_DEFAULT_EXCLUDES = new String[] { "*.iml", "*.ipr", "*.iws", ".idea/**/*" };

    /**
     * The base directory, in which to search for files.
     *
     */
    @Parameter (property = "rat.basedir", defaultValue = "${basedir}", required = true)
    protected File basedir;

    /**
     * Specifies the licenses to accept. Deprecated, use {@link #licenses} instead.
     *
     * @deprecated Use {@link #licenses} instead.
     */
    @Deprecated
    @Parameter
    private HeaderMatcherSpecification[] licenseMatchers;

    /**
     * Specifies the licenses to accept. By default, these are added to the default
     * licenses, unless you set {@link #addDefaultLicenseMatchers} to true.
     *
     * @since 0.8
     */
    @Parameter
    private IHeaderMatcher[] licenses;

    /**
     * The set of approved license family names.
     * @deprecated Use {@link #licenseFamilies} instead.
     */
    @Deprecated
    private LicenseFamilySpecification[] licenseFamilyNames;

    /**
     * Specifies the license families to accept.
     *
     * @since 0.8
     */
    @Parameter
    private ILicenseFamily[] licenseFamilies;

    /**
     * Whether to add the default list of license matchers.
     *
     */
    @Parameter(property = "rat.addDefaultLicenseMatchers", defaultValue = "true")
    private boolean addDefaultLicenseMatchers;

    /**
     * Specifies files, which are included in the report. By default, all files are included.
     *
     */
    @Parameter
    private String[] includes;

    /**
     * Specifies files, which are excluded in the report. By default, no files are excluded.
     *
     */
    @Parameter
    private String[] excludes;

    /**
     * Whether to use the default excludes when scanning for files.
     * The default excludes are:
     * <ul>
     *   <li>meta data files for version control systems</li>
     *   <li>temporary files used by Maven, see <a href="#useMavenDefaultExcludes">useMavenDefaultExcludes</a></li>
     *   <li>configuration files for Eclipse, see <a href="#useEclipseDefaultExcludes">useEclipseDefaultExcludes</a></li>
     *   <li>configuration files for IDEA, see <a href="#useIdeaDefaultExcludes">useIdeaDefaultExcludes</a></li>
     * </ul>
     */
    @Parameter(property = "rat.useDefaultExcludes", defaultValue = "true")
    private boolean useDefaultExcludes;

    /**
     * Whether to use the Maven specific default excludes when scanning for files. Maven specific default excludes are
     * given by the constant MAVEN_DEFAULT_EXCLUDES: The <code>target</code> directory, the <code>cobertura.ser</code>
     * file, and so on.
     */
    @Parameter(property = "rat.useMavenDefaultExcludes", defaultValue = "true")
    private boolean useMavenDefaultExcludes;

    /**
     * Whether to use the Eclipse specific default excludes when scanning for files. Eclipse specific default excludes
     * are given by the constant ECLIPSE_DEFAULT_EXCLUDES: The <code>.classpath</code> and <code>.project</code> files,
     * the <code>.settings</code> directory, and so on.
     */
    @Parameter(property = "rat.useEclipseDefaultExcludes", defaultValue = "true")
    private boolean useEclipseDefaultExcludes;

    /**
     * Whether to use the IDEA specific default excludes when scanning for files. IDEA specific default excludes are
     * given by the constant IDEA_DEFAULT_EXCLUDES: The <code>*.iml</code>, <code>*.ipr</code> and <code>*.iws</code>
     * files and the <code>.idea</code> directory.
     */
    @Parameter(property = "rat.useIdeaDefaultExcludes", defaultValue = "true")
    private boolean useIdeaDefaultExcludes;

    /**
     * Whether to exclude subprojects. This is recommended, if you want a
     * separate apache-rat-plugin report for each subproject.
     *
     */
    @Parameter(property = "rat.excludeSubprojects", defaultValue = "true")
    private boolean excludeSubProjects;

    /**
     *
     */
    @Component
    private MavenProject project;

    /**
     * Returns the Maven project.
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Returns the set of {@link IHeaderMatcher header matchers} to use.
     *
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             An error occurred while calculating the result.
     * @return Array of license matchers to use
     */
    protected IHeaderMatcher[] getLicenseMatchers() throws MojoFailureException, MojoExecutionException
    {
        final List<IHeaderMatcher> list = new ArrayList<IHeaderMatcher>();
        if ( licenses != null )
        {
            list.addAll( Arrays.asList( licenses ) );
        }

        if ( licenseMatchers != null )
        {
            for (final HeaderMatcherSpecification spec : licenseMatchers) {
                final String className = spec.getClassName();
                final IHeaderMatcher headerMatcher = newInstance(IHeaderMatcher.class, className);
                list.add(headerMatcher);
            }
        }

        if ( addDefaultLicenseMatchers )
        {
            list.addAll( Arrays.asList( Defaults.DEFAULT_MATCHERS ) );
        }
        return list.toArray( new IHeaderMatcher[list.size()] );
    }

    private <T> T newInstance( final Class<T> clazz, final String className )
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            @SuppressWarnings("unchecked") // incorrect cast will be caught below
            final T o = (T) cl.loadClass( className ).newInstance();

            if ( !clazz.isAssignableFrom( o.getClass() ) )
            {
                throw new MojoFailureException( "The class " + o.getClass().getName() + " does not implement "
                                + clazz.getName() );
            }
            return o;
        }
        catch ( InstantiationException e )
        {
            throw new MojoExecutionException( "Failed to instantiate class " + className + ": " + e.getMessage(), e );
        }
        catch ( ClassCastException e )
        {
            throw new MojoExecutionException( "The class " + className + " is not implementing " + clazz.getName()
                            + ": " + e.getMessage(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new MojoExecutionException( "Illegal access to class " + className + ": " + e.getMessage(), e );
        }
        catch ( ClassNotFoundException e )
        {
            throw new MojoExecutionException( "Class " + className + " not found: " + e.getMessage(), e );
        }
    }

    /**
     * Adds the given string array to the list.
     *
     * @param pList
     *            The list to which the array elements are being added.
     * @param pArray
     *            The strings to add to the list.
     */
    private void add( List<String> pList, String[] pArray )
    {
        if ( pArray != null )
        {
            Collections.addAll(pList, pArray);
        }
    }

    /**
     * Creates an iterator over the files to check.
     *
     * @return A container of files, which are being checked.
     */
    protected IReportable getResources()
    {
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( basedir );
        setExcludes( ds );
        setIncludes( ds );
        ds.scan();
        whenDebuggingLogExcludedFiles(ds);
        final String[] files = ds.getIncludedFiles();
        logAboutIncludedFiles(files);
        try
        {
            return new FilesReportable( basedir, files );
        }
        catch ( IOException e )
        {
            throw new UndeclaredThrowableException( e );
        }
    }

    private void logAboutIncludedFiles(final String[] files) {
        if (files.length == 0) {
            getLog().warn("No resources included.");
        } else {
            getLog().info(files.length + " resources included (use -debug for more details)");
            if(getLog().isDebugEnabled()) {
                for (String resource: files) {
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

    private void setIncludes( DirectoryScanner ds )
    {
        if ( includes != null )
        {
            ds.setIncludes( includes );
        }
    }

    private void setExcludes( DirectoryScanner ds )
    {
        final List<String> excludeList = buildDefaultExclusions();
        if ( excludes == null  ||  excludes.length == 0 )
        {
            getLog().info( "No excludes explicitly specified." );
        }
        else
        {
            for ( String exclude : excludes)
            {
                getLog().info( "Exclude: " + exclude);
            }
        }
        add( excludeList, excludes );
        if ( !excludeList.isEmpty() )
        {
            String[] allExcludes = excludeList.toArray( new String[excludeList.size()] );
            ds.setExcludes( allExcludes );
        }
    }

    private List<String> buildDefaultExclusions() {
        final List<String> results = new ArrayList<String>();

        addPlexusDefaults(results);

        addMavenDefaults(results);

        addEclipseDefaults(results);

        addIdeaDefaults(results);

        if ( excludeSubProjects && project != null && project.getModules() != null )
        {
            for (Object o : project.getModules()) {
                String moduleSubPath = (String) o;
                results.add(moduleSubPath + "/**/*");
            }
        }

        getLog().debug("Finished creating list of implicit excludes.");
        if (results.isEmpty()) {
            getLog().info( "No excludes implicitly specified." );
        } else {
            getLog().info( results.size() + " implicit excludes (use -debug for more details)." );
            for ( final String exclude : results ) {
                getLog().debug( "Implicit exclude: " + exclude);
            }
        }

        return results;
    }

    private void addPlexusDefaults(final List<String> excludeList1) {
        if ( useDefaultExcludes )
        {
            getLog().debug("Adding plexus default exclusions...");
            add( excludeList1, DirectoryScanner.DEFAULTEXCLUDES );
        }
        else
        {
            getLog().debug("rat.useDefaultExcludes set to false. " +
                    "Plexus default exclusions will not be added");
        }
    }

    private void addMavenDefaults(final List<String> excludeList1) {
        if ( useMavenDefaultExcludes )
        {
            getLog().debug("Adding exclusions often needed by Maven projects...");
            add( excludeList1, MAVEN_DEFAULT_EXCLUDES );
        }
        else
        {
            getLog().debug("rat.useMavenDefaultExcludes set to false. " +
                    "Exclusions often needed by Maven projects will not be added.");
        }
    }

    private void addEclipseDefaults(final List<String> excludeList1) {
        if ( useEclipseDefaultExcludes )
        {
            getLog().debug("Adding exclusions often needed by projects " +
                    "developed in Eclipse...");
            add( excludeList1, ECLIPSE_DEFAULT_EXCLUDES );
        }
        else
        {
            getLog().debug("rat.useEclipseDefaultExcludes set to false. " +
                    "Exclusions often needed by projects developed in " +
                    "Eclipse will not be added.");
        }
    }

    private void addIdeaDefaults(final List<String> excludeList1) {
        if ( useIdeaDefaultExcludes )
        {
            getLog().debug("Adding exclusions often needed by projects " +
                    "developed in IDEA...");
            add( excludeList1, IDEA_DEFAULT_EXCLUDES );
        }
        else
        {
            getLog().debug("rat.useIdeaDefaultExcludes set to false. " +
                    "Exclusions often needed by projects developed in " +
                    "IDEA will not be added.");
        }
    }

    /**
     * Writes the report to the given stream.
     *
     * @param out The target writer, to which the report is being written.
     * @param style The stylesheet to use, or <code>null</code> for raw XML
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             Another error occurred while creating the report.
     */
    protected ClaimStatistic createReport( Writer out, InputStream style ) throws MojoExecutionException, MojoFailureException
    {
        final ReportConfiguration configuration = getConfiguration();
        try
        {
            if (style != null) {
                return Report.report( out, getResources(), style, configuration );
            } else {
                return Report.report( getResources(), out, configuration );
            }
        }
        catch ( TransformerConfigurationException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( InterruptedException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( RatException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    protected ReportConfiguration getConfiguration() throws MojoFailureException,
            MojoExecutionException {
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher( new HeaderMatcherMultiplexer( getLicenseMatchers() ) );
        configuration.setApprovedLicenseNames(getApprovedLicenseNames());
        return configuration;
    }

    private ILicenseFamily[] getApprovedLicenseNames() throws MojoExecutionException, MojoFailureException
    {
        final List<ILicenseFamily> list = new ArrayList<ILicenseFamily>();
        if ( licenseFamilies != null )
        {
            list.addAll( Arrays.asList( licenseFamilies ) );
        }
        if ( licenseFamilyNames != null)
        {
            for (LicenseFamilySpecification spec : licenseFamilyNames) {
                list.add(newInstance(ILicenseFamily.class, spec.getClassName()));
            }
        }

        if ( list.isEmpty() )
        {
            return null;
        }
        return list.toArray( new ILicenseFamily[ list.size() ] );
    }
}
