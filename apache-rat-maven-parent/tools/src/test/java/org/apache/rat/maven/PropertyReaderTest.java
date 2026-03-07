package org.apache.rat.maven;

import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyReaderTest {
    @Test
    public void testReadProperties() throws IOException {
        Properties properties = PropertyReader.read("app.properties");
        assertThat(properties).isNotEmpty();
        assertThat(properties.getProperty("rat.version")).isNotEmpty();
        assertThat(properties.getProperty("rat.plugin.version")).isNotEmpty();
    }
}
