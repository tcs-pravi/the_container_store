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
import java.util.Objects;
import java.util.Set;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonTypeName(value = "matchSet")
@JsonDeserialize(as = ImmutableMatchSetQualification.class)
public interface MatchSetQualification extends Qualification {

    @Override
    Long getId();

    @Override
    BigDecimal getBenefitValue();

    Set<String> getMatchSet();

    @Override
    @JsonIgnore
    default List<Object> getParameters() {
        return new ArrayList<>(getMatchSet());
    }

    @Override
    default boolean matches(final Object value) {
        return getMatchSet().contains(Objects.toString(value));
    }

    @Value.Check
    @Value.Derived
    default void isValid() {
        Preconditions.checkArgument(!getMatchSet().isEmpty(),
                "Match Set Qualification (%s) requires at least one qualifying parameter", getId());
    }
}