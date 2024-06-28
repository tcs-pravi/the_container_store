package com.containerstore.prestonintegrations.proposal.freightfee.exception;

import com.containerstore.common.base.exception.BusinessException;
import com.google.common.base.Strings;

public class RetailPriceNotFoundException extends BusinessException {

	public RetailPriceNotFoundException(String formatSpec, Object... args) {
		super(String.format(Strings.nullToEmpty(formatSpec), args));
	}
}
