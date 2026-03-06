package org.apache.rat.maven;

import java.util.Collection;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenOptionTest {

    @Test
    void deprecatedTest() {
        final MavenOptionCollection  mavenOptionCollection = new MavenOptionCollection();
        MavenOption opt = mavenOptionCollection.getMappedOption(Arg.EDIT_COPYRIGHT.find("copyright"));

        assertThat(opt.getDeprecated()).contains("<editCopyright>");

        Option deprecatedOption = Option.builder().longOpt("dep-opt")
                .deprecated(DeprecatedAttributes.builder()
                        .setDescription("Yep it is deprecated use --other-opt instead")
                        .setForRemoval(true)
                        .setSince("1.0")
                        .get())
                .desc("This is the --dep-opt description that talks about --other-opt.")
                .build();


        try {
            mavenOptionCollection.additionalOptions().addOption(deprecatedOption);
            Option otherOpt = Option.builder().longOpt("other-opt")
                    .desc("this is the --other-opt description.").build();
            mavenOptionCollection.additionalOptions().addOption(otherOpt);

            Collection<Option> opts = mavenOptionCollection.getOptions().getOptions();
            assertThat(opts).contains(otherOpt);
            assertThat(opts).contains(deprecatedOption);

            MavenOption depMaven = mavenOptionCollection.getMappedOption(deprecatedOption);
            MavenOption otherMaven = mavenOptionCollection.getMappedOption(otherOpt);

            assertThat(depMaven.getDeprecated()).contains("<otherOpt>");
            assertThat(depMaven.getMethodName()).isEqualTo("setDepOpt");
            assertThat(depMaven.getDescription()).isEqualTo("This is the <depOpt> description that talks about <otherOpt>.");
            assertThat(depMaven.getName()).isEqualTo("depOpt");
        } finally {
            assertThat(new MavenOptionCollection().additionalOptions().getOptions()).isEmpty();
        }

    }
}
