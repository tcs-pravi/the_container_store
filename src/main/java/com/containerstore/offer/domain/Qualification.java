package com.containerstore.offer.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes({
        @Type(value = BoundaryQualification.class),
        @Type(value = MatchSetQualification.class)
})
public interface Qualification {
    Long getId();
    boolean matches(Object value);
    List<Object> getParameters();
    BigDecimal getBenefitValue();
}
