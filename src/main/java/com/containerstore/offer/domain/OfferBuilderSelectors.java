package com.containerstore.offer.domain;

import com.google.common.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.containerstore.common.base.Optionals.firstPresent;
import static com.containerstore.offer.domain.OfferBuilders.*;
import static java.util.stream.Collectors.toList;

public final class OfferBuilderSelectors {

    static final int MINIMUM_DAYS_LEFT_TO_ALLOW_REPLENISHMENT = 5;

    private OfferBuilderSelectors() {
        throw new UnsupportedOperationException();
    }

    public static Optional<OfferBuilder> bestTrackedOffer(List<OfferBuilder> offers) {
        if (offers.isEmpty()) {
            return Optional.empty();
        }
        return firstPresent(
                activeOffer(offers, endingLatest()),
                aboutToStart(offers, startingSoonest()),
                recentlyEnded(offers, endingLatest()));
    }

    public static Optional<OfferBuilder> bestReplenishableOffer(List<OfferBuilder> offers) {
        return usableOfferWithMinimumLifeRemaining(offers, endingLatest());
    }

    public static Optional<OfferBuilder> bestRecentOffer(List<OfferBuilder> offers) {
        return firstPresent(
                unexpiredOffer(offers, endingSoonest()),
                recentlyEnded(offers, endingLatest()));
    }

    static Optional<OfferBuilder> aboutToStart(
            List<OfferBuilder> offerBuilders,
            Comparator<OfferBuilder> sortedBy) {
        List<OfferBuilder> aboutToStart = offerBuilders.stream()
                .filter(hasStartDate().and(started().negate()))
                .collect(toList());
        return firstOf(aboutToStart, sortedBy);
    }

    static Optional<OfferBuilder> activeOffer(
            List<OfferBuilder> offerBuilders,
            Comparator<OfferBuilder> sortedBy) {
        List<OfferBuilder> activeOffers = offerBuilders.stream()
                .filter(started().and(ended().negate()))
                .collect(toList());
        return firstOf(activeOffers, sortedBy);
    }

    static Optional<OfferBuilder> unexpiredOffer(
            List<OfferBuilder> offerBuilders,
            Comparator<OfferBuilder> sortedBy) {
        List<OfferBuilder> unexpiredOffers = offerBuilders.stream()
                .filter(ended().negate())
                .collect(toList());
        return firstOf(unexpiredOffers, sortedBy);
    }

    static Optional<OfferBuilder> usableOfferWithMinimumLifeRemaining(
            List<OfferBuilder> offerBuilders,
            Comparator<OfferBuilder> sortedBy) {
        List<OfferBuilder> usableOffers = offerBuilders.stream()
                .filter(hasReachedAllottedUsages().negate()
                        .and(started())
                        .and(expiresSoon(MINIMUM_DAYS_LEFT_TO_ALLOW_REPLENISHMENT).negate()))
                .collect(toList());
        return firstOf(usableOffers, sortedBy);
    }

    static Optional<OfferBuilder> recentlyEnded(
            List<OfferBuilder> offerBuilders,
            Comparator<OfferBuilder> sortedBy) {
        List<OfferBuilder> offersThatHaveEnded = offerBuilders.stream()
                .filter(ended())
                .collect(toList());
        return firstOf(offersThatHaveEnded, sortedBy);
    }

    private static Optional<OfferBuilder> firstOf(
            List<OfferBuilder> offerBuilders,
            Comparator<OfferBuilder> comparator) {
        return offerBuilders.stream()
                .sorted(comparator)
                .findFirst();
    }

    /**
     * Sorts offer builders by end date ascending with nulls at the end
     */
    @VisibleForTesting
    static Comparator<OfferBuilder> endingSoonest() {
        return (first, second) -> {
            if (first.getEndDateTime() == null && second.getEndDateTime() == null) {
                return 0;
            }
            if (first.getEndDateTime() == null) {
                return 1;
            }
            if (second.getEndDateTime() == null) {
                return -1;
            }
            return first.getEndDateTime().compareTo(second.getEndDateTime());
        };
    }

    /**
     * Sorts offer builders by end date descending with nulls at the front
     */
    @VisibleForTesting
    static Comparator<OfferBuilder> endingLatest() {
        return (first, second) -> {
            if (first.getEndDateTime() == null && second.getEndDateTime() == null) {
                return 0;
            }
            if (first.getEndDateTime() == null) {
                return -1;
            }
            if (second.getEndDateTime() == null) {
                return 1;
            }
            return second.getEndDateTime().compareTo(first.getEndDateTime());
        };
    }

    /**
     * Sorts offer builders by start date ascending with nulls at the end
     */
    @VisibleForTesting
    static Comparator<OfferBuilder> startingSoonest() {
        return (first, second) -> {
            if (first.getStartDateTime() == null && second.getStartDateTime() == null) {
                return 0;
            }
            if (first.getStartDateTime() == null) {
                return 1;
            }
            if (second.getStartDateTime() == null) {
                return -1;
            }
            return first.getStartDateTime().compareTo(second.getStartDateTime());
        };
    }

}
