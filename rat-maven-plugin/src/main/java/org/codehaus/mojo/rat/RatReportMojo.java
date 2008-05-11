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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.site.decoration.Body;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.Skin;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

import rat.Defaults;

/**
 * Generates a report with RAT's output.
 * 
 * @goal rat
 * @requiresDependencyResolution test
 */
public class RatReportMojo extends AbstractRatMojo implements MavenReport
{
    /**
     * Specifies the directory where the report will be generated
     * 
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @component
     */
    private Renderer siteRenderer;

    /**
     * @component
     */
    private ArtifactFactory factory;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Returns the skins artifact file.
     * 
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             An error occurred while searching for the artifact file.
     * @return Artifact file
     */
    private File getSkinArtifactFile() throws MojoFailureException, MojoExecutionException
    {
        Skin skin = Skin.getDefaultSkin();

        String version = skin.getVersion();
        Artifact artifact;
        try
        {
            if ( version == null )
            {
                version = Artifact.RELEASE_VERSION;
            }
            VersionRange versionSpec = VersionRange.createFromVersionSpec( version );
            artifact =
                factory.createDependencyArtifact( skin.getGroupId(), skin.getArtifactId(), versionSpec, "jar", null,
                                                  null );

            resolver.resolve( artifact, getProject().getRemoteArtifactRepositories(), localRepository );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new MojoFailureException( "The skin version '" + version + "' is not valid: " + e.getMessage() );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Unable to find skin", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoFailureException( "The skin does not exist: " + e.getMessage() );
        }

        return artifact.getFile();
    }

    /**
     * Creates the report as a string. Currently, this string will be embedded verbatimly into the report document.
     * 
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             An error occurred while creating the report.
     * @return Report contents
     */
    private String createReport() throws MojoExecutionException, MojoFailureException
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter( sw );
            createReport( new PrintWriter( sw ), Defaults.getDefaultStyleSheet() );
            final String result = sw.toString();
            pw.close();
            pw = null;
            sw.close();
            sw = null;
            return result;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            if ( pw != null )
            {
                try
                {
                    pw.close();
                }
                catch ( Throwable t )
                {
                    // Ignore me
                }
            }
            if ( sw != null )
            {
                try
                {
                    sw.close();
                }
                catch ( Throwable t )
                {
                    // Ignore me
                }
            }
        }
    }

    /**
     * Called from Maven to invoke the plugin.
     * 
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             An error occurred while creating the report.
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        DecorationModel model = new DecorationModel();
        model.setBody( new Body() );
        Map attributes = new HashMap();
        attributes.put( "outputEncoding", "UTF-8" );
        Locale locale = Locale.getDefault();
        try
        {
            SiteRenderingContext siteContext =
                siteRenderer.createContextForSkin( getSkinArtifactFile(), attributes, model, getName( locale ),
                                                   locale );
            RenderingContext context = new RenderingContext( outputDirectory, getOutputName() + ".html" );

            SiteRendererSink sink = new SiteRendererSink( context );
            generate( sink, locale );

            outputDirectory.mkdirs();

            Writer writer = new FileWriter( new File( outputDirectory, getOutputName() + ".html" ) );

            siteRenderer.generateDocument( writer, sink, siteContext );

            siteRenderer.copyResources( siteContext, new File( getProject().getBasedir(), "src/site/resources" ),
                                        outputDirectory );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( MavenReportException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( RendererException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * Returns, whether the report may be generated.
     * 
     * @return Always true.
     */
    public boolean canGenerateReport()
    {
        return true;
    }

    /**
     * Searches for a RAT artifact in the dependency list and returns its version.
     * 
     * @return Version number, if found, or null.
     */
    private String getRatVersion()
    {
        for ( Iterator iter = getProject().getDependencyArtifacts().iterator(); iter.hasNext(); )
        {
            Artifact a = (Artifact) iter.next();
            if ( "rat-lib".equals( a.getArtifactId() ) )
            {
                return a.getVersion();
            }
        }
        return null;
    }

    /**
     * Writes the report to the Doxia sink.
     * 
     * @param sink
     *            The doxia sink, kind of a SAX handler.
     * @param locale
     *            The locale to use for writing the report.
     * @throws MavenReportException
     *             Writing the report failed.
     */
    public void generate( Sink sink, Locale locale ) throws MavenReportException
    {
        ResourceBundle bundle = getBundle( locale );
        final String title = bundle.getString( "report.rat.title" );
        sink.head();
        sink.title();
        sink.text( title );
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text( title );
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text( bundle.getString( "report.rat.link" ) + " " );
        sink.link( bundle.getString( "report.rat.url" ) );
        sink.text( bundle.getString( "report.rat.fullName" ) );
        sink.link_();
        final String ratVersion = getRatVersion();
        if ( ratVersion != null )
        {
            sink.text( " " + ratVersion );
        }
        sink.text( "." );
        sink.paragraph_();

        sink.paragraph();
        sink.verbatim( true );
        try
        {
            sink.text( createReport() );
        }
        catch ( MojoExecutionException e )
        {
            throw new MavenReportException( e.getMessage(), e );
        }
        catch ( MojoFailureException e )
        {
            throw new MavenReportException( e.getMessage(), e );
        }
        sink.verbatim_();
        sink.paragraph_();
        sink.body_();
    }

    /**
     * Returns the reports category name.
     * 
     * @return {@link MavenReport#CATEGORY_PROJECT_REPORTS}
     */
    public String getCategoryName()
    {
        return MavenReport.CATEGORY_PROJECT_REPORTS;
    }

    /**
     * Returns the reports bundle
     * 
     * @param locale
     *            Requested locale of the bundle
     * @return The bundle, which is used to read localized strings.
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "org/codehaus/mojo/rat/rat-report", locale, getClass().getClassLoader() );
    }

    /**
     * Returns the reports description.
     * 
     * @param locale
     *            Requested locale of the bundle
     * @return Report description, as given by the key "report.rat.description" in the bundle.
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.rat.description" );
    }

    /**
     * Returns the reports name.
     * 
     * @param locale
     *            Requested locale of the bundle
     * @return Report name, as given by the key "report.rat.name" in the bundle.
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.rat.name" );
    }

    /**
     * Returns the reports file name.
     * 
     * @return "rat-report"
     */
    public String getOutputName()
    {
        return "rat-report";
    }

    /**
     * Returns the reports output directory.
     * 
     * @return Value of the "outputDirectory" parameter.
     */
    public File getReportOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Returns, whether this is an external report.
     * 
     * @return Always false.
     */
    public boolean isExternalReport()
    {
        return false;
    }

    /**
     * Sets the reports output directory.
     * 
     * @param pOutputDirectory
     *            Reports target directory.
     */
    public void setReportOutputDirectory( File pOutputDirectory )
    {
        outputDirectory = pOutputDirectory;
    }
}
