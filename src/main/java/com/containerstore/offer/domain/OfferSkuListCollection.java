package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.*;

@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableOfferSkuListCollection.class)
public interface OfferSkuListCollection {

 @Value.Default
 default Map<String, Set<Long>> getSkuListMap() {
   return new HashMap<>();
 }

 @Nullable
 default Collection<Long> get(String key) {
  return getSkuListMap().get(key);
 }

 @Nullable
 default Boolean listContainsSku(String key, String sku) {
  return  listContainsSku(key, Long.parseLong(sku));
 }

 @Nullable
 default Boolean listContainsSku(String key, Long sku) {
  return !Objects.isNull(getSkuListMap().get(key))
          ? getSkuListMap().get(key).contains(sku)
          : null;
 }
}
