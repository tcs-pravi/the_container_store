package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.joda.time.LocalDate;

import javax.annotation.Nullable;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableOfferRedemptionRequest.class)
public interface OfferRedemptionRequest {
    String getOfferId();
    String getOrderId();
    LocalDate getOrderDate();
    String getOfferCode();
    @Nullable
    CustomerIdentifier getCustomerIdentifier();
    @Nullable
    Money getRedemptionAmount();
}
