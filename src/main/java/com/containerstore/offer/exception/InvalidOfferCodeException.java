package com.containerstore.offer.exception;

public class InvalidOfferCodeException extends InvalidOfferException {
    public InvalidOfferCodeException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
