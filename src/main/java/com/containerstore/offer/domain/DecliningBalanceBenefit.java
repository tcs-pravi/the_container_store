package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableDecliningBalanceBenefit.class)
public interface DecliningBalanceBenefit {

    Long getId();
    Money getInitialValue();
    Money getCurrentValue();
    Optional<String> getAppliesToRule();
}
