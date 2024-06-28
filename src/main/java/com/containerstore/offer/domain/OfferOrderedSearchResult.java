package com.containerstore.offer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.containerstore.common.base.Lists.nullToEmpty;

public class OfferOrderedSearchResult {

    private final List<Offer> offers = new ArrayList<>();

    @JsonCreator
    public OfferOrderedSearchResult(@JsonProperty("offers") List<Offer> offers) {
        this.offers.addAll(nullToEmpty(offers));
    }

    public List<Offer> getOffers() {
        return ImmutableList.copyOf(offers);
    }
}
