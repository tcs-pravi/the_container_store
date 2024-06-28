package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableRenewOfferRequest.class)
public interface RenewOfferRequest {
    String getCustomerId();

    @Value.Derived
    default CustomerIdentifier customerIdentifier() {
        return ImmutableCustomerIdentifier.of(getCustomerId());
    }
}
