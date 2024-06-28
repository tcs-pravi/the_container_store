package com.containerstore.offer.domain;

public class OfferCustomers {
    public static final OfferCustomer ANONYMOUS_CUSTOMER = ImmutableOfferCustomer.builder().build();

    private OfferCustomers() {
        throw new UnsupportedOperationException();
    }
}
