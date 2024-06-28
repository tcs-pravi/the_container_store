package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableQuoteFulfillment.class)
public interface QuoteFulfillment {

    String getFulfillmentId();
    List<QuoteOfferOrderLine> getSkus();
    String getFulfillmentType();
    List<Fee> getFees();
}
