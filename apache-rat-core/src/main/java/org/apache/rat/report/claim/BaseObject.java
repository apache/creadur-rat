package org.apache.rat.report.claim;


/**
 * Default implementation of {@link IObject}.
 */
public class BaseObject implements IObject {
    private final String value;

    /**
     * Creates a new instance with the given value.
     */
    public BaseObject(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

    public boolean equals(Object other) {
        if (other == null  ||  getClass() != other.getClass()) {
            return false;
        }
        return value.equals(((BaseObject) other).value);
    }

    public int hashCode() {
        return value.hashCode();
    }
}
