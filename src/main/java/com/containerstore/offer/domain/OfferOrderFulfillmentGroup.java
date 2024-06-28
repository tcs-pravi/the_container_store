package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.containerstore.common.base.money.Money;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.twoqubed.bob.annotation.Built;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.containerstore.common.base.Lists.nullToEmpty;
import static com.containerstore.offer.domain.ClosetDepartments.ELFA_DEPARTMENT;
import static com.containerstore.offer.domain.OfferOrderLines.*;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

@Built
public class OfferOrderFulfillmentGroup {

    private String fulfillmentGroupId;
    private FulfillmentGroupType fulfillmentGroupType;
    private ShippingMethod shippingMethod;
    private String shippingState;
    private final List<FulfillmentGroupFeeAdjustment> feeAdjustments = new ArrayList<>();
    private FulfillmentGroupFeeAdjustment shippingAdjustment;
    private final List<OfferOrderLine> offerOrderLines = new ArrayList<>();
    private final List<Fee> fees = new ArrayList<>();

    @RequiredBy("Unmarshalling")
    @SuppressWarnings("squid:S1186") // Required by unmarshalling
    public OfferOrderFulfillmentGroup() {}

    OfferOrderFulfillmentGroup(
            String fulfillmentGroupId,
            FulfillmentGroupType fulfillmentGroupType,
            ShippingMethod  shippingMethod,
            String shippingState,
            List<OfferOrderLine> offerOrderLines,
            List<Fee> fees) {
        this.fulfillmentGroupId = fulfillmentGroupId;
        this.fulfillmentGroupType = fulfillmentGroupType;
        this.shippingMethod = shippingMethod;
        this.shippingState = shippingState;
        this.offerOrderLines.addAll(nullToEmpty(offerOrderLines));
        this.fees.addAll(nullToEmpty(fees));
    }

    public void addOfferOrderLine(OfferOrderLine offerOrderLine) {
        checkArgument(!isNullOrEmpty(offerOrderLine.getLineId()), "OfferOrderLine must have an id.");
        checkArgument(!lineWithIdExists(offerOrderLine.getLineId()), "Line with id already exists.");
        offerOrderLines.add(offerOrderLine);
    }

    public List<OfferOrderLine> getOfferOrderLines() {
        return ImmutableList.copyOf(offerOrderLines);
    }

    public void setOfferOrderLines(List<OfferOrderLine> offerOrderLines) {
        this.offerOrderLines.clear();
        for (OfferOrderLine offerOrderLine : offerOrderLines) {
            if (lineWithIdExists(offerOrderLine.getLineId())) {
                throw new IllegalArgumentException("List contains one or more lines with the same id.");
            }
            this.offerOrderLines.add(offerOrderLine);
        }
    }

    public FulfillmentGroupType getFulfillmentGroupType() {
        return fulfillmentGroupType;
    }

    private boolean lineWithIdExists(String id) {
        for (OfferOrderLine offerOrderLine : offerOrderLines) {
            if (id.equals(offerOrderLine.getLineId())) {
                return true;
            }
        }
        return false;
    }

    public String getFulfillmentGroupId() {
        return fulfillmentGroupId;
    }

    public void setFulfillmentGroupId(String fulfillmentGroupId) {
        this.fulfillmentGroupId = fulfillmentGroupId;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public String getShippingState() {
        return shippingState;
    }

    /**
     * returns the fulfillment group total after applying all line and fee adjustments
     */
    public Money total() {
        return getTotal();
    }

    /**
     * returns the fulfillment group total after applying all line and fee adjustments
     *
     * Note: this method signature exists for MVEL simplification
     * Example: fulfillmentGroup.total.isGreaterThanOrEqualTo(toMoney(100B))
     */
    public Money getTotal() {
        return getTotalAmountAfterAdjustments();
    }

    public Money getTotalAmountAfterAdjustments() {
        return offerOrderLines.stream()
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add)
                .add(getShippingAdjustmentTotal())
                .add(getTotalFeeAmountAfterAdjustments());
    }

