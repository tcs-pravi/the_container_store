package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;


@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableCustomerIdentifier.class)
public interface CustomerIdentifier {

     @Value.Parameter
     String getId();

     @Value.Check
     default void isValid() {
          if (isNullOrEmpty(getId().trim())) {
               throw new IllegalArgumentException(format("Invalid customer identifier (%s)", getId()));
          }
     }
}
