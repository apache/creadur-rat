package org.codehaus.mojo.rat;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import rat.Defaults;
import rat.analysis.IHeaderMatcher;
import rat.analysis.util.HeaderMatcherMultiplexer;
import rat.license.ILicenseFamily;
import rat.report.IReportable;
import rat.report.RatReportFailedException;

/**
 * Abstract base class for Mojos, which are running RAT.
 */
public abstract class AbstractRatMojo extends AbstractMojo
{
    /**
     * The Maven specific default excludes.
     */
    public static final String[] MAVEN_DEFAULT_EXCLUDES = new String[] { "target/**/*", "cobertura.ser" };

    /**
     * The Eclipse specific default excludes.
     */
    public static final String[] ECLIPSE_DEFAULT_EXCLUDES = new String[] { ".classpath", ".project", ".settings/**/*" };

    /**
     * The IDEA specific default excludes.
     */
    public static final String[] IDEA_DEFAULT_EXCLUDES = new String[] { "*.iml", "*.ipr", "*.iws" };

    /**
     * The base directory, in which to search for files.
     * 
     * @parameter expression="${rat.basedir}" default-value="${basedir}"
     * @required
     */
    protected File basedir;

    /**
     * The licenses we want to match on.
     * 
     * @parameter
     */
    private HeaderMatcherSpecification[] licenseMatchers;

    /**
     * The set of approved license family names.
     */
    private LicenseFamilySpecification[] licenseFamilyNames;

    /**
     * Whether to add the default list of license matchers.
     * 
     * @parameter expression="${rat.addDefaultLicenseMatchers}" default-value="true"
     */
    private boolean addDefaultLicenseMatchers;

    /**
     * Specifies files, which are included in the report. By default, all files are included.
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * Specifies files, which are excluded in the report. By default, no files are excluded.
     * 
     * @parameter
     */
    private String[] excludes;

    /**
     * Whether to use the default excludes when scanning for files.
     * 
     * @parameter expression="${rat.useDefaultExcludes}" default-value="true"
     */
    private boolean useDefaultExcludes;

    /**
     * Whether to use the Maven specific default excludes when scanning for files. Maven specific default excludes are
     * given by the constant MAVEN_DEFAULT_EXCLUDES: The target directory, the cobertura.ser file, and so on.
     * 
     * @parameter expression="${rat.useMavenDefaultExcludes}" default-value="true"
     */
    private boolean useMavenDefaultExcludes;

    /**
     * Whether to use the Eclipse specific default excludes when scanning for files. Eclipse specific default excludes
     * are given by the constant ECLIPSE_DEFAULT_EXCLUDES: The .classpath and .project files, the .settings directory,
     * and so on.
     * 
     * @parameter expression="${rat.useEclipseDefaultExcludes}" default-value="true"
     */
    private boolean useEclipseDefaultExcludes;

    /**
     * Whether to use the IDEA specific default excludes when scanning for files. IDEA specific default excludes are
     * given by the constant IDEA_DEFAULT_EXCLUDES: The *.iml, *.ipr and *.iws files.
     * 
     * @parameter expression="${rat.useIdeaDefaultExcludes}" default-value="true"
     */
    private boolean useIdeaDefaultExcludes;

    /**
     * Whether to exclude subprojects. This is recommended, if you want a separate rat-maven-plugin report for each
     * subproject.
     * 
     * @parameter expression="${rat.excludeSubprojects}" default-value="true"
     */
    private boolean excludeSubProjects;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
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
        final List list = new ArrayList();
        if ( licenseMatchers != null )
        {
            for ( int i = 0; i < licenseMatchers.length; i++ )
            {
                final HeaderMatcherSpecification spec = licenseMatchers[i];
                final String className = spec.getClassName();
                final IHeaderMatcher headerMatcher = (IHeaderMatcher) newInstance( IHeaderMatcher.class, className );
                list.add( headerMatcher );
            }
        }

