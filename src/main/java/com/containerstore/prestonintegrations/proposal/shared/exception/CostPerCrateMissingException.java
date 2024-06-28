package com.containerstore.prestonintegrations.proposal.shared.exception;

import com.containerstore.common.base.exception.BusinessException;

import java.io.Serial;

public class CostPerCrateMissingException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 9040387060761324336L;

    public CostPerCrateMissingException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
