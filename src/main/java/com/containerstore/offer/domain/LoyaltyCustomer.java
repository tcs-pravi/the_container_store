package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.joda.time.LocalDateTime;

import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableLoyaltyCustomer.class)
public interface LoyaltyCustomer {

    Optional<LocalDateTime> getPopEnrollmentDateTime();

    Optional<Integer> getLoyaltyTier();
}
