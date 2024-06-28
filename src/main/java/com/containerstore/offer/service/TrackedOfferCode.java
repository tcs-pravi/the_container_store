package com.containerstore.offer.service;

import com.containerstore.common.base.checkdigit.Mod37CheckDigit;
import com.containerstore.offer.exception.InvalidOfferCodeException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class TrackedOfferCode {

    public static final String OFFER_CODE_CHARACTER_LIST = "23456789BCDEFGHJKLMNPRSTVWXY";
    public static final char OFFER_CODE_DELIMITER = 'Z';
    private final String offerCode;
    private final String uniquePart;
    private final String trackedOfferCode;

    @JsonCreator
    private TrackedOfferCode(
            @JsonProperty("offerCode") String offerCode,
            @JsonProperty("uniquePart") String uniquePart,
            @JsonProperty("trackedOfferCode") String trackedOfferCode) {
        checkArgument(!isNullOrEmpty(offerCode), "Offer code can not be empty or null.");
        checkArgument(!isNullOrEmpty(uniquePart), "Unique part can not be empty or null.");
        checkArgument(!isNullOrEmpty(trackedOfferCode), "Tracked offer code can not be empty or null.");

        this.trackedOfferCode = trackedOfferCode;
        this.uniquePart = uniquePart;
        this.offerCode = offerCode;
    }

    public static TrackedOfferCode from(String presentedCode) {
        String trackedOfferCode = presentedCode.toUpperCase();
        Iterator<String> splitParts = Splitter.on(TrackedOfferCode.OFFER_CODE_DELIMITER)
                .omitEmptyStrings()
                .trimResults()
                .limit(2)
                .split(trackedOfferCode)
                .iterator();
        String offerCode = splitParts.next();
        if (!splitParts.hasNext()) {
            throw new InvalidOfferCodeException("Offer code %s is not a tracked offer", presentedCode);
        }
        String uniquePart = removeCheckDigit(splitParts.next());
        return new TrackedOfferCode(offerCode, uniquePart, trackedOfferCode);
    }

    private static String removeCheckDigit(String str) {
        return str.substring(0, str.length() - 1);
    }

    public static TrackedOfferCode constructTrackedOfferCode(String offerCode, String uniquePart) {
        String base = offerCode + OFFER_CODE_DELIMITER + uniquePart;
        return from(base + checkDigitFor(base));
    }

    @VisibleForTesting
    public static char checkDigitFor(String baseString) {
        return new Mod37CheckDigit().calculateCheckDigitFor(baseString);
    }

    public String getOfferCode() {
        return offerCode;
    }

    public String getUniquePart() {
        return uniquePart;
    }

    public String getTrackedOfferCode() {
        return trackedOfferCode;
    }
}
