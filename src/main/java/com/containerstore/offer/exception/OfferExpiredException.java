package com.containerstore.offer.exception;

public class OfferExpiredException extends InvalidOfferException {
    public OfferExpiredException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}