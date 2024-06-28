package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.containerstore.common.base.Optionals.firstPresent;


/**
 * @deprecated this class will be removed when OfferController.deprecatedGetOffersForCustomer is removed
 */
@Deprecated
@Value.Immutable
@InterfaceBasedBuilderStyle
@JsonDeserialize(as = ImmutableGetOffersRequest.class)
public interface GetOffersRequest {
    @Nullable
    Integer getRingStore();
    Optional<OfferCustomer> getCustomer();
    Optional<String> getCustomerId();
    Optional<String> getCustomerType();

    @Value.Derived
    @JsonIgnore
    default Optional<OfferCustomer> getCustomerFromAvailableId() {
        return firstPresent(
                getCustomer(),
                withIdAndType());
    }

    default Optional<OfferCustomer> withIdAndType() {
        return getCustomerId()
                .map(id -> getCustomerType() + id)
                .map(cid -> ImmutableOfferCustomer.builder().withCustomerId(cid).build());
    }
}
