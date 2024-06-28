package com.containerstore.offer.service;

import com.containerstore.offer.domain.CustomAmountAdjustmentRequest;
import com.containerstore.offer.domain.Offer;
import com.containerstore.offer.domain.OfferRedemptionRequest;
import com.containerstore.offer.domain.TrackedOfferExpirationRequest;

public interface OfferRecordingService {
    void redeemOffer(OfferRedemptionRequest request);
    void unredeemOffer(OfferRedemptionRequest request);
    void replenishDecliningBalanceOffer(CustomAmountAdjustmentRequest request);
    Offer expireTrackedOffer(String offerId, TrackedOfferExpirationRequest request);
}
