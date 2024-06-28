package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.joda.time.DateTime;

import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableCustomerOfferRenewal.class)
public interface CustomerOfferRenewal {
    Long getId();
    Long getOfferId();
    CustomerIdentifier getCustomerId();
    DateTime getEnabledAt();
    Optional<DateTime> getRenewedAt();
}
