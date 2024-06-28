package com.containerstore.offer.domain;

import com.google.common.annotations.VisibleForTesting;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.containerstore.offer.domain.DeliveryMethod.GLOBAL;
import static com.google.common.base.MoreObjects.firstNonNull;

public final class OfferBuilders {

    private OfferBuilders() {
        throw new UnsupportedOperationException();
    }

    public static Predicate<OfferBuilder> isActive() {
        return OfferBuilder::isActive;
    }

    public static Predicate<OfferBuilder> isGlobal() {
        return builder -> builder.getDeliveryMethod() == GLOBAL;
    }

    public static Predicate<OfferBuilder> hasReachedAllottedUsages() {
        return builder -> builder.isSingleUse() && firstNonNull(builder.getRedemptionCount(), 0) > 0;
    }

    public static Function<OfferBuilder, Offer> toOffer() {
        return OfferBuilder::build;
    }

    @VisibleForTesting
    static Predicate<OfferBuilder> hasStartDate() {
        return builder -> builder.getStartDateTime() != null;
    }

    static Predicate<OfferBuilder> started() {
        return OfferBuilder::hasStarted;
    }

    static Predicate<OfferBuilder> ended() {
        return OfferBuilder::hasEnded;
    }

    static Predicate<OfferBuilder> expiresSoon(int days) {
        return builder -> builder.expiresSoon(days);
    }

}
