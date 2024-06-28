package com.containerstore.offer.exception;


import com.containerstore.common.base.exception.BusinessException;

public class TrackedOfferUpdateException extends BusinessException  {

    public TrackedOfferUpdateException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
