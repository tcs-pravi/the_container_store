package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.containerstore.common.base.money.Money;

import static com.google.common.base.MoreObjects.toStringHelper;

public class OrderLineAdjustment {

    private Offer appliedOffer;
    private Money unitAdjustment;
    private boolean isFixedPriceAdjustment = false;

    @RequiredBy("Unmarshalling")
    OrderLineAdjustment() {
    }

    public OrderLineAdjustment(Offer offer, Money unitAdjustment) {
        this(offer, unitAdjustment, false);
    }

    public OrderLineAdjustment(Offer offer, Money unitAdjustment, boolean isFixedPriceAdjustment) {
        this.appliedOffer = offer;
        this.unitAdjustment = unitAdjustment;
        this.isFixedPriceAdjustment = isFixedPriceAdjustment;
    }

    public Offer getAppliedOffer() {
        return appliedOffer;
    }

    public Money getUnitAdjustment() {
        return unitAdjustment;
    }

    public boolean isFixedPriceAdjustment() {
        return this.isFixedPriceAdjustment;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("appliedOffer", appliedOffer)
                .add("unitAdjustment", unitAdjustment)
                .add("isFixedPriceAdjustment", isFixedPriceAdjustment)
                .toString();
    }
}
