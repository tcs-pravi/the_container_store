package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.containerstore.common.base.money.Money;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.twoqubed.bob.annotation.Built;

import java.util.*;
import java.util.stream.Collectors;

import static com.containerstore.common.base.money.Money.ZERO;
import static com.containerstore.offer.domain.ClosetDepartments.*;
import static com.containerstore.offer.domain.FeeTypes.AVERA_INSTALL_FEE;
import static com.containerstore.offer.domain.OfferCustomers.ANONYMOUS_CUSTOMER;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;

@Built
public class OfferOrder {

    private Integer ringStore;
    private String sourceReferenceId;
    private OfferCustomer purchasingCustomer = ANONYMOUS_CUSTOMER;
    private String shippingFeeType;
    private List<OfferOrderFulfillmentGroup> offerOrderFulfillmentGroups = new ArrayList<>();
    private List<Offer> prospectiveOffers = new ArrayList<>();
    private Set<Offer> winningOffers = new HashSet<>();
    private Long elfaSpaceCount;
    private Long employeeDesignedSpaceCount;

    private PurchasingChannel purchasingChannel;
    private Map<String, Integer> webCategoryItemCounts = new HashMap<>();
    private Map<Long, Integer> skuItemCounts = new HashMap<>();

    @RequiredBy("Unmarshalling")
    public OfferOrder() {
    }

    OfferOrder(
            Integer ringStore,
            OfferCustomer purchasingCustomer,
            String shippingFeeType,
            List<OfferOrderFulfillmentGroup> fulfillmentGroups,
            List<Offer> prospectiveOffers,
            String sourceReferenceId,
            PurchasingChannel purchasingChannel) {
        this.ringStore = ringStore;
        this.purchasingCustomer = firstNonNull(purchasingCustomer, ANONYMOUS_CUSTOMER);
        this.shippingFeeType = shippingFeeType;
        this.offerOrderFulfillmentGroups = firstNonNull(fulfillmentGroups, new ArrayList<OfferOrderFulfillmentGroup>());
        this.prospectiveOffers = firstNonNull(prospectiveOffers, new ArrayList<Offer>());
        this.sourceReferenceId = sourceReferenceId;
        this.purchasingChannel = purchasingChannel;
    }

    public Integer getRingStore() {
        return ringStore;
    }

    public List<OfferOrderFulfillmentGroup> getOfferOrderFulfillmentGroups() {
        return offerOrderFulfillmentGroups;
    }

    public List<Offer> getProspectiveOffers() {
        return ImmutableList.copyOf(prospectiveOffers);
    }

    public String getShippingFeeType() {
        return shippingFeeType;
    }

    public PurchasingChannel getPurchasingChannel() {
        return purchasingChannel;
    }

    public static OfferOrderBuilder builder() {
        return OfferOrderBuilder.builder();
    }

    /**
     * returns the order total after applying all line and fee adjustments
     *
     * Note: this method signature exists for MVEL simplification
     * Example: order.total.isGreaterThanOrEqualTo(toMoney(100B))
     */
    public Money getTotal() {
        return getTotalAmountAfterAdjustments();
    }

    /**
     * Note: this method signature exists for MVEL simplification
     * Example: order.merchandiseTotal.isGreaterThanOrEqualTo(toMoney(100B))
     */
    public Money getMerchandiseTotal() {
        return getMerchandiseTotalAfterAdjustments();
    }

    /**
     * returns the order total after all applying all line and fee adjustments
     */
    public Money getTotalAmountAfterAdjustments() {
        return offerOrderFulfillmentGroups.stream()
                .map(OfferOrderFulfillmentGroup::getTotalAmountAfterAdjustments)
                .reduce(ZERO, Money::add);
    }

    public Money getTotalAmountBeforeAdjustments() {
        return offerOrderFulfillmentGroups.stream()
                .map(OfferOrderFulfillmentGroup::getTotalAmountBeforeAdjustments)
                .reduce(ZERO, Money::add);
    }

    public Money getTotalOfItemsPricedAtRetail() {
        return offerOrderFulfillmentGroups.stream()
                .map(OfferOrderFulfillmentGroup::getTotalOfItemsPricedAtRetail)
                .reduce(ZERO, Money::add);
    }

    /**
     * returns the total of merchandise exclusively.
     * This differs from totalAfterAdjustments in that it will omit any fees. (e.g. shipping)
     * Because it is After Adjustments It will include any line adjustments.
     */
    public Money getMerchandiseTotalAfterAdjustments() {
        return offerOrderFulfillmentGroups.stream()
                .map(OfferOrderFulfillmentGroup::getMerchandiseTotalAfterAdjustments)
                .reduce(ZERO, Money::add);
    }

