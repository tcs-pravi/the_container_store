package com.containerstore.offer.domain;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public class QuantityAwareOfferBenefitPair extends OfferBenefitPair {

    private int quantity;

    public QuantityAwareOfferBenefitPair(Offer offer, AdjustmentBenefit benefit) {
        this(offer, benefit, 1);
    }

    public QuantityAwareOfferBenefitPair(Offer offer, AdjustmentBenefit benefit, int quantity) {
        super(offer, benefit);
        this.quantity = quantity;
    }

    @Override
    public BigDecimal adjustmentValue() {
        return getBenefit().getAdjustmentValue().divide(new BigDecimal(quantity), 4, HALF_UP);
    }
}
