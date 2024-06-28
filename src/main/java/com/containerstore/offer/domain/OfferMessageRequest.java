package com.containerstore.offer.domain;

import com.twoqubed.bob.annotation.Built;

@Built
public class OfferMessageRequest {

    private final String offerCode;
    private final MessageType type;
    private final int storeNumber;

    OfferMessageRequest(String offerCode, MessageType type, int storeNumber) {
        this.offerCode = offerCode;
        this.type = type;
        this.storeNumber = storeNumber;
    }

    public MessageType getType() {
        return type;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public int getStoreNumber() {
        return storeNumber;
    }

    public static OfferMessageRequestBuilder builder() {
        return OfferMessageRequestBuilder.builder();
    }
}
