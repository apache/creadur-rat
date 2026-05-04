package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOptionCollection;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CLIOptionTest {
    final CLIOptionCollection optionCollection = CLIOptionCollection.INSTANCE;
    final CLIOption optionA = new CLIOption(CLIOptionCollection.INSTANCE, new Option("a", false, "short key"));
    final CLIOption optionB = new CLIOption(CLIOptionCollection.INSTANCE, Option.builder("b").longOpt("bee").hasArg().desc("two key").build());
    final CLIOption optionC = new CLIOption(CLIOptionCollection.INSTANCE, Option.builder().longOpt("sea").hasArgs().type(File.class).desc("long key").build());
    final CLIOption optionD = new CLIOption(CLIOptionCollection.INSTANCE, Option.builder().longOpt("dee").hasArgs().argName("dede").desc("long key").build());

    @Test
    void getText() {
        assertThat(optionA.getText()).isEqualTo("-a");
        assertThat(optionB.getText()).isEqualTo("--bee or -b");
        assertThat(optionC.getText()).isEqualTo("--sea");
    }

    @Test
    void cleanupName() {
        assertThat(optionA.cleanupName(optionA.getOption())).isEqualTo("a");
        assertThat(optionA.cleanupName(optionB.getOption())).isEqualTo("bee");
        assertThat(optionA.cleanupName(optionC.getOption())).isEqualTo("sea");
    }

    @Test
    void getExample() {
        assertThat(optionA.getExample()).isEqualTo("-a");
        assertThat(optionB.getExample()).isEqualTo("--bee Arg");
        assertThat(optionC.getExample()).isEqualTo("--sea Arg [Arg2 [Arg3 [...]]] --");
        assertThat(optionD.getExample()).isEqualTo("--dee dede [dede2 [dede3 [...]]] --");
    }
}
