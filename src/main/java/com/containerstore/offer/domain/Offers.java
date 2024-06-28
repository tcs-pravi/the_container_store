package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import com.containerstore.common.base.time.SystemTime;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.containerstore.common.base.BigDecimals.ONE_HUNDRED;
import static com.containerstore.common.thirdparty.mvel.MvelHelper.evaluateExpression;
import static com.containerstore.offer.domain.AdjustmentBenefits.withAdjustmentType;
import static com.containerstore.offer.domain.AdjustmentType.AMOUNT_OFF;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.math.RoundingMode.HALF_UP;

public final class Offers {

    public static final int ANY_BRICK_AND_MORTAR_STORE = 0;
    public static final int CSD_STORE = 16;
    public static final int WEB_STORE = 899;
    public static final DateTimeZone CENTRAL_DATE_TIME_ZONE = DateTimeZone.forID("America/Chicago");
    private static final int END_DATE_EXTENDED_HOURS_TO_ACCOMMODATE_WESTMOST_STATES = 5;
    private static final int SCALE_TO_SUPPORT_PERCENTAGE_OFF_UP_TO_FOUR_DECIMAL_PLACES = 6;
    private static final String MVEL_OFFER_ORDER_VARIABLE_NAME = "order";
    private static final String MVEL_OFFER_VARIABLE_NAME = "offer";
    private static final String MVEL_FULFILLMENT_GROUP_VARIABLE_NAME = "fulfillmentGroup";



    private Offers() {
        throw new UnsupportedOperationException();
    }

    public static Predicate<Offer> forStore(final Integer storeId) {
        return offer -> {
            if (isBrickAndMortarStore(storeId) && offer.getOfferStores().contains(ANY_BRICK_AND_MORTAR_STORE)) {
                return true;
            }
            return offer.getOfferStores().contains(storeId);
        };
    }

    public static Predicate<Offer> forCustomer(final OfferCustomer customer) {
        return offer -> {
            if (isNullOrEmpty(offer.getCustomerQualifications())) {
                return true;
            }
            return customerQualifiesForOffer(customer, offer.getCustomerQualifications());
        };
    }

    public static Predicate<OfferBuilder> validForCustomer(final OfferCustomer customer) {
        return builder -> {
            if (isNullOrEmpty(builder.getCustomerQualifications())) {
                return true;
            }
            return customerQualifiesForOffer(customer, builder.getCustomerQualifications());
        };
    }

    public static Predicate<Offer> isGlobal() {
        return offer -> offer.getDeliveryMethod().equals(DeliveryMethod.GLOBAL) && !offer.isTracked();
    }

    public static Predicate<Offer> withOfferCode(final String offerCode) {
        return offer -> offer.getOfferCode().equals(offerCode);
    }

    public static Predicate<Offer> withDeliveryMethod(final DeliveryMethod deliveryMethod) {
        return offer -> offer.getDeliveryMethod().equals(deliveryMethod);
    }

    public static boolean hasStarted(Offer offer) {
        DateTime now = SystemTime.asDateTime();
        return offer.getStartDateTime() != null
                && (offer.getStartDateTime().isEqual(now) || offer.getStartDateTime().isBefore(now));
    }

    public static boolean hasEnded(Offer offer) {
        DateTime now = SystemTime.asDateTime();
        return offer.getEndDateTime() != null
                && (offer.getEndDateTime().isEqual(now) || offer.getEndDateTime().isBefore(now));
    }

    public static boolean customerQualifiesForOffer(OfferCustomer customer, String qualifications) {
        return evaluateExpression(qualifications, ImmutableMap.<String, Object>builder()
                .put("customer", customer).build());
    }

    public static boolean hasReachedAllottedUsages(Offer offer) {
        return offer.isSingleUse() && firstNonNull(offer.getRedemptionCount(), 0) > 0;
    }

    public static boolean isValidForStore(Offer offer, Integer storeNumber) {
        return Offers.forStore(storeNumber).test(offer);
    }

