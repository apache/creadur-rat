package org.apache.rat.mp;

import static org.junit.Assert.*;

import org.junit.Test;

public class AbstractRatMojoTest {

	@Test
	public void testAmountOfExclusions() {
		assertEquals("Did you change the amount of eclipse excludes?", 3, AbstractRatMojo.ECLIPSE_DEFAULT_EXCLUDES.size());
		assertEquals("Did you change the amount of idea excludes?", 4, AbstractRatMojo.IDEA_DEFAULT_EXCLUDES.size());
		assertEquals("Did you change the amount of mvn excludes?", 4, AbstractRatMojo.MAVEN_DEFAULT_EXCLUDES.size());
	}

}
