package org.apache.rat.configuration;

import java.util.Arrays;

public enum Format {
    CONFIG( "cfg", "config");
    
    
    String[] suffix;
    
    Format(String... suffix) {
        this.suffix = suffix;
    }
    
    public static Format fromName(String name) {
        Format.valueOf("foo");
        String[] parts = name.split("\\.");
        String suffix = parts[parts.length-1];
        for (Format f: Format.values()) {
            if (Arrays.stream(f.suffix).allMatch( suffix::equals )) {
                return f;
            }
        }
        throw new IllegalArgumentException(String.format("No such suffix: %s", suffix));
    }
}
