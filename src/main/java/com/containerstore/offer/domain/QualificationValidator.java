package com.containerstore.offer.domain;

import com.google.common.base.Preconditions;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class QualificationValidator {

    private QualificationValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validate(QualifiedAdjustmentBenefit benefit) {
        validateAdjustmentType(benefit);
        validateUniqueBoundaries(benefit);
        validateQualificationCount(benefit);
    }

    private static void validateAdjustmentType(final QualifiedAdjustmentBenefit benefit) {
        Preconditions.checkState(
                benefit.getAdjustmentType() == AdjustmentType.PERCENT_OFF
                || benefit.getAdjustmentType() == AdjustmentType.AMOUNT_OFF,
                "Qualified adjustment benefits only support amount off and percent off adjustments");
    }

    private static void validateUniqueBoundaries(QualifiedAdjustmentBenefit benefit) {
        benefit.getQualifications().stream()
            .map(Qualification::getParameters)
            .flatMap(List::stream)
            .forEach(t -> ensureValueMatchesOnlyOneQualification(benefit, t));
        validateExtremeBoundaries(benefit);
    }

    private static void validateExtremeBoundaries(final QualifiedAdjustmentBenefit benefit) {
        ensureValueMatchesOnlyOneQualification(benefit, MIN_VALUE);
        ensureValueMatchesOnlyOneQualification(benefit, MAX_VALUE);
    }

    private static void ensureValueMatchesOnlyOneQualification(QualifiedAdjustmentBenefit benefit, Object value) {
        long qualifiedCount = benefit.getQualifications().stream()
                .filter(q -> q.matches(value))
                .count();
        checkState(qualifiedCount <= 1, "Qualified Benefit (%s) has overlapping qualifications", benefit.getId());
    }

    private static void validateQualificationCount(final QualifiedAdjustmentBenefit benefit) {
        long count = benefit.getQualifications().stream()
                .map(Qualification::getParameters)
                .flatMap(List::stream)
                .count();
        checkState(count > 0, "Qualified Benefit (%s) requires at least 1 qualification", benefit.getId());
    }
}
