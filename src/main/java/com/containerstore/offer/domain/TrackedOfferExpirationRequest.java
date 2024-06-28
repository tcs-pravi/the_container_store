package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableTrackedOfferExpirationRequest.class)
public interface TrackedOfferExpirationRequest {
    @Nullable
    DateTime getEndDateTime();
    String getTrackedOfferCode();
    @Nullable
    String getNote();

}
