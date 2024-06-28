package com.containerstore.prestonintegrations.proposal.salesforceintegration.exception;

import com.containerstore.common.base.exception.BusinessException;

import java.io.Serial;

public class OpportunityNotFoundException extends BusinessException {
    @Serial
    private static final long serialVersionUID = -8904221299037441086L;

    public OpportunityNotFoundException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
