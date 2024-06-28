package com.containerstore.prestonintegrations.proposal.shared.exception;

import com.containerstore.common.base.exception.BusinessException;

public class SellingPriceGreaterThanRetailPriceException extends BusinessException {
    public SellingPriceGreaterThanRetailPriceException(String errorMessage) {
        super(errorMessage);
    }
}
