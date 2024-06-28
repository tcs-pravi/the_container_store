package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.containerstore.common.base.BigDecimals.ONE_HUNDRED;
import static java.math.RoundingMode.HALF_UP;

public final class OfferOrderLines {

    public static final String WEB_CATEGORIES_SKU_ATTRIBUTE = "WEB_CATEGORIES";

    private static final Logger LOGGER = Logger.getLogger(OfferOrderLines.class);

    private OfferOrderLines() {
        throw new UnsupportedOperationException();
    }

    public static Money sum(List<OfferOrderLine> lines) {
        return lines.stream()
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add);
    }

    public static Predicate<OfferOrderLine> withDepartmentId(Integer departmentId) {
        return line -> line.isInDepartment(departmentId);
    }

    public static Predicate<OfferOrderLine> withWebCategoryId(int categoryId) {
        return withWebCategoryIds(categoryId);
    }

    public static Predicate<OfferOrderLine> withWebCategoryIds(Integer... categoryIds) {
        return line -> !Collections.disjoint(Arrays.asList(categoryIds), line.getWebCategories());
    }

    public static Predicate<OfferOrderLine> withSkus(Long... skuNumbers) {
        return line -> Arrays.asList(skuNumbers).contains(line.getSkuNumber());
    }

    public static Predicate<OfferOrderLine> withSkuLists(Offer offer, String... skuListIds) {
        return line -> Arrays.asList(skuListIds).stream()
                .map(skuList -> offer.getSkuLists().get(skuList))
                .filter(skus -> !CollectionUtils.isEmpty(skus))
                .anyMatch(skus -> skus.contains(line.getSkuNumber()));
    }

    public static Predicate<OfferOrderLine> withRegistryId() {
        return line -> line.getRegistryId().isPresent();
    }

    public static Predicate<OfferOrderLine> withSpaceUseIds(String... spaceUseIds) {
        return line -> Arrays.asList(spaceUseIds).contains(line.getSpaceUseId());
    }

    public static Predicate<OfferOrderLine> fromEmployeeDesignedSpace() {
        return OfferOrderLine::isFromEmployeeDesignedSpace;
    }

    public static Predicate<OfferOrderLine> fromDesignedElfaSpace() {
        return OfferOrderLine::isFromDesignedElfaSpace;
    }

    public static Function<OfferOrderLine, Money> extendedAmount() {
        return OfferOrderLine::amount;
    }

    public static Function<OfferOrderLine, Money> amountWithAdjustments() {
        return OfferOrderLine::amountWithAdjustments;
    }

    public static Comparator<OfferOrderLine> byPriceDescending() {
        return (first, second) -> second.priceWithAdjustments().compareTo(first.priceWithAdjustments());
    }

    public static Comparator<OfferOrderLine> byPreOfferPriceDescending() {
        return (first, second) -> second.getPreOfferPrice().compareTo(first.getPreOfferPrice());
    }

    public static Comparator<OfferOrderLine> byPreOfferPriceAscending() {
        return (first, second) -> first.getPreOfferPrice().compareTo(second.getPreOfferPrice());
    }

    public static Comparator<OfferOrderLine> byLineIdAscending() {
        return Comparator.comparing(OfferOrderLine::getLineId);
    }

    public static Comparator<OfferOrderLine> byLineIdDescending() {
        return Comparator.comparing(OfferOrderLine::getLineId).reversed();
    }

    public static Comparator<OfferOrderLine> bySkuNumberAscending() {
        return Comparator.comparingLong(OfferOrderLine::getSkuNumber);
    }

    public static Comparator<OfferOrderLine> byQuantityDescending() {
        return Comparator.comparingLong(OfferOrderLine::getQuantity).reversed();
    }

    public static Predicate<OfferOrderLine> linePriceCanBeDiscounted() {
        return fixedPriceItem().negate()
                .and(zeroPricedItem().negate());
    }

    public static Predicate<OfferOrderLine> linePriceCanBeGivenFixedPrice() {
        return zeroPricedItem().negate();
    }

    public static Predicate<OfferOrderLine> canReceiveOffer(Offer offer) {
        return line -> line.canReceiveOffer(offer);
    }

    public static Predicate<OfferOrderLine> canReceivePercentOffAdjustment(Offer offer) {
        return canReceiveOffer(offer)
                .and(linePriceCanBeDiscounted());
    }

    public static Predicate<OfferOrderLine> canReceiveFixedPriceAdjustment(Offer offer) {
        return canReceiveOffer(offer)
                .and(linePriceCanBeGivenFixedPrice());
    }

    public static Predicate<OfferOrderLine> zeroPricedItem() {
        return line -> line.getPreOfferPrice().isZero()
                || line.getSku().getRetailPrice().isLessThanOrEqualTo(Money.ZERO);
    }

    public static Predicate<OfferOrderLine> fixedPriceItem() {
        return OfferOrderLine::hasFixedPriceAdjustment;
    }

    public static Predicate<OfferOrderLine> withPercentageOffBetter(final BigDecimal percentage) {
        return line -> {
            BigDecimal retailPrice = line.getSku().getRetailPrice().get();
            if (retailPrice.compareTo(BigDecimal.ZERO) < 1) {
                LOGGER.warn(String.format("Invalid retail price (%s) for sku %s",
                        retailPrice, line.getSku().getSkuNumber()));
                return false;
            }
            BigDecimal differenceFromRetail = retailPrice.subtract(line.getPreOfferPrice().get());
            BigDecimal salePercentage = differenceFromRetail.divide(retailPrice, 4, HALF_UP).multiply(ONE_HUNDRED);
            return percentage.compareTo(salePercentage) > 0;
        };
    }

    public static Predicate<OfferOrderLine> withFixedPriceBetter(final BigDecimal fixedPrice) {
        return line -> {
            BigDecimal preOfferPrice = line.getPreOfferPrice().get();
            return preOfferPrice.compareTo(fixedPrice) > 0;
        };
    }

    public static Predicate<OfferOrderLine> benefitAppliesToLine(
            final Offer offer,
            final AdjustmentBenefit benefit,
            final OfferOrder order) {
        return line -> benefit.isRuleSatisfiedBy(offer, order, line);
    }

    public static OfferOrderLine createLineWithQuantity(OfferOrderLine offerOrderLine, int quantity) {
        // Calling the constructor directly to ensure all fields are copied to the new line.
        return new OfferOrderLine(
                offerOrderLine.getLineId(),
                offerOrderLine.getSkuNumber(),
                offerOrderLine.getSku(),
                quantity,
                offerOrderLine.getPreOfferPrice(),
                offerOrderLine.getRegistryId().orElse(null),
                UUID.randomUUID().toString(),
                offerOrderLine.getSpaceUseId(),
                offerOrderLine.getSpaceSource(),
                offerOrderLine.getSpaceId(),
                new ArrayList<>(offerOrderLine.getLineAdjustments()),
                offerOrderLine.getAdditionalShippingFeeAdjustment().orElse(null)
        );

    }
}
