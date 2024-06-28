package com.containerstore.offer.exception;

import com.containerstore.common.base.exception.BusinessException;

public class CustomerTrackedOfferGenerationException extends BusinessException {

    public CustomerTrackedOfferGenerationException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
