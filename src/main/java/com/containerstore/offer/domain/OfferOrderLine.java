package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.containerstore.common.base.money.Money;
import com.containerstore.sku.domain.Sku;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.twoqubed.bob.annotation.Built;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.containerstore.common.base.Lists.nullToEmpty;
import static com.containerstore.offer.domain.AppliesTo.*;
import static com.containerstore.offer.domain.OfferOrderLines.WEB_CATEGORIES_SKU_ATTRIBUTE;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.stream.Collectors.toList;

@Built
public class OfferOrderLine {

    private static final Integer TCS_CLOSETS_DEPARTMENT = 4;
    private static final Integer PRESTON_DEPARTMENT = 25;
    private static final String INTEGER_PATTERN = "[0-9]+";
    private static final String UNSPECIFIED_SPACE_USE_ID = "NONE";

    // lineId is used by the client to link the returned adjustments back to the appropriate line.
    private String lineId;
    private long skuNumber;
    private int quantity;
    private Money preOfferPrice;
    private Sku sku;
    private Integer registryId;
    private String splitId;
    private String spaceUseId;
    private String spaceSource;
    private String spaceId;
    private Optional<AdditionalShippingFeeAdjustment> additionalShippingFeeAdjustment = Optional.empty();
    private final List<OrderLineAdjustment> lineAdjustments = new ArrayList<>();

    @RequiredBy("snakeyaml")
    private OfferOrderLine() {}

    @JsonCreator
    OfferOrderLine(
            @JsonProperty("lineId") String lineId,
            @JsonProperty("skuNumber") long skuNumber,
            @JsonProperty("sku") Sku sku,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("preOfferPrice") Money preOfferPrice,
            @JsonProperty("registryId") Integer registryId,
            @JsonProperty("splitId") String splitId,
            @JsonProperty("spaceUseId") String spaceUseId,
            @JsonProperty("spaceSource") String spaceSource,
            @JsonProperty("spaceId") String spaceId,
            @JsonProperty("lineAdjustments") List<OrderLineAdjustment> lineAdjustments,
            @JsonProperty("additionalShippingFeeAdjustment") AdditionalShippingFeeAdjustment
                    additionalShippingFeeAdjustment) {
        this.lineId = lineId;
        this.skuNumber = skuNumber;
        this.sku = sku;
        this.quantity = quantity;
        this.preOfferPrice = preOfferPrice;
        this.registryId = registryId;
        this.splitId = splitId;
        this.spaceUseId = firstNonNull(spaceUseId, UNSPECIFIED_SPACE_USE_ID);
        this.spaceSource = spaceSource;
        this.spaceId = spaceId;
        this.lineAdjustments.addAll(nullToEmpty(lineAdjustments));
        this.additionalShippingFeeAdjustment = Optional.ofNullable(additionalShippingFeeAdjustment);
    }

    public String getLineId() {
        return lineId;
    }

    public Optional<String> getSplitId() {
        return Optional.ofNullable(splitId);
    }

