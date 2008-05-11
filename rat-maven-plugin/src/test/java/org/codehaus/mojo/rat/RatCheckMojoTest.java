package org.codehaus.mojo.rat;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;


/**
 * Test case for the {@link RatCheckMojo}.
 */
public class RatCheckMojoTest extends AbstractMojoTestCase
{
    /**
     * Creates a new instance of {@link Renderer}.
     * @return A configured instance of {@link DefaultRenderer}.
     * @throws Exception Creating the object failed.
     */
    private Renderer newSiteRenderer()
            throws Exception
    {
        return (Renderer) container.lookup( Renderer.ROLE, "default" );
    }

    /**
     * Creates a new instance of {@link ArtifactFactory}.
     * @return A configured instance of {@link DefaultArtifactFactory}.
     * @throws Exception Creating the object failed.
     */
    private ArtifactFactory newArtifactFactory()
            throws Exception
    {
        final InvocationHandler handler = new InvocationHandler(){
            public Object invoke( Object pProxy, Method pMethod, Object[] pArgs )
                throws Throwable
            {
                System.out.println("Invoking method " + pMethod);
                throw new IllegalStateException( "Not implemented" );
            }
        };
        return (ArtifactFactory)
            Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(),
                                    new Class[]{ ArtifactFactory.class },
                                    handler );
    }

    /**
     * Creates a new instance of {@link ArtifactResolver}.
     * @return A configured instance of {@link DefaultArtifactResolver}.
     * @throws Exception Creating the object failed.
     */
    private ArtifactResolver newArtifactResolver()
            throws Exception
    {
        final InvocationHandler handler = new InvocationHandler(){
            public Object invoke( Object pProxy, Method pMethod, Object[] pArgs )
                throws Throwable
            {
                System.out.println("Invoking method " + pMethod);
                throw new IllegalStateException( "Not implemented" );
            }
        };
        return (ArtifactResolver)
            Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(),
                                    new Class[]{ ArtifactResolver.class },
                                    handler );
    }

    /**
     * Creates an instance of {@link ArtifactRepository}.
     * @return A configured instance of {@link DefaultArtifactRepository}.
     * @throws Exception Creating the object failed.
     */
    private ArtifactRepository newArtifactRepository()
            throws Exception
    {
        File m2Dir = new File( System.getProperty( "user.home" ), ".m2" );
        File settingsFile = new File( m2Dir, "settings.xml" );
        String localRepo = null;
        if ( settingsFile.exists() )
        {
            Settings settings = new SettingsXpp3Reader().read( new FileReader( settingsFile ) );
            localRepo = settings.getLocalRepository();
        }
        if ( localRepo == null )
        {
            localRepo = System.getProperty( "user.home" ) + "/.m2/repository";
        }
        ArtifactRepositoryLayout repositoryLayout =
            (ArtifactRepositoryLayout) container.lookup(ArtifactRepositoryLayout.ROLE, "default" );
        return new DefaultArtifactRepository( "local", "file://" + localRepo, repositoryLayout );
    }

    /**
     * Creates a new instance of {@link RatCheckMojo}.
     * @param pDir The directory, where to look for a pom.xml file.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private RatCheckMojo newRatCheckMojo( String pDir )
            throws Exception
    {
        return (RatCheckMojo) newRatMojo( pDir, "check" );
    }

    /**
     * Creates a new instance of {@link AbstractRatMojo}.
     * @param pDir The directory, where to look for a pom.xml file.
     * @param pGoal The goal, which the Mojo must implement.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private AbstractRatMojo newRatMojo( String pDir, String pGoal )
            throws Exception
    {
        final File baseDir = new File( getBasedir() );
        final File testBaseDir = new File( new File( baseDir, "src/test" ), pDir );
        File testPom = new File( testBaseDir, "pom.xml" );
        AbstractRatMojo mojo = (AbstractRatMojo) lookupMojo( pGoal, testPom );
        assertNotNull( mojo );
        final File buildDirectory = new File( new File( baseDir, "target/test" ), pDir );
        setVariableValueToObject( mojo, "basedir", testBaseDir );
        setVariableValueToObject( mojo, "addDefaultLicenseMatchers", Boolean.TRUE );
        setVariableValueToObject( mojo, "useDefaultExcludes", Boolean.TRUE );
        setVariableValueToObject( mojo, "useMavenDefaultExcludes", Boolean.TRUE );
        setVariableValueToObject( mojo, "useEclipseDefaultExcludes", Boolean.TRUE );
        final Build build = new Build();
        build.setDirectory( buildDirectory.getPath() );
        final MavenProjectStub project = new MavenProjectStub(){
            public Build getBuild()
            {
                return build;
            }
        };
        setVariableValueToObject( mojo, "project", project );
        if (mojo instanceof RatReportMojo)
        {
            setVariableValueToObject( mojo, "localRepository", newArtifactRepository() );
            setVariableValueToObject( mojo, "resolver", newArtifactResolver() );
            setVariableValueToObject( mojo, "factory", newArtifactFactory() );
            setVariableValueToObject( mojo, "siteRenderer", newSiteRenderer() );
        }
        else if ( mojo instanceof RatCheckMojo )
        {
            final File ratTxtFile = new File( buildDirectory, "rat.txt" );
            setVariableValueToObject( mojo, "reportFile", ratTxtFile );
        }
        return mojo;
    }

    /**
     * Creates a new instance of {@link RatReportMojo}.
     * @param pDir The directory where to read the POM file.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private RatReportMojo newRatReportMojo( String pDir )
            throws Exception
    {
        return (RatReportMojo) newRatMojo( pDir, "rat" );
    }

    /**
     * Reads the location of the rat text file from the Mojo.
     * @param pMojo The configured Mojo.
     * @return Value of the "reportFile" property.
     * @throws Exception An error occurred while reading the property.
     */
    private File getRatTxtFile( RatCheckMojo pMojo )
            throws Exception
    {
        return (File) getVariableValueFromObject( pMojo, "reportFile" );
    }

    /**
     * Runs a check, which should expose no problems.
     * @throws Exception The test failed.
     */
    public void testIt1() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo( "it1" );
        final File ratTxtFile = getRatTxtFile(mojo);
        mojo.execute();
        checkResult( ratTxtFile, 1, 0 );
    }

    /**
     * Reads the created report file and verifies, whether the detected numbers
     * are matching.
     * @param pRatTxtFile The file to read.
     * @param pNumAslFiles The number of files with ASL.
     * @param pNumNoLicenseFiles The number of files without license.
     * @throws IOException An error occurred while reading the file.
     */
    private void checkResult( File pRatTxtFile, int pNumAslFiles, int pNumNoLicenseFiles )
            throws IOException
    {
        assertTrue( pRatTxtFile.exists() );
        BufferedReader reader = new BufferedReader( new FileReader( pRatTxtFile ) );
        Integer numAslFiles = null;
        Integer numNoLicenseFiles = null;
        for (;;)
        {
            String line = reader.readLine();
            if ( line == null )
            {
                break;
            }
            int offset = line.indexOf( "Apache Licensed: " );
            if ( offset >= 0 )
            {
                numAslFiles = new Integer( line.substring( offset + "Apache Licensed: ".length() ).trim() );
            }
            offset = line.indexOf( "Unknown Licenses" );
            if ( offset >= 0 )
            {
                numNoLicenseFiles = new Integer( line.substring( 0, offset ).trim() );
            }
        }
        reader.close();
        assertEquals( new Integer( pNumAslFiles), numAslFiles );
        assertEquals( new Integer( pNumNoLicenseFiles ), numNoLicenseFiles );
    }

    /**
     * Runs a check, which should detect a problem.
     * @throws Exception The test failed.
     */
    public void testIt2() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo( "it2" );
        final File ratTxtFile = getRatTxtFile(mojo);
        try
        {
            mojo.execute();
            fail( "Expected RatCheckException" );
        }
        catch ( RatCheckException e )
        {
            // Ok
        }
        checkResult( ratTxtFile, 1, 1 );
    }

//    /**
//     * Runs a report, which should detect no problems.
//     * @throws Exception The test failed.
//     */
//    public void testIt3() throws Exception {
//        final RatReportMojo mojo = newRatReportMojo( "it3" );
//        mojo.execute();
//    }
}
