package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.containerstore.common.base.money.Money;

import static com.google.common.base.MoreObjects.toStringHelper;

public class FulfillmentGroupFeeAdjustment {

    private Offer appliedOffer;
    private String fulfillmentGroupId;
    private String feeType;
    private String feeId;
    private Money adjustmentAmount;

    @RequiredBy("Unmarshalling")
    FulfillmentGroupFeeAdjustment() {
    }

    public FulfillmentGroupFeeAdjustment(
            Offer appliedOffer,
            Money adjustmentAmount,
            String fulfillmentGroupId,
            String feeType,
            String feeId) {
        this.appliedOffer = appliedOffer;
        this.adjustmentAmount = adjustmentAmount;
        this.fulfillmentGroupId = fulfillmentGroupId;
        this.feeType = feeType;
        this.feeId = feeId;
    }

    public Offer getAppliedOffer() {
        return appliedOffer;
    }

    public String getFulfillmentGroupId() {
        return fulfillmentGroupId;
    }

    public Money getAdjustmentAmount() {
        return adjustmentAmount;
    }

    public String getFeeType() {
        return feeType;
    }

    public String getFeeId() {
        return feeId;
    }

    @Override
    public String toString() {
        return toStringHelper(this.getClass())
                .add("fulfillmentGroupId", fulfillmentGroupId)
                .add("adjustmentAmount", adjustmentAmount)
                .add("appliedOffer", appliedOffer)
                .add("feeType", feeType)
                .add("feeId", feeId)
                .toString();
    }
}