    public long getSkuNumber() {
        return skuNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getPreOfferPrice() {
        return preOfferPrice != null ? preOfferPrice : sku.getRegisterPrice();
    }

    public void setSku(Sku sku) {
        this.sku = sku;
    }

    public Sku getSku() {
        return sku;
    }

    public Optional<AdditionalShippingFeeAdjustment> getAdditionalShippingFeeAdjustment() {
        return additionalShippingFeeAdjustment;
    }

    public void setAdditionalShippingFeeAdjustment(AdditionalShippingFeeAdjustment additionalShippingFeeAdjustment) {
        this.additionalShippingFeeAdjustment = Optional.ofNullable(additionalShippingFeeAdjustment);
    }

    public Money extendedRetailAdditionalShippingFee() {
        if (sku == null || sku.getFeeAmount() == null) {
            return Money.ZERO;
        }

        return sku.getFeeAmount().multiply(quantity);
    }

    public Optional<Money> extendedAdditionalShippingFeeAdjustment() {
        return additionalShippingFeeAdjustment
                .map(AdditionalShippingFeeAdjustment::getAdjustmentPerUnit)
                .map(adjustment -> adjustment.multiply(quantity));
    }

    public Optional<Integer> getRegistryId() {
        return Optional.ofNullable(registryId);
    }

    public void addLineAdjustment(OrderLineAdjustment lineAdjustment) {
        this.lineAdjustments.add(lineAdjustment);
    }

    public List<OrderLineAdjustment> getLineAdjustments() {
        return ImmutableList.copyOf(lineAdjustments);
    }

    public Money amount() {
        return getPreOfferPrice().multiply(quantity);
    }

    public Money amountWithAdjustments() {
        return priceWithAdjustments().multiply(quantity);
    }

    public Money priceWithAdjustments() {
        return lineAdjustments
                .stream()
                .map(OrderLineAdjustment::getUnitAdjustment)
                .reduce(getPreOfferPrice(), Money::add);
    }

    public boolean hasFixedPriceAdjustment() {
        return lineAdjustments.stream()
                .anyMatch(OrderLineAdjustment::isFixedPriceAdjustment);
    }

    public List<Integer> getWebCategories() {
        return skuAttributeValue(WEB_CATEGORIES_SKU_ATTRIBUTE)
                .map(this::toCategoryIds)
                .orElse(new ArrayList<>());
    }

    public boolean isPricedAtRetail() {
        return getSku().getRetailPrice().equals(priceWithAdjustments());
    }

    public boolean isInDepartment(Integer departmentId) {
        return getSku() != null
                && Objects.equals(getSku().getDepartmentId(), departmentId);
    }

    public boolean canReceiveOffer(Offer offer) {
        return offer.combinedAdjustmentBenefits().stream()
                .anyMatch(this::canReceiveBenefit);
    }

    public boolean canReceiveBenefit(AdjustmentBenefit benefit) {
        return isSpecialtyItem() && getLineAppliesTo() == benefit.getAppliesTo()
                || !isSpecialtyItem() && !benefit.appliesToSpecialtyItem();
    }

    private boolean isSpecialtyItem() {
        return isInDepartment(TCS_CLOSETS_DEPARTMENT) || isInDepartment(PRESTON_DEPARTMENT);
    }

    private AppliesTo getLineAppliesTo() {
        if (isInDepartment(TCS_CLOSETS_DEPARTMENT)) {
            return TCS_CLOSETS;
        } else if (isInDepartment(PRESTON_DEPARTMENT)) {
            return PRESTON;
        } else {
            return LINE;
        }
    }

    public String getSpaceUseId() {
        return spaceUseId;
    }

    public String getSpaceSource() {
        return spaceSource;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public boolean isFromEmployeeDesignedSpace() {
        return CpcSpaceSource.isEmployeeDesignedSource(spaceSource);
    }

    public boolean isFromDesignedElfaSpace() {
        return CpcSpaceSource.isDesignedElfaSource(spaceSource);
    }

    public static OfferOrderLineBuilder builder() {
        return OfferOrderLineBuilder.builder();
    }

    @Override
    public String toString() {
        return toStringHelper(this.getClass())
                .add("lineId", lineId)
                .add("splitId", splitId)
                .add("skuNumber", skuNumber)
                .add("quantity", quantity)
                .add("preOfferPrice", getPreOfferPrice())
                .add("registryId", registryId)
                .add("lineAdjustments", lineAdjustments)
                .add("additionalShippingFeeAdjustment", additionalShippingFeeAdjustment)
                .add("spaceId", spaceId)
                .add("spaceUseId", spaceUseId)
                .add("spaceSource", spaceSource)
                .toString();
    }

    private Optional<String> skuAttributeValue(String skuAttribute) {
        return sku == null
                ? Optional.empty()
                : Optional.ofNullable(sku.getSkuAttributes().get(skuAttribute));
    }

    private List<Integer> toCategoryIds(String commaSeparatedList) {
        return Splitter
                .on(',')
                .omitEmptyStrings()
                .trimResults()
                .splitToList(commaSeparatedList)
                .stream()
                .filter(s -> s.matches(INTEGER_PATTERN))
                .map(Integer::valueOf)
                .collect(toList());
    }
}
