package com.containerstore.offer.service;

import com.containerstore.offer.domain.*;

public interface OfferService {
    Offer getPresentedOffer(PresentedOfferRequest request);
    OfferSearchResult getOffersForCustomer(CustomerOffersRequest request);
    OfferOrderedSearchResult getOffersForReview(CustomerIdentifier customerId);
    OfferSearchResult getActiveAvailableOffers(CustomerIdentifier customerId);
    Offer getOfferByCode(String offerCode);
}
