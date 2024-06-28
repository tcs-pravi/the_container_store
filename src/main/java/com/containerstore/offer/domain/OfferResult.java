package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.containerstore.common.base.validation.ValidationResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfferResult {

    private Offer offer;
    private List<ValidationResult> validationResults;

    @RequiredBy("Unmarshalling")
    public OfferResult() {
    }

    public OfferResult(Offer offer, List<ValidationResult> validationResults) {
        this.offer = offer;
        this.validationResults = validationResults;
    }

    public OfferResult(Offer offer) {
        this(offer, new ArrayList<>());
    }

    public Offer getOffer() {
        return offer;
    }

    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }
}
