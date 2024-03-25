package org.apache.rat.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.testhelpers.TestingMatcher;
import org.junit.jupiter.api.Test;

public class SimpleLicenseTest {

    @Test
    public void descriptionTest() {
        SimpleLicense lic = new SimpleLicense(
                ILicenseFamily.builder().setLicenseFamilyCategory("familyId")
                        .setLicenseFamilyName("TestingLicense: familyId").build(),
                new TestingMatcher(), "These are the notes", "My testing license", "TestingId");
        Description underTest = lic.getDescription();
        assertEquals(Component.Type.License, underTest.getType());
        assertEquals("My testing license", underTest.getCommonName());
        assertEquals("", underTest.getDescription());
        assertNull(underTest.getParamValue());
        Map<String, Description> children = underTest.getChildren();
        assertTrue(children.containsKey("id"));
        assertTrue(children.containsKey("name"));
        assertTrue(children.containsKey("notes"));
        assertTrue(children.containsKey("TestingMatcher"));
    }
}
