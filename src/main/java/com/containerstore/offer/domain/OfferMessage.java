package com.containerstore.offer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.twoqubed.bob.annotation.Built;

import java.util.Arrays;

@Built
public class OfferMessage {

    private final byte[] messageBytes;
    private final MessageType type;

    @JsonCreator
    OfferMessage(
            @JsonProperty("type") MessageType type,
            @JsonProperty("messageBytes") byte[] messageBytes) {
        this.messageBytes = copyOf(messageBytes);
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getMessageBytes() {
        return copyOf(messageBytes);
    }

    public static OfferMessageBuilder builder() {
        return OfferMessageBuilder.builder();
    }

    private byte[] copyOf(byte[] bytes) {
        return (bytes == null) ? null : Arrays.copyOf(bytes, bytes.length);
    }
}
