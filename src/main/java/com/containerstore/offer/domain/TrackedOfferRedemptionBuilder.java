package com.containerstore.offer.domain;

public class TrackedOfferRedemptionBuilder {

    private Long trackedOfferCodeId;
    private String orderId;
    private org.joda.time.LocalDate orderDate;

    public static TrackedOfferRedemptionBuilder builder() {
        return new TrackedOfferRedemptionBuilder();
    }

    public TrackedOfferRedemptionBuilder withTrackedOfferCodeId(Long trackedOfferCodeId) {
        this.trackedOfferCodeId = trackedOfferCodeId;
        return this;
    }

    public TrackedOfferRedemptionBuilder withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public TrackedOfferRedemptionBuilder withOrderDate(org.joda.time.LocalDate orderDate) {
        this.orderDate = orderDate;
        return this;
    }

    public TrackedOfferRedemption build() {
        return new TrackedOfferRedemption(
                trackedOfferCodeId,
                orderId,
                orderDate
        );
    }
}
