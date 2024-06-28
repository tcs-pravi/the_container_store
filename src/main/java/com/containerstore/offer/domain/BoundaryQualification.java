package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonTypeName(value = "boundary")
@JsonDeserialize(as = ImmutableBoundaryQualification.class)
public interface BoundaryQualification extends Qualification {

    @Override
    Long getId();

    @Override
    BigDecimal getBenefitValue();
    Optional<BigDecimal> getLowerBound();
    Optional<BigDecimal> getUpperBound();

    @Override
    @JsonIgnore
    default List<Object> getParameters() {
        List<Object> qualifications = new ArrayList<>();
        getLowerBound().ifPresent(qualifications::add);
        getUpperBound().ifPresent(qualifications::add);

        return qualifications;
    }

    @Value.Check
    default void isValid() {
        Preconditions.checkArgument(getLowerBound().isPresent() || getUpperBound().isPresent(),
                "Boundary Qualifier (%s) requires an upper or lower boundary", getId());
    }

    @Override
    default boolean matches(final Object value) {
        Optional<BigDecimal> computedValue = BigDecimalConverter.fromObject(value);
        if (computedValue.isPresent()) {
            return isLowerBoundSatisfiedBy(computedValue.get()) && isUpperBoundSatisfiedBy(computedValue.get());
        }
        return false;
    }

    default boolean isLowerBoundSatisfiedBy(BigDecimal value) {
        return getLowerBound()
                .map(value::compareTo)
                .map(a -> a >= 0)
                .orElse(true);
    }

    default boolean isUpperBoundSatisfiedBy(BigDecimal value) {
        return getUpperBound()
                .map(value::compareTo)
                .map(a -> a < 0)
                .orElse(true);
    }
}