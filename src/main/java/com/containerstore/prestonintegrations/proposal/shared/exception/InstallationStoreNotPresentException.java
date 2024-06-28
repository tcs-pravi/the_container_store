package com.containerstore.prestonintegrations.proposal.shared.exception;

import com.containerstore.common.base.exception.BusinessException;

import java.io.Serial;

public class InstallationStoreNotPresentException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 7595574692885527552L;

    public InstallationStoreNotPresentException(String formatSpec, Object... args) {
        super(formatSpec, args);
    }
}
