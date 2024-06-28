package com.containerstore.offer.exception;

import com.containerstore.common.base.exception.BusinessException;

public class DecliningBalanceReplenishException extends BusinessException {
    public DecliningBalanceReplenishException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
