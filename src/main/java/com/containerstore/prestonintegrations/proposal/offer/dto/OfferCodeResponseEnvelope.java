package com.containerstore.prestonintegrations.proposal.offer.dto;

import com.containerstore.offer.domain.Offer;

import java.util.Optional;

public record OfferCodeResponseEnvelope(Optional<Offer> offer, String offerCode, String message, java.util.List<com.containerstore.common.base.validation.ValidationResult> validationResults) {
    public  boolean isValid(){
        return this.offer.isPresent();
    }

    public boolean hasValidationErrors(){
        return validationResults != null && !validationResults.isEmpty();
    }

}
