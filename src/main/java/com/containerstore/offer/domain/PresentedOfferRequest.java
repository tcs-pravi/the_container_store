package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutablePresentedOfferRequest.class)
public interface PresentedOfferRequest {

    String getOfferCode();
    int getRingStore();

    @Value.Check
    default void check() {
        checkState(getRingStore() > 0, "RingStore number must be greater than 0");
        checkState(!isNullOrEmpty(getOfferCode()), "Offer code must be supplied");
    }
}
