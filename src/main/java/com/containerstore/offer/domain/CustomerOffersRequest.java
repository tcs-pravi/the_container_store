package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import static com.google.common.base.Preconditions.checkState;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableCustomerOffersRequest.class)
public interface CustomerOffersRequest {

    int getRingStore();
    OfferCustomer getCustomer();

    @Value.Check
    default void check() {
        checkState(getRingStore() > 0, "Ring store number must be greater than 0");
        checkState(!getCustomer().getIsPopMember() || !getCustomer().isAnonymous(),
                "POP customers cannot be anonymous");
    }
}
