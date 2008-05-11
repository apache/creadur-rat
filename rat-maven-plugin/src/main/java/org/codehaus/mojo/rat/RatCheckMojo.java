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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import rat.Defaults;

/**
 * Run RAT to perform a violation check.
 * 
 * @goal check
 * @phase verify
 */
public class RatCheckMojo extends AbstractRatMojo
{
    /**
     * Where to store the report.
     * 
     * @parameter expression="${rat.outputFile}" default-value="${project.build.directory}/rat.txt"
     */
    private File reportFile;

    /**
     * Maximum number of files with unapproved licenses.
     * @parameter expression="${rat.numUnapprovedLicenses}" default-value="0"
     */
    private int numUnapprovedLicenses;
    
    private URL getStylesheetURL()
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource( "org/codehaus/mojo/rat/identity.xsl" );
        if ( url == null )
        {
            throw new IllegalStateException( "Failed to locate stylesheet" );
        }
        return url;
    }

    private Document getRawReport()
        throws MojoExecutionException, MojoFailureException
    {
        URL url = getStylesheetURL();
        InputStream style = null;
        try
        {
            style = url.openStream();
            final StringWriter sw = new StringWriter();
            createReport( new PrintWriter( sw ), style );
            style.close();
            style = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware( true );
            dbf.setValidating( false );
            return dbf.newDocumentBuilder().parse( new InputSource( new StringReader( sw.toString() ) ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( SAXException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( ParserConfigurationException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            if ( style != null )
            {
                try
                {
                    style.close();
                }
                catch ( Throwable t )
                {
                    /* Ignore me */
                }
            }
        }
    }

    /**
     * Invoked by Maven to execute the Mojo.
     * 
     * @throws MojoFailureException
     *             An error in the plugin configuration was detected.
     * @throws MojoExecutionException
     *             Another error occurred while executing the plugin.
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File parent = reportFile.getParentFile();
        parent.mkdirs();

        final Document report = getRawReport();
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( reportFile );
            Transformer t = TransformerFactory.newInstance().newTransformer( new StreamSource( Defaults.getDefaultStyleSheet() ) );
            t.transform( new DOMSource( report ), new StreamResult( fos ) );
            fos.close();
            fos = null;
        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( "Failed to create file " + reportFile + ": " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create file " + reportFile + ": " + e.getMessage(), e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( Throwable t )
                {
                    // Ignore me
                }
            }
        }

        check( report );
    }

    
    private void count( RatStatistics pStatistics, Node node )
    {
        for ( Node child = node.getFirstChild();  child != null;  child = child.getNextSibling() )
        {
            switch ( child.getNodeType() )
            {
                case Node.ELEMENT_NODE:
                    final Element e = (Element) child;
                    final String uri = e.getNamespaceURI();
                    if ( uri == null  ||  uri.length() == 0 )
                    {
                        final String localName = e.getLocalName();
                        if ( "license-approval".equals( localName ) )
                        {
                            if ( Boolean.valueOf( e.getAttribute( "name" ) ).booleanValue() )
                            {
                                pStatistics.setNumApprovedLicenses( pStatistics.getNumApprovedLicenses() + 1 );
                            }
                            else
                            {
                                pStatistics.setNumUnapprovedLicenses( pStatistics.getNumUnapprovedLicenses() + 1 );
                            }
                        }
                    }
                    count( pStatistics, child );
                    break;
                case Node.DOCUMENT_FRAGMENT_NODE:
                    count( pStatistics, child );
                    break;
                default:
                    break;
            }
        }
    }

    protected void check( Document document )
        throws MojoFailureException
    {
        RatStatistics statistics = new RatStatistics();
        count( statistics, document );
        check( statistics );
    }

    protected void check( RatStatistics statistics )
        throws MojoFailureException
    {
        if ( numUnapprovedLicenses < statistics.getNumUnapprovedLicenses() )
        {
            throw new RatCheckException( "Too many unapproved licenses: " + statistics.getNumApprovedLicenses() );
        }
    }
}
