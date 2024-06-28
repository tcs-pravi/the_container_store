package com.containerstore.offer.exception;

import com.containerstore.common.base.exception.BusinessException;

public class InvalidSkuListIdException extends BusinessException {
    public InvalidSkuListIdException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
