package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;


@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableLegacyCustomerIdentifier.class)
public interface LegacyCustomerIdentifier {

     @Nullable
     String getCustomerId();
     @Nullable
     String getCustomerType();

     @Value.Check
     default void isValid() {
          if (!isNullOrEmpty(getCustomerId()) || !isNullOrEmpty(getCustomerType())) {
               String concatenatedId = getCustomerType() + getCustomerId();
               Matcher matcher = legacyCustomerIdPattern().matcher(concatenatedId);
               if (!matcher.matches()) {
                    throw new IllegalArgumentException(format("Invalid customer identifier (%s)", concatenatedId));
               }
          }
     }

     default LegacyCustomerIdentifier parse(String customerId) {
          Matcher matcher = legacyCustomerIdPattern().matcher(customerId);
          if (!matcher.matches()) {
               throw new IllegalArgumentException(format("Invalid customer identifier (%s)", customerId));
          }
          return ImmutableLegacyCustomerIdentifier.builder()
                  .withCustomerId(matcher.group(2))
                  .withCustomerType(matcher.group(1))
                  .build();
     }

     @Value.Derived
     default Optional<CustomerIdentifier> getCustomerIdentifier() {
          if (isNullOrEmpty(getCustomerId()) || isNullOrEmpty(getCustomerType())) {
               return Optional.empty();
          }
          return Optional.of(ImmutableCustomerIdentifier.of(getCustomerType() + getCustomerId()));
     }

     default Pattern legacyCustomerIdPattern() {
          return Pattern.compile("^([a-zA-Z]+)(\\d+)$");
     }
}
