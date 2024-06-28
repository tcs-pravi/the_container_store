package com.containerstore.offer.service;

import com.containerstore.offer.exception.InvalidOfferCodeException;

import java.util.Optional;

import static com.containerstore.offer.domain.CheckDigitValidator.hasAcceptableCheckDigit;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class InferredOfferCode {

    private final String offerCode;
    private final Optional<TrackedOfferCode> trackedOfferCode;

    private InferredOfferCode(String offerCode, Optional<TrackedOfferCode> trackedOfferCode) {
        this.offerCode = offerCode;
        this.trackedOfferCode = trackedOfferCode;
    }

    public static InferredOfferCode infer(String presentedCode) {
        checkArgument(!isNullOrEmpty(presentedCode), "Cannot infer null or empty code");

        Optional<TrackedOfferCode> trackedOfferCode = Optional.empty();
        String presentedCodeUpperCase = presentedCode.toUpperCase();
        String offerCode = presentedCodeUpperCase;
        if (presentedCodeUpperCase.indexOf(TrackedOfferCode.OFFER_CODE_DELIMITER) != -1) {
            trackedOfferCode = Optional.of(TrackedOfferCode.from(presentedCodeUpperCase));
            offerCode = trackedOfferCode.get().getOfferCode();
        }
        return new InferredOfferCode(offerCode, trackedOfferCode);
    }

    public String offerCode() {
        return offerCode;
    }

    public Optional<TrackedOfferCode> trackedOfferCode() {
        return trackedOfferCode;
    }

    public boolean isValid() {
        return !trackedOfferCode.isPresent()
                || hasAcceptableCheckDigit(trackedOfferCode.get().getTrackedOfferCode());
    }

    public static boolean isTrackedOfferCode(String offerCode) {
        InferredOfferCode inferred = InferredOfferCode.infer(offerCode);
        return inferred.trackedOfferCode().isPresent();
    }

    public static InferredOfferCode validatedInferredCode(String offerCode) {
        InferredOfferCode inferred = InferredOfferCode.infer(offerCode);

        if (!inferred.trackedOfferCode().isPresent()) {
            throw new InvalidOfferCodeException("Offer code %s is not a tracked offer", offerCode);
        }
        if (!inferred.isValid()) {
            throw new InvalidOfferCodeException("Offer code %s is invalid", offerCode);
        }
        return inferred;
    }
}

