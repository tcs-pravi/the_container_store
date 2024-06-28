package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.Optional;

import static com.containerstore.offer.domain.AdjustmentType.FIXED_PRICE;
import static com.containerstore.offer.domain.AdjustmentType.PERCENT_OFF;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableBuyGetAdjustmentBenefit.class)
public interface BuyGetAdjustmentBenefit {
    Long getId();
    String getQualificationGroupRule();
    Integer getQualificationGroupSize();
    String getAdjustmentGroupRule();
    Integer getAdjustmentGroupSize();
    Optional<Integer> getGroupCountLimit();
    BigDecimal getAdjustmentValue();
    AdjustmentType getAdjustmentType();

    @Value.Default
    default boolean allowAdjustmentLinesAboveQualificationMinimum() {
        return false;
    }

    @Value.Default
    default boolean discountLowestFirst() {
        // temporary kludge for proof of concept: negative adjustment group size enables
        // a new way of discounting for BuyGet benefits. Prior to this, discounting for BuyGet is
        // always done on the next available highest priced item. That will continue to
        // happen unless this value is true. If true, discounting for BuyGet will be done
        // on the next available lowest priced item.
        //
        // note: even though negative we take the absolute value when using the group size
        //
        // in the future, we would have a dedicated field/database column that controls
        // this value
        return getAdjustmentGroupSize().intValue() < 0;
    }

    @Value.Check
    default void validate() {
        Preconditions.checkState(getAdjustmentType() == PERCENT_OFF || getAdjustmentType() == FIXED_PRICE,
                "BuyGetAdjustment benefits must be percentage off or fixed price adjustment type.");
        Preconditions.checkState(getGroupCountLimit().map(l -> l >= 0).orElse(true), "Group limit cannot be negative.");
    }
}
