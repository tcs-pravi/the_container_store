package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableApplyForQuoteResponse.class)
public interface ApplyForQuoteResponse {

    Set<Offer> getWinningOffers();
    List<OfferOrderFulfillmentGroup> getFulfilmentGroups();
}
