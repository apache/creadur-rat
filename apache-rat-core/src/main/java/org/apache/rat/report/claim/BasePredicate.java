package org.apache.rat.report.claim;


/**
 * Default implementation of {@link IPredicate}.
 */
public class BasePredicate implements IPredicate {
    private final String name;

    /**
     * Creates a new instance.
     */
    public BasePredicate(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object other) {
        if (other == null  ||  getClass() != other.getClass()) {
            return false;
        }
        return name.equals(((BasePredicate) other).name);
    }

    public int hashCode() {
        return name.hashCode();
    }
}
