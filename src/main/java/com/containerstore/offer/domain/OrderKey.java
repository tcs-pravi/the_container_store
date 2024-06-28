package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.google.common.base.Objects;
import com.twoqubed.bob.annotation.Built;
import org.joda.time.LocalDate;

import static com.google.common.base.MoreObjects.toStringHelper;

@Built
public class OrderKey {
    private String orderId;
    private LocalDate orderDate;

    @RequiredBy("Unmarshalling")
    private OrderKey() {
    }

    OrderKey(String orderId, LocalDate orderDate) {
        this.orderId = orderId;
        this.orderDate = orderDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String orderId() {
        return orderId;
    }

    public LocalDate orderDate() {
        return orderDate;
    }

    public static OrderKeyBuilder builder() {
        return OrderKeyBuilder.builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrderKey other = (OrderKey) o;
        return Objects.equal(orderId, other.orderId) && Objects.equal(orderDate, other.orderDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderId, orderDate);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("orderId", orderId)
                .add("orderDate", orderDate)
                .toString();
    }
}
