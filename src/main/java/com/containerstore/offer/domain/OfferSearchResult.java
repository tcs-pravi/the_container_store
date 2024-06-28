package com.containerstore.offer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;

public class OfferSearchResult {

    private final Set<Offer> offers = new HashSet<>();

    @JsonCreator
    public OfferSearchResult(@JsonProperty("offers") Collection<Offer> offers) {
        this.offers.addAll(firstNonNull(offers, new HashSet<Offer>()));
    }

    public Set<Offer> getOffers() {
        return ImmutableSet.copyOf(offers);
    }
}
