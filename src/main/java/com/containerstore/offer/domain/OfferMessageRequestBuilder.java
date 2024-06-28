package com.containerstore.offer.domain;

public class OfferMessageRequestBuilder {

    private String offerCode;
    private MessageType type;
    private int storeNumber;

    public static OfferMessageRequestBuilder builder() {
        return new OfferMessageRequestBuilder();
    }

    public OfferMessageRequestBuilder withOfferCode(String offerCode) {
        this.offerCode = offerCode;
        return this;
    }

    public OfferMessageRequestBuilder withType(MessageType type) {
        this.type = type;
        return this;
    }

    public OfferMessageRequestBuilder withStoreNumber(int storeNumber) {
        this.storeNumber = storeNumber;
        return this;
    }

    public OfferMessageRequest build() {
        return new OfferMessageRequest(
                offerCode,
                type,
                storeNumber
        );
    }
}
