package org.apache.rat.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenOptionCollectionTest {

    @Test
    void deprecatedOptionTest() {
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

    @ParameterizedTest
    @MethodSource("namingTestData")
    public void namingTest(String expectedName, Option option) {
        MavenOptionCollection collection = new MavenOptionCollection();
        MavenOption mavenOption = collection.getMappedOption(option);
        assertThat(mavenOption.getName()).isEqualTo(expectedName);
    }

    static Stream<Arguments> namingTestData() {
        List<Arguments> lst  = new ArrayList<>();
        Option option = Option.builder().longOpt("multiple-args").hasArgs().build();
        lst.add(Arguments.of("multipleArgs", option));
        option = Option.builder().longOpt("multiple-arg-single-name").hasArgs().build();
        lst.add(Arguments.of("multipleArgSingleNames", option));
        option = Option.builder().longOpt("multiple-args-approved").hasArgs().build();
        lst.add(Arguments.of("multipleArgsApproved", option));
        option = Option.builder().longOpt("multiple-args-denied").hasArgs().build();
        lst.add(Arguments.of("multipleArgsDenied", option));

        option = Option.builder().longOpt("single-args").hasArg().build();
        lst.add(Arguments.of("singleArgs", option));
        option = Option.builder().longOpt("single-arg-single-name").hasArg().build();
        lst.add(Arguments.of("singleArgSingleName", option));
        option = Option.builder().longOpt("single-arg-approved").hasArg().build();
        lst.add(Arguments.of("singleArgApproved", option));
        option = Option.builder().longOpt("single-args-denied").hasArg().build();
        lst.add(Arguments.of("singleArgsDenied", option));

        option = Option.builder().longOpt("no-args").build();
        lst.add(Arguments.of("noArgs", option));
        option = Option.builder().longOpt("no-arg-single-name").build();
        lst.add(Arguments.of("noArgSingleName", option));
        option = Option.builder().longOpt("no-args-approved").build();
        lst.add(Arguments.of("noArgsApproved", option));
        option = Option.builder().longOpt("no-args-denied").build();
        lst.add(Arguments.of("noArgsDenied", option));

        return lst.stream();
    }
}
