package com.containerstore.common.base.validation;

import com.containerstore.common.base.RequiredBy;
import lombok.Data;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

@Data
public class ValidationResult {
    private ValidationSeverity severity;
    private String code;
    private String message;

    @RequiredBy("Unmarshalling")
    public ValidationResult() {
    }

    public ValidationResult(ValidationSeverity severity, String code, String messageFormat, Object... args) {
        this.severity = severity;
        this.code = code;
        this.message = format(messageFormat, args);
    }

    public ValidationResult(ValidationSeverity severity, String code) {
        this(severity, code, "");
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("severity", severity)
                .add("code", code)
                .add("message", message)
                .toString();
    }

    public static ValidationResult validationError(String code, String messageFormat, Object... args) {
        return new ValidationResult(ValidationSeverity.ERROR, code, messageFormat, args);
    }
}