    public Money getTotalAmountBeforeAdjustments() {
        return offerOrderLines.stream()
                .map(extendedAmount())
                .reduce(Money.ZERO, Money::add)
                .add(getTotalFeeAmountBeforeAdjustments());
    }

    public Money totalForFeeTypeBeforeAdjustments(String feeType) {
        return getFees().stream()
                .filter(f -> feeType.equals(f.getType()))
                .map(Fee::getPreOfferAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money totalForFeeTypeAfterAdjustments(String feeType) {
        return getFees().stream()
                .filter(f -> feeType.equals(f.getType()))
                .map(this::feeAmountAfterAdjustments)
                .reduce(Money.ZERO, Money::add);
    }

    /**
     * Note: this method signature exists for MVEL simplification
     * Example: fulfillment.merchandiseTotal.isGreaterThanOrEqualTo(toMoney(100B))
     */
    public Money getMerchandiseTotal() {
        return getMerchandiseTotalAfterAdjustments();
    }

    /**
     * returns the total of merchandise exclusively.
     * This differs from totalAfterAdjustments in that it will omit any fees. (e.g. shipping)
     * Because it is After Adjustments It will include any line adjustments.
     */
    public Money getMerchandiseTotalAfterAdjustments() {
        return offerOrderLines.stream()
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add);
    }

    private Money getShippingAdjustmentTotal() {
        return getShippingAdjustment()
                .map(FulfillmentGroupFeeAdjustment::getAdjustmentAmount)
                .orElse(Money.ZERO);
    }

    public Money getTotalFeeAmountBeforeAdjustments() {
        return getFees().stream()
                .map(Fee::getPreOfferAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money getTotalFeeAmountAfterAdjustments() {
        return getFees().stream()
                .map(this::feeAmountAfterAdjustments)
                .reduce(Money.ZERO, Money::add);
    }

    public Money getFeeAdjustmentTotal() {
        return getFeeAdjustments().stream()
                .map(FulfillmentGroupFeeAdjustment::getAdjustmentAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money getTotalOfItemsPricedAtRetail() {
        return offerOrderLines.stream()
                .filter(OfferOrderLine::isPricedAtRetail)
                .map(OfferOrderLine::amount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money totalForDepartmentBeforeAdjustments(int departmentId) {
        return offerOrderLines.stream()
                .filter(withDepartmentId(departmentId))
                .map(extendedAmount())
                .reduce(Money.ZERO, Money::add);
    }

    public Money totalForDepartmentAfterAdjustments(int departmentId) {
        return offerOrderLines.stream()
                .filter(withDepartmentId(departmentId))
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add);
    }

    public Money totalForElfaSpaceUses(String... spaceUseIds) {
        return offerOrderLines
                .stream()
                .filter(withSpaceUseIds(spaceUseIds))
                .filter(l -> l.isInDepartment(ELFA_DEPARTMENT))
                .map(OfferOrderLine::amountWithAdjustments)
                .reduce(Money.ZERO, Money::add);
    }

    public List<String> employeeDesignedSpaceIds() {
        return offerOrderLines
                .stream()
                .filter(OfferOrderLines.fromEmployeeDesignedSpace())
                .map(OfferOrderLine::getSpaceId)
                .collect(Collectors.toList());
    }

    public List<String> elfaSpaceIds() {
        return offerOrderLines
                .stream()
                .filter(OfferOrderLines.fromDesignedElfaSpace())
                .map(OfferOrderLine::getSpaceId)
                .collect(Collectors.toList());
    }

    public Money totalForWebCategory(int categoryId) {
        return totalForWebCategories(categoryId);
    }

    public Money totalForWebCategories(Integer...categoryIds) {
        return offerOrderLines.stream()
                .filter(withWebCategoryIds(categoryIds))
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add);
    }

    public Money totalForSkus(Long... skuNumbers) {
        return offerOrderLines.stream()
                .filter(withSkus(skuNumbers))
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add);
    }

    public Money totalForSkuLists(Offer offer, String... skuListIds) {
        return offerOrderLines.stream()
                .filter(withSkuLists(offer, skuListIds))
                .map(amountWithAdjustments())
                .reduce(Money.ZERO, Money::add);
    }

    public int itemCountForSkuLists(Offer offer, String... skuListIds) {
        return offerOrderLines
                .stream()
                .filter(withSkuLists(offer, skuListIds))
                .mapToInt(OfferOrderLine::getQuantity)
                .sum();
    }

    public List<FulfillmentGroupFeeAdjustment> getFeeAdjustmentsForFee(Fee fee) {
        return getFeeAdjustments().stream()
                .filter(adj -> Objects.equals(adj.getFeeId(), fee.getId()))
                .collect(Collectors.toList());
    }

    public int webCategoryItemCount(int categoryId) {
        return itemCountForWebCategories(categoryId);
    }

    public int itemCountForWebCategories(Integer...categoryIds) {
        return offerOrderLines
                .stream()
                .filter(withWebCategoryIds(categoryIds))
                .mapToInt(OfferOrderLine::getQuantity)
                .sum();
    }

    public int itemCountForSkus(Long... skuNumbers) {
        return offerOrderLines
                .stream()
                .filter(withSkus(skuNumbers))
                .mapToInt(OfferOrderLine::getQuantity)
                .sum();
    }

    public int itemCount() {
        return offerOrderLines
                .stream()
                .mapToInt(OfferOrderLine::getQuantity)
                .sum();
    }

    public Money getRegistryTotal() {
        return offerOrderLines.stream()
                .filter(withRegistryId())
                .map(extendedAmount())
                .reduce(Money.ZERO, Money::add);
    }

    public static OfferOrderFulfillmentGroupBuilder builder() {
        return OfferOrderFulfillmentGroupBuilder.builder();
    }

    public List<Fee> getFees() {
        return ImmutableList.copyOf(fees);
    }

    public List<FulfillmentGroupFeeAdjustment> getFeeAdjustments() {
        return ImmutableList.copyOf(feeAdjustments);
    }

    public void addFeeAdjustment(FulfillmentGroupFeeAdjustment feeAdjustment) {
        this.feeAdjustments.add(feeAdjustment);
    }

    public void setShippingAdjustment(FulfillmentGroupFeeAdjustment shippingAdjustment) {
        this.shippingAdjustment = shippingAdjustment;
    }

    public Optional<FulfillmentGroupFeeAdjustment> getShippingAdjustment() {
        return Optional.ofNullable(shippingAdjustment);
    }

    public void splitLine(OfferOrderLine lineToReplace, List<OfferOrderLine> splitLines) {
        int index = this.offerOrderLines.indexOf(lineToReplace);
        this.offerOrderLines.remove(lineToReplace);
        this.offerOrderLines.addAll(index < 0 ? 0 : index, splitLines);
    }

    @VisibleForTesting
    Money feeAmountAfterAdjustments(Fee fee) {
        Money adjustmentAmount = getFeeAdjustmentsForFee(fee).stream()
                .map(FulfillmentGroupFeeAdjustment::getAdjustmentAmount)
                .reduce(Money.ZERO, Money::add);

        return fee.getPreOfferAmount().add(adjustmentAmount);
    }

    @Override
    public String toString() {
        return toStringHelper(this.getClass())
                .add("fulfillmentGroupId", fulfillmentGroupId)
                .add("fulfillmentGroupType", fulfillmentGroupType)
                .add("shippingMethod", shippingMethod)
                .add("shippingState", shippingState)
                .add("offerOrderLines", offerOrderLines)
                .add("feeAdjustments", feeAdjustments)
                .add("fees", fees)
                .toString();
    }
}
