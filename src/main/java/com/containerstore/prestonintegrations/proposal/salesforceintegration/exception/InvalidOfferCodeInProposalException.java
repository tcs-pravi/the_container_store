package com.containerstore.prestonintegrations.proposal.salesforceintegration.exception;

import com.containerstore.common.base.exception.BusinessException;

import java.io.Serial;

public class InvalidOfferCodeInProposalException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 3958757361551153653L;

    public InvalidOfferCodeInProposalException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
