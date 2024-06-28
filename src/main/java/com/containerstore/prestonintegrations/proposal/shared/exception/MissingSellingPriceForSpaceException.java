package com.containerstore.prestonintegrations.proposal.shared.exception;

import com.containerstore.common.base.exception.BusinessException;

public class MissingSellingPriceForSpaceException extends BusinessException {
    public MissingSellingPriceForSpaceException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
