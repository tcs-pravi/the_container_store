package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableApplyForQuoteRequest.class)
public interface ApplyForQuoteRequest {

    Optional<String> getCustomerId();
    int getRingStore();

    Optional<PurchasingChannel> getPurchasingChannel();
    List<QuoteFulfillment> getFulfillments();
}