    public Money totalForWebCategory(int categoryId) {
        return totalForWebCategories(categoryId);
    }

    /**
     * returns the merchandise total of SKU's for given web Categories.
     * This method only counts a SKU once even it belongs to multiple categories for total calculations.
     */
    public Money totalForWebCategories(Integer... categoryIds) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForWebCategories(categoryIds))
                .reduce(ZERO, Money::add);
    }

    public int webCategoryItemCount(int categoryId) {
        return itemCountForWebCategories(categoryId);
    }

    public int itemCountForWebCategories(Integer... categoryIds) {
        String categoryCsv = Arrays.asList(categoryIds).stream().sorted()
                .map(Object::toString).collect(Collectors.joining(","));
        return webCategoryItemCounts.computeIfAbsent(
                categoryCsv,
                k -> offerOrderFulfillmentGroups
                        .stream()
                        .mapToInt(fg -> fg.itemCountForWebCategories(categoryIds))
                        .sum());
    }

    public int itemCountForSkuLists(Offer offer, String... skuListIds) {
        return offerOrderFulfillmentGroups.stream()
                .mapToInt(fg -> fg.itemCountForSkuLists(offer, skuListIds))
                .sum();
    }

    public Money totalForSkuList(Collection<Long> skuNumbers) {
        return totalForSkus(skuNumbers.toArray(new Long[0]));
    }

    public Money totalForSkus(Long... skuNumbers) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForSkus(skuNumbers))
                .reduce(ZERO, Money::add);
    }

    public Money totalForSkuLists(Offer offer, String... skuListIds) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForSkuLists(offer, skuListIds))
                .reduce(ZERO, Money::add);
    }

    public int itemCountForSkus(Long... skuNumbers) {
        return Arrays.stream(skuNumbers)
                .mapToInt(this::itemCountForSku)
                .sum();
    }

    public int itemCount() {
        return offerOrderFulfillmentGroups.stream()
                .mapToInt(OfferOrderFulfillmentGroup::itemCount)
                .sum();
    }

    private int itemCountForSku(Long skuNumber) {
        return skuItemCounts.computeIfAbsent(
                skuNumber,
                k -> offerOrderFulfillmentGroups
                        .stream()
                        .mapToInt(fg -> fg.itemCountForSkus(k))
                        .sum());
    }

    public Money totalForDepartmentBeforeAdjustments(int departmentId) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForDepartmentBeforeAdjustments(departmentId))
                .reduce(ZERO, Money::add);
    }

    public Money totalForDepartmentAfterAdjustments(int departmentId) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForDepartmentAfterAdjustments(departmentId))
                .reduce(ZERO, Money::add);
    }

    public Money totalForFeeBeforeAdjustments(String feeType) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForFeeTypeBeforeAdjustments(feeType))
                .reduce(ZERO, Money::add);
    }

    public Money totalForFeeAfterAdjustments(String feeType) {
        return offerOrderFulfillmentGroups.stream()
                .map(fg -> fg.totalForFeeTypeAfterAdjustments(feeType))
                .reduce(ZERO, Money::add);
    }

    public Money getTotalForAveraWithBasicInstall() {
        return totalForDepartmentAfterAdjustments(AVERA_DEPARTMENT)
                .add(totalForFeeAfterAdjustments(AVERA_INSTALL_FEE));
    }

    public Money getTotalForLarenWithBasicInstall() {
        return totalForDepartmentAfterAdjustments(LAREN_DEPARTMENT);
    }

    public Money getTotalForPrestonWithBasicInstall() {
        return totalForDepartmentAfterAdjustments(PRESTON_DEPARTMENT);
    }

    public Money getTotalForElfaMerchandise() {
        return totalForDepartmentAfterAdjustments(ELFA_DEPARTMENT);
    }

    public Money getTotalForElfaSpaceUses(String... spaceUseIds) {
        return offerOrderFulfillmentGroups
                .stream()
                .map(fg -> fg.totalForElfaSpaceUses(spaceUseIds))
                .reduce(Money.ZERO, Money::add);
    }

    public long employeeDesignedSpaceCount() {
        if (this.employeeDesignedSpaceCount == null) {
            this.employeeDesignedSpaceCount = offerOrderFulfillmentGroups
                    .stream()
                    .flatMap(fg -> fg.employeeDesignedSpaceIds().stream())
                    .distinct()
                    .count();
        }
        return this.employeeDesignedSpaceCount;
    }

    public long elfaSpaceCount() {
        if (this.elfaSpaceCount == null) {
            this.elfaSpaceCount = offerOrderFulfillmentGroups
                    .stream()
                    .flatMap(fg -> fg.elfaSpaceIds().stream())
                    .distinct()
                    .count();
        }
        return this.elfaSpaceCount;
    }

    public void addWinningOffer(Offer offer) {
        winningOffers.add(offer);
    }

    public Set<Offer> getWinningOffers() {
        return winningOffers;
    }

    public Set<Offer> getNonWinningOffers() {
        Set<Offer> nonWinningOffers = new HashSet<>(getProspectiveOffers());
        nonWinningOffers.removeAll(winningOffers);
        return nonWinningOffers;
    }

    public List<OfferOrderLine> getOrderLinesWithId(String lineId) {
        List<OfferOrderLine> offerOrderLines = new ArrayList<>();
        for (OfferOrderFulfillmentGroup fulfillmentGroup : offerOrderFulfillmentGroups) {
            for (OfferOrderLine line : fulfillmentGroup.getOfferOrderLines()) {
                if (line.getLineId().equals(lineId)) {
                    offerOrderLines.add(line);
                }
            }
        }
        return offerOrderLines;
    }

    /**
     * @deprecated as of Offer Services 2.0, use getOrderLinesWithId() to find
     * all order lines matching this ID, then get the adjustments for each order line.
     */
    @Deprecated
    @VisibleForTesting
    public List<OrderLineAdjustment> getLineAdjustmentsForLine(String lineId) {
        List<OrderLineAdjustment> adjustments = new ArrayList<>();
        for (OfferOrderFulfillmentGroup fulfillmentGroup : offerOrderFulfillmentGroups) {
            for (OfferOrderLine line : fulfillmentGroup.getOfferOrderLines()) {
                if (line.getLineId().equals(lineId)) {
                    adjustments.addAll(line.getLineAdjustments());
                }
            }
        }
        return adjustments;
    }

    public Optional<OfferOrderFulfillmentGroup> getFulfillmentGroupWithId(String id) {
        return offerOrderFulfillmentGroups.stream()
                .filter(fg -> fg.getFulfillmentGroupId().equals(id))
                .findFirst();
    }

    public Optional<OfferOrderFulfillmentGroup> getFulfillmentGroupWithLine(OfferOrderLine line) {
        for (OfferOrderFulfillmentGroup fulfillmentGroup : getOfferOrderFulfillmentGroups()) {
            for (OfferOrderLine offerOrderLine : fulfillmentGroup.getOfferOrderLines()) {
                if (offerOrderLine.getLineId().equals(line.getLineId())) {
                    return Optional.of(fulfillmentGroup);
                }
            }
        }
        return Optional.empty();
    }

    public List<OfferOrderLine> allOfferOrderLines() {
        List<OfferOrderLine> offerOrderLines = new ArrayList<>();
        for (OfferOrderFulfillmentGroup fulfillmentGroup : offerOrderFulfillmentGroups) {
            offerOrderLines.addAll(fulfillmentGroup.getOfferOrderLines());
        }
        return offerOrderLines;
    }

    public int getOfferOrderLineCount() {
        int count = 0;
        for (OfferOrderFulfillmentGroup fg : offerOrderFulfillmentGroups) {
            count += fg.getOfferOrderLines().size();
        }
        return count;
    }

    public boolean isEmployeeTransaction() {
        return !isNullOrEmpty(purchasingCustomer.getEmployeeId());
    }

    public OfferCustomer getPurchasingCustomer() {
        return purchasingCustomer;
    }

    public int getCustomerLoyaltyTier() {
        return getPurchasingCustomer() != null && getPurchasingCustomer().getLoyaltyTier() != null
                ? getPurchasingCustomer().getLoyaltyTier().intValue()
                : 0;
    }

    public String getSourceReferenceId() {
        return sourceReferenceId;
    }

    public Money getTotalAdditionalShippingFeesAfterAdjustments() {
        return getOfferOrderFulfillmentGroups().stream()
                .map(OfferOrderFulfillmentGroup::getOfferOrderLines)
                .flatMap(Collection::stream)
                .map(line -> line.extendedRetailAdditionalShippingFee()
                        .minus(line.extendedAdditionalShippingFeeAdjustment().orElse(ZERO)))
                .reduce(ZERO, Money::add);
    }

    public List<FulfillmentGroupFeeAdjustment> getFulfillmentGroupFeeAdjustments() {
        return getOfferOrderFulfillmentGroups().stream()
                .map(OfferOrderFulfillmentGroup::getFeeAdjustments)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return toStringHelper(this.getClass())
                .add("ringStore", ringStore)
                .add("shippingFeeType", shippingFeeType)
                .add("offerOrderFulfillmentGroups", offerOrderFulfillmentGroups)
                .add("sourceReferenceId", sourceReferenceId)
                .add("purchasingChannel", purchasingChannel)
                .toString();
    }
}
