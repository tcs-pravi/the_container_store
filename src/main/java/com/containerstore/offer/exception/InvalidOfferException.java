package com.containerstore.offer.exception;

import com.containerstore.common.base.exception.BusinessException;

public class InvalidOfferException extends BusinessException {
    public InvalidOfferException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
