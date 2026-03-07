package org.apache.rat.maven.tools;

import java.nio.file.Path;
import java.util.Map;
import org.apache.rat.commandline.Arg;
import org.apache.rat.maven.MavenOption;
import org.apache.rat.maven.MavenOptionCollection;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CodeGeneratorTest {
    @TempDir
    private Path testPath;
    private CodeGenerator codeGenerator;

    CodeGeneratorTest() {}

    @BeforeEach
    void setup() {
        codeGenerator = new CodeGenerator(testPath.toString());
    }

    @Test
    public void testGenerateMethods() throws Exception {
        MavenOptionCollection mavenOptionCollection = new MavenOptionCollection();
        Map<MavenOption, String> methods = codeGenerator.gatherMethods();
        MavenOption mavenOption = mavenOptionCollection.getMappedOption(Arg.EXCLUDE.option());
        String methodText = methods.get(mavenOption);
        TextUtils.assertContains("public void setInputExcludes(final String[] inputExcludes) {", methodText);

        mavenOption = mavenOptionCollection.getMappedOption(Arg.FAMILIES_APPROVED.option());
        methodText = methods.get(mavenOption);
        TextUtils.assertContains("public void setLicenseFamiliesApproved(final String licenseFamiliesApproved) {", methodText);
        TextUtils.assertContainsExactly(1, "public void set", methodText);
    }
}
