package com.containerstore.offer.domain;

import com.containerstore.common.base.checkdigit.Mod37CheckDigit;
import com.containerstore.offer.service.TrackedOfferCode;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class CheckDigitValidator {

    private static final Mod37CheckDigit VALIDATOR = new Mod37CheckDigit();
    private static final List<Character> LOOKS_LIKE_ZERO = asList('0', 'O', 'Q');
    private static final List<Character> LOOKS_LIKE_ONE = asList('1', 'I');

    private CheckDigitValidator() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasValidCheckDigit(String offerCode) {
        if (!isNullOrEmpty(offerCode)) {
            return VALIDATOR.validateCheckDigit(offerCode);
        }
        return false;
    }

    public static boolean hasAcceptableCheckDigit(String offerCode) {
        if (!isNullOrEmpty(offerCode)) {
            for (String permutation : offerCodePermutations(offerCode)) {
                if (VALIDATOR.validateCheckDigit(permutation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAcceptableCheckDigit(char checkDigit) {
        return TrackedOfferCode.OFFER_CODE_CHARACTER_LIST.indexOf(checkDigit) != -1;
    }

    public static char getCheckDigitFromOfferCode(String offerCode) {
        checkArgument(!isNullOrEmpty(offerCode), "offerCode is required.");
        return offerCode.charAt(offerCode.length() - 1);
    }

    private static List<String> offerCodePermutations(String offerCode) {
        char checkDigit = getCheckDigitFromOfferCode(offerCode);
        if (LOOKS_LIKE_ZERO.contains(checkDigit)) {
            return permutationsFor(offerCode, LOOKS_LIKE_ZERO);
        }
        if (LOOKS_LIKE_ONE.contains(checkDigit)) {
            return permutationsFor(offerCode, LOOKS_LIKE_ONE);
        }
        return singletonList(offerCode);
    }

    private static List<String> permutationsFor(String offerCode, List<Character> checkDigits) {
        List<String> permutations = new ArrayList<>();
        for (Character checkDigit : checkDigits) {
            permutations.add(replaceCheckDigit(offerCode, checkDigit));
        }
        return permutations;
    }

    @VisibleForTesting
    static String replaceCheckDigit(String offerCode, Character checkDigit) {
        return offerCode.substring(0, offerCode.length() - 1).concat(checkDigit.toString());
    }
}
