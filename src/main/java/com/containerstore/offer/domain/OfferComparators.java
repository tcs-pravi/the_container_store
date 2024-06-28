package com.containerstore.offer.domain;

import java.util.Comparator;

public final class OfferComparators {

    private OfferComparators() {
        throw new UnsupportedOperationException();
    }

    public static Comparator<Offer> byPercentOffFirst() {
        return (first, second) -> Boolean.compare(second.hasPercentOff(), first.hasPercentOff());
    }
}
