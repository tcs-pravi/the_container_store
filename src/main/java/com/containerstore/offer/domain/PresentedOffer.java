package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.twoqubed.bob.annotation.Built;
import org.joda.time.LocalDate;

@Built
public class PresentedOffer {

    private long offerId;
    private Long trackedOfferCodeId;
    private String orderId;
    private LocalDate orderDate;

    @RequiredBy("Unmarshalling")
    PresentedOffer() {}

    PresentedOffer(long offerId, Long trackedOfferCodeId, String orderId, LocalDate orderDate) {
        this.offerId = offerId;
        this.trackedOfferCodeId = trackedOfferCodeId;
        this.orderId = orderId;
        this.orderDate = orderDate;
    }

    public Long getTrackedOfferCodeId() {
        return trackedOfferCodeId;
    }

    public void setTrackedOfferCodeId(Long trackedOfferCodeId) {
        this.trackedOfferCodeId = trackedOfferCodeId;
    }

    public long getOfferId() {
        return offerId;
    }

    public void setOfferId(long offerId) {
        this.offerId = offerId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public static PresentedOfferBuilder builder() {
        return PresentedOfferBuilder.builder();
    }
}

