package com.containerstore.offer.domain;

public class OfferOrderBuilder {

    private Integer ringStore;
    private OfferCustomer purchasingCustomer;
    private String shippingFeeType;
    private java.util.List<OfferOrderFulfillmentGroup> fulfillmentGroups;
    private java.util.List<Offer> prospectiveOffers;
    private String sourceReferenceId;
    private PurchasingChannel purchasingChannel;

    public static OfferOrderBuilder builder() {
        return new OfferOrderBuilder();
    }

    public OfferOrderBuilder withRingStore(Integer ringStore) {
        this.ringStore = ringStore;
        return this;
    }

    public OfferOrderBuilder withPurchasingCustomer(OfferCustomer purchasingCustomer) {
        this.purchasingCustomer = purchasingCustomer;
        return this;
    }

    public OfferOrderBuilder withShippingFeeType(String shippingFeeType) {
        this.shippingFeeType = shippingFeeType;
        return this;
    }

    public OfferOrderBuilder withFulfillmentGroups(java.util.List<OfferOrderFulfillmentGroup> fulfillmentGroups) {
        this.fulfillmentGroups = fulfillmentGroups;
        return this;
    }

    public OfferOrderBuilder withProspectiveOffers(java.util.List<Offer> prospectiveOffers) {
        this.prospectiveOffers = prospectiveOffers;
        return this;
    }

    public OfferOrderBuilder withSourceReferenceId(String sourceReferenceId) {
        this.sourceReferenceId = sourceReferenceId;
        return this;
    }

    public OfferOrderBuilder withPurchasingChannel(PurchasingChannel purchasingChannel) {
        this.purchasingChannel = purchasingChannel;
        return this;
    }

    public OfferOrder build() {
        return new OfferOrder(
                ringStore,
                purchasingCustomer,
                shippingFeeType,
                fulfillmentGroups,
                prospectiveOffers,
                sourceReferenceId,
                purchasingChannel
        );
    }
}
