package org.apache.rat.config.parameters;

import java.util.function.Supplier;

import org.apache.rat.config.parameters.Component.Type;

public class DescriptionImpl implements Component.Description {
    private final Type type;
    private final String name;
    private final String desc;
    private final Supplier<String> getter;
    
    public DescriptionImpl(Type type, String name, String desc, Supplier<String> getter) {
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.getter = getter == null ? () -> "" : getter;;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getCommonName() {
        return name;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String getParamValue() {
        return getter.get();
    }
}
