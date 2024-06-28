package com.containerstore.prestonintegrations.proposal.shared.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import com.containerstore.spring.boot.starters.rest.RestError;
import lombok.Getter;

@Getter
public class FieldValidationRestError extends RestError {

	private final Map<String, String> errorMap;

	public FieldValidationRestError(HttpStatus status, Throwable cause, Map<String, String> errorMap) {
		super(status, cause);
		this.errorMap = errorMap;
	}
}
