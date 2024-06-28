package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableOfferSkuListRecord.class)
public interface OfferSkuListRecord {

    String getSkuListId();
    Long getSku();
}
