package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableTrackedOfferCodeMetadata.class)
public interface TrackedOfferCodeMetadata {
    @Nullable
    DateTime getStartDateTime();
    @Nullable
    String getAlternateIdentifier();
    @Nullable
    String getSourceOrderId();
    @Nullable
    String getCustomerId();
    @Nullable
    LocalDate getSourceOrderDate();
    @Nullable
    BigDecimal getCustomAdjustmentValue();
    @Nullable
    String getNote();

    @Value.Derived
    @JsonIgnore
    default OrderKey getSourceOrderKey() {
        return OrderKey.builder()
                .withOrderId(getSourceOrderId())
                .withOrderDate(getSourceOrderDate())
                .build();
    }

    default Optional<CustomerIdentifier> customerIdentifier() {
        return Optional.ofNullable(getCustomerId())
                .map(ImmutableCustomerIdentifier::of);
    }

}
