package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.joda.time.LocalDateTime;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableOfferCustomer.class)
public interface OfferCustomer {
    @Nullable
    String getCustomerId();
    @Nullable
    String getEmployeeId();

    // request format: 2016-09-01T23:11:32
    @Nullable
    LocalDateTime getPopEnrollmentDateTime();

    @Nullable
    Boolean getIsWebCustomer();

    @Nullable
    Integer getLoyaltyTier();

    @Value.Default
    default boolean getIsPopMember() {
        return false;
    }

    @Value.Derived
    @JsonIgnore
    default boolean hasCompletedWebProfile() {
        return Boolean.TRUE.equals(getIsWebCustomer());
    }

    @Value.Derived
    @JsonIgnore
    default boolean isLoyaltyMember() {
        return getIsPopMember();
    }

    @Value.Derived
    @JsonIgnore
    default boolean hasLoyaltyTier(int tier) {
        return Integer.valueOf(tier).equals(getLoyaltyTier());
    }

    @Value.Derived
    @JsonIgnore
    default boolean isAnonymous() {
        return isNullOrEmpty(getCustomerId());
    }

    @Value.Derived
    @JsonIgnore
    default boolean enrolledMinutesAgo(int minutes) {
        return Optional.ofNullable(getPopEnrollmentDateTime())
                .map(dt -> LocalDateTime.now().minusMinutes(minutes).isBefore(dt))
                .orElse(Boolean.FALSE);
    }

    @JsonIgnore
    default boolean isEmployee() {
        return !isNullOrEmpty(getEmployeeId());
    }

    @JsonIgnore
    default boolean isCompletelyPopulated() {
        return isAnonymous() || (
                getCustomerId() != null
                && getPopEnrollmentDateTime() != null
                && getLoyaltyTier() != null
                && getIsWebCustomer() != null);
    }
}
