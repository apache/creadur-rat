package org.apache.rat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationExceptionTest {

    @Test
    void fromTest() {
        RuntimeException runTime = new RuntimeException();
        assertThat(ConfigurationException.from(runTime))
                .isInstanceOf(ConfigurationException.class)
                .hasCause(runTime);

        Exception ex = new Exception();
        assertThat(ConfigurationException.from(ex))
                .isInstanceOf(ConfigurationException.class)
                .hasCause(ex);

        ex = new ConfigurationException("yee haw");
        assertThat(ConfigurationException.from(ex))
                .isInstanceOf(ConfigurationException.class)
                .isEqualTo(ex);
    }
}
