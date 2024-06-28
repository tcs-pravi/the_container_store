package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableEmailDeliveryRequest.class)
public interface EmailDeliveryRequest {
    String getCustomerId();
    String getEmailType();
    Map<String, String> getEmailData();
}
