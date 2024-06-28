package com.containerstore.offer.service;

import com.containerstore.common.base.money.Money;
import com.containerstore.offer.domain.CustomerIdentifier;
import com.containerstore.offer.domain.Offer;
import com.containerstore.offer.domain.OrderKey;
import com.containerstore.offer.domain.TrackedOfferCodeMetadata;

import java.util.Optional;

public interface OfferGenerationService {
    Offer createTrackedOffer(Offer offer, TrackedOfferCodeMetadata metadata);
    Offer createTrackedOffer(String offerIdOrCode, TrackedOfferCodeMetadata metadata);
    Offer getOrCreateTrackedOffer(String offerIdOrCode, TrackedOfferCodeMetadata metadata);
    Offer getOrCreateTrackedOffer(Offer offer, TrackedOfferCodeMetadata metadata);
    Offer getOrCreateTrackedOffer(TrackedOfferCode trackedOfferCode, TrackedOfferCodeMetadata metadata);
    Offer createRemainingBalanceTrackedOffer(
            Offer redeemedTrackedOffer,
            CustomerIdentifier customerId,
            Money remainingBalance);
    Offer associateTrackedOffer(
            TrackedOfferCode trackedOfferCode,
            OrderKey orderKey,
            Optional<CustomerIdentifier> customerIdentifier);
    Offer validatedBaseOffer(String offerIdOrCode);
}
