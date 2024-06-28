package com.containerstore.offer.domain;

public class OfferMessageBuilder {

    private MessageType type;
    private byte[] messageBytes;

    public static OfferMessageBuilder builder() {
        return new OfferMessageBuilder();
    }

    public OfferMessageBuilder withType(MessageType type) {
        this.type = type;
        return this;
    }

    public OfferMessageBuilder withMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
        return this;
    }

    public OfferMessage build() {
        return new OfferMessage(
                type,
                messageBytes
        );
    }
}
