package com.containerstore.common.base.validation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public final class ValidationResults {

    private ValidationResults() {
        throw new UnsupportedOperationException();
    }

    public static List<ValidationResult> errors(List<ValidationResult> results) {
        return results.stream()
                .filter(errors())
                .collect(toList());
    }

    public static List<ValidationResult> warnings(List<ValidationResult> results) {
        return results.stream()
                .filter(warnings())
                .collect(toList());
    }

    public static List<ValidationResult> infos(List<ValidationResult> results) {
        return results.stream()
                .filter(infos())
                .collect(toList());
    }

    public static List<ValidationResult> errorsOrWarnings(List<ValidationResult> results) {
        return results.stream()
                .filter(errors().or(warnings()))
                .collect(toList());
    }

    public static Function<ValidationResult, String> toMessages() {
        return ValidationResult::getMessage;
    }

    private static Predicate<ValidationResult> errors() {
        return result -> result.getSeverity() == ValidationSeverity.ERROR;
    }

    private static Predicate<ValidationResult> warnings() {
        return result -> result.getSeverity() == ValidationSeverity.WARNING;
    }

    private static Predicate<ValidationResult> infos() {
        return result -> result.getSeverity() == ValidationSeverity.INFO;
    }
}
