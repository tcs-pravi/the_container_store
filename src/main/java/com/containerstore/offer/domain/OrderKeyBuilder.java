package com.containerstore.offer.domain;

public class OrderKeyBuilder {

    private String orderId;
    private org.joda.time.LocalDate orderDate;

    public static OrderKeyBuilder builder() {
        return new OrderKeyBuilder();
    }

    public OrderKeyBuilder withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderKeyBuilder withOrderDate(org.joda.time.LocalDate orderDate) {
        this.orderDate = orderDate;
        return this;
    }

    public OrderKey build() {
        return new OrderKey(
                orderId,
                orderDate
        );
    }
}
