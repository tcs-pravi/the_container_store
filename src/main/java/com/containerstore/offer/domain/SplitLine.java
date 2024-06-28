package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import org.immutables.value.Value;

@Value.Immutable
@InterfaceBasedBuilderStyle
public interface SplitLine {
    OfferOrderLine getOriginalLine();
    OfferOrderLine getLineWithDesiredQuantity();
    OfferOrderLine getLineWithRemainingQuantity();
}