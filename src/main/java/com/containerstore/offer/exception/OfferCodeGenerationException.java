package com.containerstore.offer.exception;

import com.containerstore.common.base.exception.BusinessException;

public class OfferCodeGenerationException extends BusinessException {
    public OfferCodeGenerationException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
