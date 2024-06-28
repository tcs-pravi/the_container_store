package com.containerstore.offer.exception;

public class OfferNotActiveException extends InvalidOfferException {
    public OfferNotActiveException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
