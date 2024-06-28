package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableQualifiedAdjustmentBenefit.class)
public interface QualifiedAdjustmentBenefit {
    Long getId();
    AppliesTo getAppliesTo();
    Optional<String> getAppliesToRule();
    AdjustmentType getAdjustmentType();
    String getQualificationsSource();
    List<Qualification> getQualifications();

    @Value.Check
    default void validateQualifications() {
        QualificationValidator.validate(this);
    }

    default Optional<Qualification> getBenefitFor(Object value) {
        return getQualifications().stream()
                .filter(p -> p.matches(value))
                .findAny();
    }
}