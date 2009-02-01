package org.apache.rat.anttasks;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;

import junit.framework.Assert;

public class ReportTest extends AbstractRatAntTaskTest {
	private static final File antFile = new File("src/test/resources/antunit/report-junit.xml").getAbsoluteFile();

	protected File getAntFile() {
		return antFile;
	}

	public void testWithReportSentToAnt() throws Exception {
		executeTarget("testWithReportSentToAnt");
		assertLogMatches("AL +\\Q" + getAntFile().getPath() + "\\E");
	}

	public void testWithReportSentToFile() throws Exception {
		final File reportFile = new File(getTempDir(), "selftest.report");
		getTempDir().mkdirs();
		final String alLine = "AL +\\Q" + getAntFile().getPath() + "\\E";
		if (reportFile.isFile()  &&  !reportFile.delete()) {
			throw new IOException("Unable to remove report file " + reportFile);
		}
		executeTarget("testWithReportSentToFile");
		assertLogDoesntMatch(alLine);
		Assert.assertTrue("Expected report file " + reportFile, reportFile.isFile());
		assertFileMatches(reportFile, alLine);
	}

	public void testWithASLUnknown() throws Exception {
		executeTarget("testWithASLUnknown");
		assertLogDoesntMatch("AL +\\Q" + getAntFile().getPath() + "\\E");
		assertLogMatches("\\!\\?\\?\\?\\?\\? +\\Q" + getAntFile().getPath() + "\\E");
	}

	public void testCustomMatcher() throws Exception {
		executeTarget("testCustomMatcher");
		assertLogDoesntMatch("AL +\\Q" + getAntFile().getPath() + "\\E");
		assertLogMatches("EXMPL +\\Q" + getAntFile().getPath() + "\\E");
	}

	public void testNoResources() throws Exception {
		try {
			executeTarget("testNoResources");
			fail("Expected Exception");
		} catch (BuildException e) {
			final String expect = "You must specify at least one file";
			assertTrue("Expected " + expect + ", got " + e.getMessage(),
					e.getMessage().indexOf(expect) != -1);
		}
	}

	public void testNoLicenseMatchers() throws Exception {
		try {
			executeTarget("testNoLicenseMatchers");
			fail("Expected Exception");
		} catch (BuildException e) {
			final String expect = "at least one license";
			assertTrue("Expected " + expect + ", got " + e.getMessage(),
					e.getMessage().indexOf(expect) != -1);
		}
	}
}
