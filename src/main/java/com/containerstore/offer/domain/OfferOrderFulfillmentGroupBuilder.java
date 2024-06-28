package com.containerstore.offer.domain;

public class OfferOrderFulfillmentGroupBuilder {

    private String fulfillmentGroupId;
    private FulfillmentGroupType fulfillmentGroupType;
    private ShippingMethod shippingMethod;
    private String shippingState;
    private java.util.List<OfferOrderLine> offerOrderLines;
    private java.util.List<Fee> fees;

    public static OfferOrderFulfillmentGroupBuilder builder() {
        return new OfferOrderFulfillmentGroupBuilder();
    }

    public OfferOrderFulfillmentGroupBuilder withFulfillmentGroupId(String fulfillmentGroupId) {
        this.fulfillmentGroupId = fulfillmentGroupId;
        return this;
    }

    public OfferOrderFulfillmentGroupBuilder withFulfillmentGroupType(FulfillmentGroupType fulfillmentGroupType) {
        this.fulfillmentGroupType = fulfillmentGroupType;
        return this;
    }

    public OfferOrderFulfillmentGroupBuilder withShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
        return this;
    }

    public OfferOrderFulfillmentGroupBuilder withShippingState(String shippingState) {
        this.shippingState = shippingState;
        return this;
    }

    public OfferOrderFulfillmentGroupBuilder withOfferOrderLines(java.util.List<OfferOrderLine> offerOrderLines) {
        this.offerOrderLines = offerOrderLines;
        return this;
    }

    public OfferOrderFulfillmentGroupBuilder withFees(java.util.List<Fee> fees) {
        this.fees = fees;
        return this;
    }

    public OfferOrderFulfillmentGroup build() {
        return new OfferOrderFulfillmentGroup(
                fulfillmentGroupId,
                fulfillmentGroupType,
                shippingMethod,
                shippingState,
                offerOrderLines,
                fees
        );
    }
}
