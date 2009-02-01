package org.apache.rat.anttasks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;

public abstract class AbstractRatAntTaskTest extends BuildFileTest {
	private static final File tempDir = new File("target/anttasks");

	protected abstract File getAntFile();

	protected File getTempDir() {
		return tempDir;
	}

	public void setUp() {
		configureProject(getAntFile().getPath());
	}

	protected void assertLogDoesntMatch(String pPattern) {
		final String log = super.getLog();
		Assert.assertFalse("Log matches the pattern: " + pPattern + ", got " + log,
				isMatching(pPattern, log));
	}

	protected void assertLogMatches(String pPattern) {
		final String log = super.getLog();
		Assert.assertTrue("Log doesn' match string: " + pPattern + ", got " + log,
				isMatching(pPattern, log));
	}

	private boolean isMatching(final String pPattern, final String pValue) {
		return Pattern.compile(pPattern).matcher(pValue).find();
	}

	private String load(File pFile) throws IOException {
		FileReader fr = new FileReader(pFile);
		try {
			final StringBuffer sb = new StringBuffer();
			char[] buffer = new char[1024];
			for (;;) {
				int res = fr.read(buffer);
				if (res == -1) {
					fr.close();
					fr = null;
					return sb.toString();
				}
				if (res > 0) {
					sb.append(buffer, 0, res);
				}
			}
		} finally {
			FileUtils.close(fr);
		}
	}

	protected void assertFileMatches(File pFile, String pPattern)
			throws IOException {
		final String content = load(pFile);
		Assert.assertTrue("File " + pFile
				+ " doesn't match the pattern " + pPattern
				+ ", got " + content,
				isMatching(pPattern, content));
	}
}