        if ( addDefaultLicenseMatchers )
        {
            list.addAll( Arrays.asList( Defaults.DEFAULT_MATCHERS ) );
        }
        return (IHeaderMatcher[]) list.toArray( new IHeaderMatcher[list.size()] );
    }

    private Object newInstance( final Class clazz, final String className )
        throws MojoExecutionException, MojoFailureException
    {
        final Object o;
        try
        {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            o = cl.loadClass( className ).newInstance();
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

        if ( !clazz.isAssignableFrom( o.getClass() ) )
        {
            throw new MojoFailureException( "The class " + o.getClass().getName() + " does not implement "
                            + clazz.getName() );
        }
        return o;
    }

    /**
     * Adds the given string array to the list.
     * 
     * @param pList
     *            The list to which the array elements are being added.
     * @param pArray
     *            The strings to add to the list.
     */
    private void add( List pList, String[] pArray )
    {
        if ( pArray != null )
        {
            for ( int i = 0; i < pArray.length; i++ )
            {
                pList.add( pArray[i] );
            }
        }
    }

    /**
     * Creates an iterator over the files to check.
     * 
     * @return A container of files, which are being checked.
     */
    protected IReportable getResources()
    {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( basedir );
        setExcludes( ds );
        setIncludes( ds );
        ds.scan();
        final String[] files = ds.getIncludedFiles();
        try
        {
            return new FilesReportable( basedir, files );
        }
        catch ( IOException e )
        {
            throw new UndeclaredThrowableException( e );
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
        final List excludeList1 = new ArrayList();
        if ( useDefaultExcludes )
        {
            add( excludeList1, DirectoryScanner.DEFAULTEXCLUDES );
        }
        if ( useMavenDefaultExcludes )
        {
            add( excludeList1, MAVEN_DEFAULT_EXCLUDES );
        }
        if ( useEclipseDefaultExcludes )
        {
            add( excludeList1, ECLIPSE_DEFAULT_EXCLUDES );
        }
        if ( useIdeaDefaultExcludes )
        {
            add( excludeList1, IDEA_DEFAULT_EXCLUDES );
        }
        if ( excludeSubProjects && project != null && project.getModules() != null )
        {
            for ( Iterator it = project.getModules().iterator(); it.hasNext(); )
            {
                String moduleSubPath = (String) it.next();
                excludeList1.add( moduleSubPath + "/**/*" );
            }
        }
        final List excludeList = excludeList1;
        add( excludeList, excludes );
        if ( !excludeList.isEmpty() )
        {
            String[] allExcludes = (String[]) excludeList.toArray( new String[excludeList.size()] );
            ds.setExcludes( allExcludes );
        }
    }

    /**
     * Writes the report to the given stream.
     * 
     * @param out The target writer, to which the report is being written.
     * @param style The stylesheet to use
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             Another error occurred while creating the report.
     */
    protected void createReport( PrintWriter out, InputStream style ) throws MojoExecutionException, MojoFailureException
    {
        HeaderMatcherMultiplexer m = new HeaderMatcherMultiplexer( getLicenseMatchers() );
        try
        {
            rat.Report.report( out, getResources(), style, m, getApprovedLicenseNames() );
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
        catch ( RatReportFailedException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    private ILicenseFamily[] getApprovedLicenseNames() throws MojoExecutionException, MojoFailureException
    {
        if ( licenseFamilyNames == null || licenseFamilyNames.length == 0 )
        {
            return null;
        }
        ILicenseFamily[] results = new ILicenseFamily[licenseFamilyNames.length];
        for ( int i = 0; i < licenseFamilyNames.length; i++ )
        {
            LicenseFamilySpecification spec = licenseFamilyNames[i];
            ILicenseFamily licenseFamily = (ILicenseFamily) newInstance( ILicenseFamily.class, spec.getClassName() );
            results[i] = licenseFamily;
        }
        return results;
    }
}