    public static Money percentageOffPrice(Money currentPrice, BigDecimal percentage) {
        Money discount = percentageOffDiscountAmount(currentPrice, percentage);
        return currentPrice.subtract(discount);
    }

    public static Money fixedPrice(AdjustmentBenefit benefit, Money defaultPrice) {
        if (benefit.getAdjustmentValue() == null) {
            return defaultPrice;
        } else {
            return new Money(benefit.getAdjustmentValue());
        }
    }

    public static Money percentageOffDiscountAmount(Money currentPrice, BigDecimal percentage) {
        return  currentPrice.multiply(percentageAsDecimal(percentage)).asMoney();
    }

    public static Money amountOff(Offer offer) {
        return offer.combinedAdjustmentBenefits()
                .stream()
                .filter(withAdjustmentType(AMOUNT_OFF))
                .map(b -> new Money(Optional.ofNullable(b.getAdjustmentValue()).orElse(BigDecimal.ZERO)))
                .reduce(Money.ZERO, Money::add);
    }

    /**
     * Returns an offer's end date as a <code>LocalDate</code>, representing the
     * last date an offer is valid; for display purposes only.
     * <p>
     * In some cases, this is simply the offer's end date (relative to central time)
     * with the time portion removed. Frequently, however, an Offer-Services administrator
     * will set offers to expire exactly at 5 a.m. central time to allow customers in westmost
     * states (e.g. Hawaii and Alaska) to use the offer at reasonable times
     * (i.e. before midnight or 1 a.m. their local time).
     * <p>
     * In these cases the implied end date of the offer is the previous day.
     * For example, an offer expiring at 5 a.m. August 5, 2014 central time
     * essentially expires August 4, 2014.
     *
     * @param endDateTime  the end date time of an offer
     * @return an adjusted LocalDate representing the implied end date of the offer
     * for displaying purposes
     */
    public static LocalDate impliedEndDate(DateTime endDateTime) {
        DateTime centralDateTime = endDateTime.toDateTime(CENTRAL_DATE_TIME_ZONE);
        LocalDate impliedEndDate = new LocalDate(
                centralDateTime.getYear(), centralDateTime.getMonthOfYear(), centralDateTime.getDayOfMonth());
        if (betweenMidnightAndSixInclusive(centralDateTime)) {
            impliedEndDate = impliedEndDate.minusDays(1);
        }
        return impliedEndDate;
    }

    /**
     * Returns an offer end date extended until 5 a.m. the next day
     * to accommodate customers living in westmost states (e.g. Hawaii or Alaska).
     *
     * @param dateTime the end DateTime of an offer
     * @return an end date time adjusted to 5 a.m. the next day
     */
    public static DateTime extendedEndDateTime(DateTime dateTime) {
        return dateTime.toDateTime(CENTRAL_DATE_TIME_ZONE)
                .dayOfMonth().roundCeilingCopy()
                .plusHours(END_DATE_EXTENDED_HOURS_TO_ACCOMMODATE_WESTMOST_STATES);
    }

    public static Predicate<Offer> offerIsValid(final OfferOrder order) {
        return offer -> {
            if (order.isEmployeeTransaction() && !offer.isEmployeeEligible()) {
                return false;
            }
            if (!customerQualifiesForOffer(order.getPurchasingCustomer(), offer.getCustomerQualifications())) {
                return false;
            }
            return orderQualifiesForOffer(order, offer);
        };
    }

    public static boolean offerOrderQualificationsAreMet(OfferOrder order, Offer offer) {
        return evaluateExpression(offer.getOrderQualifications(),
                ImmutableMap.<String, Object> builder()
                        .put(MVEL_OFFER_ORDER_VARIABLE_NAME, order)
                        .put(MVEL_OFFER_VARIABLE_NAME, offer)
                        .build());
    }

