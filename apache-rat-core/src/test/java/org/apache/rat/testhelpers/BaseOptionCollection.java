package org.apache.rat.testhelpers;

import org.apache.commons.cli.Option;
import org.apache.rat.ui.UIOptionCollection;

public final class BaseOptionCollection extends UIOptionCollection<BaseOption> {
    private BaseOptionCollection(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends UIOptionCollection.Builder<BaseOption, Builder> {
        private Builder() {
            super(BaseOption::new);
        }

        public BaseOptionCollection build() {
            return new BaseOptionCollection(this);
        }
    }
}
