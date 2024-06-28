package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.containerstore.common.base.BigDecimals.ONE_HUNDRED;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableFeeAdjustmentBenefit.class)
public interface FeeAdjustmentBenefit {

    String getFeeType();
    BigDecimal getAdjustmentPercent();
    @Nullable
    String getAppliesToRule();

    @Value.Default
    default Money getFeeTypeMinimum() {
        return Money.ZERO;
    }

    @Value.Derived
    default boolean isFreeAdjustment() {
        return getAdjustmentPercent().compareTo(ONE_HUNDRED) == 0;
    }
}
