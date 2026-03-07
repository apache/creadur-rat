package org.apache.rat.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyReader {
    private PropertyReader() {
        // do not instantiate
    }
    public static Properties read(final String propertyFileName) throws IOException {
        try (InputStream is = PropertyReader.class.getClassLoader()
                .getResourceAsStream(propertyFileName)) {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        }
    }
}