    public static boolean orderFullyQualifiesForOffer(OfferOrder order, Offer offer) {
        if (offer.getOfferScope().equals(OfferScope.FULFILLMENT_GROUP)) {
            for (OfferOrderFulfillmentGroup fulfillmentGroup : order.getOfferOrderFulfillmentGroups()) {
                if (fulfillmentGroupMeetsOrderQualifications(fulfillmentGroup, offer, order)) {
                    return true;
                }
            }
            return false;
        } else if (offer.getOfferScope().equals(OfferScope.ORDER)) {
            return evaluateExpression(offer.getOrderQualifications(),
                    ImmutableMap.<String, Object>builder()
                            .put(MVEL_OFFER_ORDER_VARIABLE_NAME, order)
                            .put(MVEL_OFFER_VARIABLE_NAME, offer)
                            .build());
        }

        return false;
    }

    public static boolean fulfillmentGroupMeetsOrderQualifications(OfferOrderFulfillmentGroup fulfillmentGroupToQualify,
                                                                   Offer offer,
                                                                   OfferOrder order) {
        return evaluateExpression(offer.getOrderQualifications(),
                ImmutableMap.<String, Object>builder()
                        .put(MVEL_OFFER_ORDER_VARIABLE_NAME, order)
                        .put(MVEL_FULFILLMENT_GROUP_VARIABLE_NAME, fulfillmentGroupToQualify)
                        .put(MVEL_OFFER_VARIABLE_NAME, offer)
                        .build());
    }

    public static boolean fulfillmentGroupQualifiesForOffer(OfferOrderFulfillmentGroup fulfillmentGroupToQualify,
                                                            Offer offer,
                                                            OfferOrder order) {
        if (offer.getOfferScope() == OfferScope.FULFILLMENT_GROUP) {
            return fulfillmentGroupMeetsOrderQualifications(fulfillmentGroupToQualify, offer, order);
        }
        // if it is not a fg scoped offer, fg qualifies
        return true;
    }

    public static boolean orderQualifiesForOffer(OfferOrder order, Offer offer) {
        if (offer.getOfferScope() == OfferScope.ORDER) {
            return offerOrderQualificationsAreMet(order, offer);
        }
        // if it is not an order scoped offer, order qualifies
        return true;
    }

    public static Predicate<Offer> hasEarnedOfferBenefits() {
        return offer -> !offer.getEarnedOfferBenefits().isEmpty();
    }

    public static Function<DecliningBalanceBenefit, AdjustmentBenefit> toAmountOffBenefit() {
        return benefit -> {
            ImmutableAdjustmentBenefit.Builder builder = ImmutableAdjustmentBenefit.builder()
                    .withId(benefit.getId())
                    .withAdjustmentType(AdjustmentType.AMOUNT_OFF)
                    .withAppliesTo(AppliesTo.LINE)
                    .withAdjustmentValue(benefit.getCurrentValue().getAmount());
            benefit.getAppliesToRule().ifPresent(builder::withAppliesToRule);
            return builder.build();
        };
    }

    public static Optional<OrderKey> sourceOrderFor(Offer redeemedOffer) {
        return redeemedOffer.getSourceOrders()
                .stream()
                .findFirst();
    }

    public static String withLineBreaks(String text, int lineLength, String lineBreak) {
        return text.replaceAll("\\s+", " ")
                    .replaceAll(String.format(" *(.{1,%d})(?=$| ) *", lineLength), "$1" + lineBreak);
    }

    private static BigDecimal percentageAsDecimal(BigDecimal percentage) {
        return percentage.divide(ONE_HUNDRED, SCALE_TO_SUPPORT_PERCENTAGE_OFF_UP_TO_FOUR_DECIMAL_PLACES, HALF_UP);
    }

    private static boolean betweenMidnightAndSixInclusive(DateTime dateTime) {
        return dateTime.getHourOfDay() < 6
                || (dateTime.getHourOfDay() == 6 && dateTime.getMinuteOfHour() == 0);
    }

    private static boolean isBrickAndMortarStore(Integer storeId) {
        return CSD_STORE != storeId && WEB_STORE != storeId;
    }
}
