package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableAdditionalShippingFeeAdjustment.class)
public interface AdditionalShippingFeeAdjustment {

    Money getAdjustmentPerUnit();
    String getDiscountReason();
}
