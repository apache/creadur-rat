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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.site.decoration.Body;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.Skin;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.rat.Defaults;
import org.codehaus.doxia.sink.Sink;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Generates a report with Rat's output.
 */
@SuppressWarnings("deprecation") // MavenReport invokes the deprecated Sink implementation
@Mojo(name = "rat", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class RatReportMojo extends AbstractRatMojo implements MavenReport {
    public static final String DOT_HTML = ".html";
    @Component
    private Renderer siteRenderer;

    @Component
    private ArtifactFactory factory;

    @Component
    private ArtifactResolver resolver;

    /**
     * Specifies the directory where the report will be generated
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;

    /**
     * Returns the skins artifact file.
     *
     * @return Artifact 
     * @throws MojoFailureException   An error in the plugin configuration was detected.
     * @throws MojoExecutionException An error occurred while searching for the artifact file.
     */
    private Artifact getSkinArtifactFile() throws MojoFailureException, MojoExecutionException {
        final Skin skin = Skin.getDefaultSkin();

        String version = skin.getVersion();
        final Artifact artifact;
        try {
            if (version == null) {
                version = Artifact.RELEASE_VERSION;
            }
            VersionRange versionSpec = VersionRange.createFromVersionSpec(version);
            artifact =
                    factory.createDependencyArtifact(skin.getGroupId(), skin.getArtifactId(), versionSpec, "jar", null,
                            null);

            resolver.resolve(artifact, getProject().getRemoteArtifactRepositories(), localRepository);
        } catch (InvalidVersionSpecificationException e) {
            throw new MojoFailureException("The skin version '" + version + "' is not valid: " + e.getMessage());
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Unable to find skin", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoFailureException("The skin does not exist: " + e.getMessage());
        }

        return artifact;
    }

    /**
     * Called from Maven to invoke the plugin.
     *
     * @throws MojoFailureException   An error in the plugin configuration was detected.
     * @throws MojoExecutionException An error occurred while creating the report.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("RAT will not execute since it is configured to be skipped via system property 'rat.skip'.");
            return;
        }

        final DecorationModel model = new DecorationModel();
        model.setBody(new Body());
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("outputEncoding", "UTF-8");
        final Locale locale = Locale.getDefault();
        try {
            final SiteRenderingContext siteContext =
                    siteRenderer.createContextForSkin(getSkinArtifactFile(), attributes, model, getName(locale),
                            locale);
            final RenderingContext context = new RenderingContext(outputDirectory, getOutputName() + DOT_HTML);

            final SiteRendererSink sink = new SiteRendererSink(context);
            generate(sink, locale);

            if (!outputDirectory.mkdirs() && !outputDirectory.isDirectory()) {
                throw new IOException("Could not create output directory " + outputDirectory);
            }


            final Writer writer = new FileWriter(new File(outputDirectory, getOutputName() + DOT_HTML));

            siteRenderer.generateDocument(writer, sink, siteContext);


            siteRenderer.copyResources(siteContext, outputDirectory);
        } catch (IOException | RendererException | MavenReportException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Returns, whether the report may be generated.
     *
     * @return Always true.
     */
    public boolean canGenerateReport() {
        return true;
    }

    /**
     * Searches for a Rat artifact in the dependency list and returns its version.
     *
     * @return Version number, if found, or null.
     */
    private String getRatVersion() {
        //noinspection unchecked
        for (Artifact a : (Iterable<Artifact>) getProject().getDependencyArtifacts()) {
            if ("rat-lib".equals(a.getArtifactId())) {
                return a.getVersion();
            }
        }
        return null;
    }

    /**
     * Writes the report to the Doxia sink.
     *
     * @param sink   The doxia sink, kind of a SAX handler.
     * @param locale The locale to use for writing the report.
     * @throws MavenReportException Writing the report failed.
     */
    public void generate(Sink sink, Locale locale) throws MavenReportException {
        ResourceBundle bundle = getBundle(locale);
        final String title = bundle.getString("report.rat.title");
        sink.head();
        sink.title();
        sink.text(title);
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text(title);
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text(bundle.getString("report.rat.link") + " ");
        sink.link(bundle.getString("report.rat.url"));
        sink.text(bundle.getString("report.rat.fullName"));
        sink.link_();
        final String ratVersion = getRatVersion();
        if (ratVersion != null) {
            sink.text(" " + ratVersion);
        }
        sink.text(".");
        sink.paragraph_();

        sink.paragraph();
        sink.verbatim(true);
        try {
            sink.text(createReport(Defaults.getDefaultStyleSheet()));
        } catch (MojoExecutionException | MojoFailureException e) {
            throw new MavenReportException(e.getMessage(), e);
        }
        sink.verbatim_();
        sink.paragraph_();
        sink.section1_();
        sink.body_();
    }

    /**
     * Returns the reports category name.
     *
     * @return {@link MavenReport#CATEGORY_PROJECT_REPORTS}
     */
    public String getCategoryName() {
        return MavenReport.CATEGORY_PROJECT_REPORTS;
    }

    /**
     * Returns the reports bundle
     *
     * @param locale Requested locale of the bundle
     * @return The bundle, which is used to read localized strings.
     */
    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("org/apache/rat/mp/rat-report", locale, getClass().getClassLoader());
    }

    /**
     * Returns the reports description.
     *
     * @param locale Requested locale of the bundle
     * @return Report description, as given by the key "report.rat.description" in the bundle.
     */
    public String getDescription(Locale locale) {
        return getBundle(locale).getString("report.rat.description");
    }

    /**
     * Returns the reports name.
     *
     * @param locale Requested locale of the bundle
     * @return Report name, as given by the key "report.rat.name" in the bundle.
     */
    public String getName(Locale locale) {
        return getBundle(locale).getString("report.rat.name");
    }

    /**
     * Returns the reports file name.
     *
     * @return "rat-report"
     */
    public String getOutputName() {
        return "rat-report";
    }

    /**
     * Returns the reports output directory.
     *
     * @return Value of the "outputDirectory" parameter.
     */
    public File getReportOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns, whether this is an external report.
     *
     * @return Always false.
     */
    public boolean isExternalReport() {
        return false;
    }

    /**
     * Sets the reports output directory.
     *
     * @param pOutputDirectory Reports target directory.
     */
    public void setReportOutputDirectory(File pOutputDirectory) {
        outputDirectory = pOutputDirectory;
    }
}
