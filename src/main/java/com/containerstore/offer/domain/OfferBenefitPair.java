package com.containerstore.offer.domain;

import java.math.BigDecimal;
import java.util.Comparator;

public class OfferBenefitPair {

    private final Offer offer;
    private final AdjustmentBenefit benefit;

    public OfferBenefitPair(Offer offer, AdjustmentBenefit benefit) {
        this.offer = offer;
        this.benefit = benefit;
    }

    public Offer getOffer() {
        return offer;
    }

    public AdjustmentBenefit getBenefit() {
        return benefit;
    }

    public BigDecimal adjustmentValue() {
        return getBenefit().getAdjustmentValue();
    }

    public static final class Fn {

        private Fn() {
            throw new UnsupportedOperationException();
        }

        public static Comparator<OfferBenefitPair> byAdjustmentValueDescending() {
            return (first, second) -> second.adjustmentValue().compareTo(first.adjustmentValue());
        }

        public static Comparator<OfferBenefitPair> byAdjustmentValueAscending() {
            return (first, second) -> first.adjustmentValue().compareTo(second.adjustmentValue());
        }

        public static Comparator<OfferBenefitPair> comparatorByAdjustmentType(AdjustmentType type) {
            return AdjustmentType.FIXED_PRICE.equals(type)
                    ? byAdjustmentValueAscending()
                    : byAdjustmentValueDescending();
        }
    }
}
