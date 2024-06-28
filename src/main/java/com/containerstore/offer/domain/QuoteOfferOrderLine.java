package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;


@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableQuoteOfferOrderLine.class)
public interface QuoteOfferOrderLine {

    long getSkuNumber();
    int getQuantity();
    String getLineId();

    @Nullable String getSpaceUseId();
    @Nullable String getSpaceSource();
    @Nullable String getSpaceId();
}
