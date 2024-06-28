package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableAdditionalShippingFeeWaivedBenefit.class)
public interface AdditionalShippingFeeWaivedBenefit {

    Long getId();
    Optional<String> getAppliesToRule();
}
