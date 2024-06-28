package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableSkuListResult.class)
public interface SkuListResult {
    Map<String, List<Long>> getSkuLists();
}
