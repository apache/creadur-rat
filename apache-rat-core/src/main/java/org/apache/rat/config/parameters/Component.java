package org.apache.rat.config.parameters;

public interface Component {
    public enum Type { License, Matcher, Parameter, Text };

    /**
     * Returns the component Description.
     * @return the component description.
     */
    default Description getDescription() {
        return  DescriptionBuilder.build(this);
    }

}
