package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

import static com.containerstore.common.thirdparty.mvel.MvelHelper.evaluateExpression;
import static com.containerstore.offer.domain.AppliesTo.*;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableAdjustmentBenefit.class)
public interface AdjustmentBenefit {

    Long getId();
    AdjustmentType getAdjustmentType();
    AppliesTo getAppliesTo();
    List<String> getSkus();
    @Nullable
    BigDecimal getAdjustmentValue();
    @Nullable
    String getAppliesToRule();

    @Value.Derived
    @JsonIgnore
    default boolean isRuleSatisfiedBy(Offer offer, OfferOrder order, OfferOrderLine line) {
        return evaluateExpression(getAppliesToRule(), ImmutableMap.<String, Object>builder()
                .put("offer", offer)
                .put("order", order)
                .put("line", line)
                .put("skus", getSkus())
                .build());
    }

    @Value.Derived
    @JsonIgnore
    default boolean isHighestPricedUnitBenefit() {
        return HIGHEST_PRICED_UNIT == getAppliesTo();
    }

    @Value.Derived
    @JsonIgnore
    default boolean appliesToSpecialtyItem() {
        return TCS_CLOSETS == getAppliesTo() || PRESTON == getAppliesTo();
    }

}
