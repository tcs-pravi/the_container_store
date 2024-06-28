package com.containerstore.prestonintegrations.proposal.salesforceintegration.exception;

import com.containerstore.common.base.exception.BusinessException;

import java.io.Serial;

public class SpaceValidationException extends BusinessException {
    @Serial
    private static final long serialVersionUID = -8904221299037441086L;

    public SpaceValidationException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
