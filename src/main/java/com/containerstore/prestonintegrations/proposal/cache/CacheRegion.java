package com.containerstore.prestonintegrations.proposal.cache;

import java.util.Locale;

public enum CacheRegion {
    STATE, PROPOSALCONSTANTS;

    String toLowerCaseString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
