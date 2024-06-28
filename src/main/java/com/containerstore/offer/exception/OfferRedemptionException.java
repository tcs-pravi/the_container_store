package com.containerstore.offer.exception;


import com.containerstore.common.base.exception.BusinessException;

public class OfferRedemptionException extends BusinessException  {

    public OfferRedemptionException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
