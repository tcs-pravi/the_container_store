package com.containerstore.offer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.twoqubed.bob.annotation.Built;
import org.joda.time.LocalDate;

/**
 * Represents a redemption of a single-use or multi-use unique offer code.
 */
@Built
public class TrackedOfferRedemption {

    private final Long trackedOfferCodeId;
    private final String orderId;
    private final LocalDate orderDate;

    @JsonCreator
    TrackedOfferRedemption(
            @JsonProperty("trackedOfferCodeId") Long trackedOfferCodeId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("orderDate") LocalDate orderDate) {
        this.trackedOfferCodeId = trackedOfferCodeId;
        this.orderId = orderId;
        this.orderDate = orderDate;
    }

    public Long getTrackedOfferCodeId() {
        return trackedOfferCodeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public static TrackedOfferRedemptionBuilder builder() {
        return TrackedOfferRedemptionBuilder.builder();
    }
}
