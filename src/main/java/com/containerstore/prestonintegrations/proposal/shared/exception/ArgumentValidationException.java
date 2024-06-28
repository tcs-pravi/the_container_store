package com.containerstore.prestonintegrations.proposal.shared.exception;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class ArgumentValidationException extends MethodArgumentNotValidException {

	public ArgumentValidationException(MethodParameter parameter, BindingResult bindingResult) {
		super(parameter, bindingResult);
	}

	@Override
	public String getMessage() {
		List<String> errorFields = this.getBindingResult().getFieldErrors().stream().map(FieldError::getField).toList();
		return String.format("Validation error in fields %s", errorFields);
	}
}
