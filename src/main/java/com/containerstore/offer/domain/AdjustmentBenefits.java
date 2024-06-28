package com.containerstore.offer.domain;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.containerstore.offer.domain.AdjustmentType.FLAT_RATE_SHIPPING;
import static com.containerstore.offer.domain.AdjustmentType.FREE_SHIPPING;

public final class AdjustmentBenefits {

    private AdjustmentBenefits() {
        throw new UnsupportedOperationException();
    }

    public static Function<AdjustmentBenefit, Long> toAdjustmentBenefitId() {
        return AdjustmentBenefit::getId;
    }

    public static Predicate<AdjustmentBenefit> withAdjustmentType(final AdjustmentType adjustmentType) {
        return benefit -> benefit.getAdjustmentType().equals(adjustmentType);
    }

    public static Predicate<AdjustmentBenefit> withShippingAdjustmentType() {
        return withAdjustmentType(FREE_SHIPPING).or(withAdjustmentType(FLAT_RATE_SHIPPING));
    }

    public static Function<AdjustmentBenefit, AdjustmentBenefit> toCustomAdjustment(
            final BigDecimal customAdjustmentValue) {
        return benefit -> ImmutableAdjustmentBenefit.copyOf(benefit).withAdjustmentValue(customAdjustmentValue);
    }
}
