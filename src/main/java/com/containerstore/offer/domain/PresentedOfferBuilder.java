package com.containerstore.offer.domain;

public class PresentedOfferBuilder {

    private long offerId;
    private Long trackedOfferCodeId;
    private String orderId;
    private org.joda.time.LocalDate orderDate;

    public static PresentedOfferBuilder builder() {
        return new PresentedOfferBuilder();
    }

    public PresentedOfferBuilder withOfferId(long offerId) {
        this.offerId = offerId;
        return this;
    }

    public PresentedOfferBuilder withTrackedOfferCodeId(Long trackedOfferCodeId) {
        this.trackedOfferCodeId = trackedOfferCodeId;
        return this;
    }

    public PresentedOfferBuilder withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public PresentedOfferBuilder withOrderDate(org.joda.time.LocalDate orderDate) {
        this.orderDate = orderDate;
        return this;
    }

    public PresentedOffer build() {
        return new PresentedOffer(
                offerId,
                trackedOfferCodeId,
                orderId,
                orderDate
        );
    }
}
