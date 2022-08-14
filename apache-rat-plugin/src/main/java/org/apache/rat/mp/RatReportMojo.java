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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.doxia.tools.SiteToolException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.MavenMultiPageReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.utils.WriterFactory;
import org.apache.rat.Defaults;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;


/**
 * Generates a report with Rat's output.
 */
@Mojo(name = "rat", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class RatReportMojo extends AbstractRatMojo implements MavenMultiPageReport {

    /**
     * The output directory for the report. Note that this parameter is only evaluated if the goal is run directly from
     * the command line. If the goal is run indirectly as part of a site generation, the output directory configured in
     * the Maven Site Plugin is used instead.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}", readonly = true, required = true)
    protected File outputDirectory;

    /**
     * Specifies the input encoding.
     */
    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true)
    private String inputEncoding;

    /**
     * Specifies the output encoding.
     */
    @Parameter(property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}", readonly = true)
    private String outputEncoding;

    /**
     * The local repository.
     */
    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    protected ArtifactRepository localRepository;

    /**
     * Remote repositories used for the project.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    protected List<ArtifactRepository> remoteRepositories;

    /**
     * SiteTool.
     */
    @Component
    protected SiteTool siteTool;

    /**
     * Doxia Site Renderer component.
     */
    @Component
    protected Renderer siteRenderer;

    /**
     * The current sink to use
     */
    private Sink sink;

    /**
     * The sink factory to use
     */
    private SinkFactory sinkFactory;

    /**
     * The current report output directory to use
     */
    private File reportOutputDirectory;


    /**
     * This method is called when the report generation is invoked directly as a standalone Mojo.
     *
     * @throws MojoExecutionException if an error occurs when generating the report
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute()
            throws MojoExecutionException {
        if (!canGenerateReport()) {
            return;
        }

        File outputDirectory = new File(getOutputDirectory());

        String filename = getOutputName() + ".html";

        Locale locale = Locale.getDefault();

        try {
            SiteRenderingContext siteContext = createSiteRenderingContext(locale);

            // copy resources
            getSiteRenderer().copyResources(siteContext, outputDirectory);

            // TODO Replace null with real value
            RenderingContext docRenderingContext = new RenderingContext(outputDirectory, filename, null);

            SiteRendererSink sink = new SiteRendererSink(docRenderingContext);

            generate(sink, null, locale);

            // MSHARED-204: only render Doxia sink if not an external report
            if (!isExternalReport()) {
                outputDirectory.mkdirs();

                try (Writer writer =
                             new OutputStreamWriter(Files.newOutputStream(new File(outputDirectory, filename).toPath()),
                                     getOutputEncoding())) {
                    // render report
                    getSiteRenderer().mergeDocumentIntoSite(writer, sink, siteContext);
                }
            }

            // copy generated resources also
            getSiteRenderer().copyResources(siteContext, outputDirectory);
        } catch (RendererException | IOException | MavenReportException e) {
            throw new MojoExecutionException(
                    "An error has occurred in " + getName(Locale.ENGLISH) + " report generation.", e);
        }
    }

    private SiteRenderingContext createSiteRenderingContext(Locale locale)
            throws MavenReportException, IOException {
        DecorationModel decorationModel = new DecorationModel();

        Map<String, Object> templateProperties = new HashMap<>();
        // We tell the skin that we are rendering in standalone mode
        templateProperties.put("standalone", Boolean.TRUE);
        templateProperties.put("project", getProject());
        templateProperties.put("inputEncoding", getInputEncoding());
        templateProperties.put("outputEncoding", getOutputEncoding());
        // Put any of the properties in directly into the Velocity context
        for (Map.Entry<Object, Object> entry : getProject().getProperties().entrySet()) {
            templateProperties.put((String) entry.getKey(), entry.getValue());
        }

        SiteRenderingContext context;
        try {
            Artifact skinArtifact =
                    siteTool.getSkinArtifactFromRepository(localRepository, remoteRepositories, decorationModel);

            getLog().info(buffer().a("Rendering content with ").strong(skinArtifact.getId()
                    + " skin").a('.').toString());

            context = siteRenderer.createContextForSkin(skinArtifact, templateProperties, decorationModel,
                    project.getName(), locale);
        } catch (SiteToolException e) {
            throw new MavenReportException("Failed to retrieve skin artifact", e);
        } catch (RendererException e) {
            throw new MavenReportException("Failed to create context for skin", e);
        }

        // Generate static site
        context.setRootDirectory(project.getBasedir());

        return context;
    }

    /**
     * Generate a report.
     *
     * @param sink   the sink to use for the generation.
     * @param locale the wanted locale to generate the report, could be null.
     * @throws MavenReportException if any
     * @deprecated use {@link #generate(Sink, SinkFactory, Locale)} instead.
     */
    @Deprecated
    @Override
    public void generate(org.codehaus.doxia.sink.Sink sink, Locale locale)
            throws MavenReportException {
        generate(sink, null, locale);
    }

    /**
     * Generate a report.
     *
     * @param sink
     * @param locale
     * @throws MavenReportException
     * @deprecated use {@link #generate(Sink, SinkFactory, Locale)} instead.
     */
    @Deprecated
    public void generate(Sink sink, Locale locale)
            throws MavenReportException {
        generate(sink, null, locale);
    }

    /**
     * This method is called when the report generation is invoked by maven-site-plugin.
     *
     * @param sink
     * @param sinkFactory
     * @param locale
     * @throws MavenReportException
     */
    @Override
    public void generate(Sink sink, SinkFactory sinkFactory, Locale locale)
            throws MavenReportException {
        if (!canGenerateReport()) {
            getLog().info("This report cannot be generated as part of the current build. "
                    + "The report name should be referenced in this line of output.");
            return;
        }

        this.sink = sink;

        this.sinkFactory = sinkFactory;

        executeReport(locale);

        closeReport();
    }

    /**
     * @return CATEGORY_PROJECT_REPORTS
     */
    @Override
    public String getCategoryName() {
        return CATEGORY_PROJECT_REPORTS;
    }

    @Override
    public File getReportOutputDirectory() {
        if (reportOutputDirectory == null) {
            reportOutputDirectory = new File(getOutputDirectory());
        }

        return reportOutputDirectory;
    }

    @Override
    public void setReportOutputDirectory(File reportOutputDirectory) {
        this.reportOutputDirectory = reportOutputDirectory;
        this.outputDirectory = reportOutputDirectory;
    }

    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }

    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }

    /**
     * Gets the input files encoding.
     *
     * @return The input files encoding, never <code>null</code>.
     */
    protected String getInputEncoding() {
        return (inputEncoding == null) ? ReaderFactory.FILE_ENCODING : inputEncoding;
    }

    /**
     * Gets the effective reporting output files encoding.
     *
     * @return The effective reporting output file encoding, never <code>null</code>.
     */
    protected String getOutputEncoding() {
        return (outputEncoding == null) ? WriterFactory.UTF_8 : outputEncoding;
    }

    /**
     * Actions when closing the report.
     */
    protected void closeReport() {
        getSink().close();
    }

    /**
     * @return the sink used
     */
    public Sink getSink() {
        return sink;
    }

    /**
     * @return the sink factory used
     */
    public SinkFactory getSinkFactory() {
        return sinkFactory;
    }

    /**
     * @return {@code false} by default.
     * @see org.apache.maven.reporting.MavenReport#isExternalReport()
     */
    @Override
    public boolean isExternalReport() {
        return false;
    }

    @Override
    public boolean canGenerateReport() {
        return !skip;
    }

    /**
     * Searches for a Rat artifact in the dependency list and returns its version.
     *
     * @return Version number, if found, or null.
     */
    // TODO The canonical way is to read the pom.properties for the artifact desired in META-INF/maven
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
     * @param locale The locale to use for writing the report.
     * @throws MavenReportException Writing the report failed.
     */
    protected void executeReport(Locale locale) throws MavenReportException {
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
        sink.verbatim(SinkEventAttributeSet.BOXED);
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
}
