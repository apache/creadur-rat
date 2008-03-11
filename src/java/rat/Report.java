/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Writer;

import javax.xml.transform.TransformerConfigurationException;

import rat.analysis.IHeaderMatcher;
import rat.license.ILicenseFamily;
import rat.report.IReportable;
import rat.report.RatReport;
import rat.report.RatReportFailedException;
import rat.report.xml.XmlReportFactory;
import rat.report.xml.writer.IXmlWriter;
import rat.report.xml.writer.impl.base.XmlWriter;

public class Report {

	public static final void main(String args[]) throws Exception {
		if (args == null || args.length != 1) {
			printUsage();
		} else {
			Report report = new Report(args[0]);
			//report.report(System.out);
            report.styleReport(System.out);
		} 		
	}
	
	private static final void printUsage() {
		System.out.println("Usage: <directory>");
		System.out.println("       where ");
        System.out.println(           "<directory> is the base directory to be audited.");
        System.out.println("NOTE: RAT is really little more than a grep ATM");
		System.out.println("      RAT is also rather memory hungry ATM");
		System.out.println("      RAT is very basic ATM");
        System.out.println("      RAT ATM runs on unpacked releases");
        System.out.println("      RAT highlights possible issues");
        System.out.println("      RAT reports require intepretation");
        System.out.println("      RAT often requires some tuning before it runs well against a project");
        System.out.println("      RAT relies on heuristics: it may miss issues");
	}
	
	private final String baseDirectory;
	
	private Report(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	
	public void report(PrintStream out) throws Exception {
        DirectoryWalker base = getDirectory(out);
        if (base != null) {
            report(base, new OutputStreamWriter(out), Defaults.createDefaultMatcher(), null);
        }
	}
    
    private DirectoryWalker getDirectory(PrintStream out) {
        DirectoryWalker result = null;
        File base = new File(baseDirectory);
        if (!base.exists()) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" does not exist.\n");
        } else if (!base.isDirectory()) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" must be a directory.\n");
        } else {
            result = new DirectoryWalker(base);
        }
        return result;
    }
    
    public void styleReport(PrintStream out) throws Exception {
        DirectoryWalker base = getDirectory(out);
        if (base != null) {
            InputStream style = Defaults.getDefaultStyleSheet();
            report(out, base, style, Defaults.createDefaultMatcher(), null);
        }
    }

    public static void report(PrintStream out, IReportable base, final InputStream style, final IHeaderMatcher matcher,
            final ILicenseFamily[] approvedLicenseNames) 
           throws IOException, TransformerConfigurationException, 
           InterruptedException, RatReportFailedException {
        report(new OutputStreamWriter(out), base, style, matcher, approvedLicenseNames);
    }

    public static void report(Writer out, IReportable base, final InputStream style, 
            final IHeaderMatcher matcher, final ILicenseFamily[] approvedLicenseNames) 
                throws IOException, TransformerConfigurationException, FileNotFoundException, InterruptedException, RatReportFailedException {
        PipedReader reader = new PipedReader();
        PipedWriter writer = new PipedWriter(reader);
        ReportTransformer transformer = new ReportTransformer(out, style, reader);
        Thread transformerThread = new Thread(transformer);
        transformerThread.start();
        report(base, writer, matcher, approvedLicenseNames);
        writer.flush();
        writer.close();
        transformerThread.join();
    }
    
    
    public static void report(final IReportable container, final Writer out, final IHeaderMatcher matcher,
             final ILicenseFamily[] approvedLicenseNames) throws IOException, RatReportFailedException {
        IXmlWriter writer = new XmlWriter(out);
        RatReport report = XmlReportFactory.createStandardReport(writer, matcher, approvedLicenseNames);  
        report.startReport();
        container.run(report);
        report.endReport();
        writer.closeDocument();
    }
}
