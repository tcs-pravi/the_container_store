package com.containerstore.offer.domain;

import com.containerstore.common.base.time.SystemTime;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public class OfferBuilder {
    private Long id;
    private String offerCode;
    private String offerName;
    private DateTime createDateTime;
    private DateTime startDateTime;
    private DateTime endDateTime;
    private boolean tracked;
    private Long trackedOfferCodeId;
    private boolean isSingleUse;
    private Integer durationInDays;
    private String customerQualifications;
    private String orderQualifications;
    private DeliveryMethod deliveryMethod;
    private Integer redemptionCount;
    private final List<Integer> offerStores = new ArrayList<>();
    private final List<AdjustmentBenefit> adjustmentBenefits = new ArrayList<>();
    private final List<BuyGetAdjustmentBenefit> buyGetAdjustmentBenefits = new ArrayList<>();
    private final List<DecliningBalanceBenefit> decliningBalanceBenefits = new ArrayList<>();
    private final List<AdditionalShippingFeeWaivedBenefit> additionalShippingFeeWaivedBenefits = new ArrayList<>();
    private final List<FeeAdjustmentBenefit> feeAdjustmentBenefits = new ArrayList<>();
    private final List<QualifiedAdjustmentBenefit> qualifiedAdjustmentBenefits = new ArrayList<>();
    private OfferScope offerScope;
    private boolean isTrackingCodeExternallyGenerated;
    private boolean isPromotedWhenNotApplied;
    private boolean isEmployeeEligible;
    private boolean isStackable;
    private String emailTemplateId;
    private final List<EarnedOfferBenefit> earnedOfferBenefits = new ArrayList<>();
    private final List<OrderKey> redemptionOrders = new ArrayList<>();
    private final List<OrderKey> sourceOrders = new ArrayList<>();
    private BigDecimal customAdjustmentValue;
    private boolean isTrackedCodeReplaceable;
    private String offerTerms;
    private String alternateExpirationText;
    private String promotionalText;
    private Integer purchasingCustomerLoyaltyTier;

    private String note;
    private Availability availability;

    private OfferSkuListCollection skuLists = ImmutableOfferSkuListCollection.builder().build();

    OfferBuilder() {
    }

    public OfferBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public OfferBuilder withOfferCode(String offerCode) {
        this.offerCode = offerCode;
        return this;
    }

    public OfferBuilder withOfferName(String offerName) {
        this.offerName = offerName;
        return this;
    }

    public OfferBuilder withOfferScope(OfferScope offerScope) {
        this.offerScope = offerScope;
        return this;
    }

    public OfferBuilder withCreateDateTime(DateTime createDateTime) {
        this.createDateTime = createDateTime;
        return this;
    }
    public OfferBuilder withStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public OfferBuilder withEndDateTime(DateTime endDateTime) {
        this.endDateTime = endDateTime;
        return this;
    }

    public OfferBuilder withIsTracked(boolean tracked) {
        this.tracked = tracked;
        return this;
    }

    public OfferBuilder withTrackedOfferCodeId(Long trackedOfferCodeId) {
        this.trackedOfferCodeId = trackedOfferCodeId;
        return this;
    }

    public OfferBuilder withIsSingleUse(boolean isSingleUse) {
        this.isSingleUse = isSingleUse;
        return this;
    }

    public OfferBuilder withDuration(Integer durationInDays) {
        this.durationInDays = durationInDays;
        return this;
    }

    public OfferBuilder withCustomerQualifications(String customerQualifications) {
        this.customerQualifications = customerQualifications;
        return this;
    }

    public OfferBuilder withOrderQualifications(String orderQualifications) {
        this.orderQualifications = orderQualifications;
        return this;
    }

    public OfferBuilder withDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
        return this;
    }

    public OfferBuilder withRedemptionCount(Integer redemptionCount) {
        this.redemptionCount = redemptionCount;
        return this;
    }

    public OfferBuilder withAlternateExpirationText(String alternateExpirationText) {
        this.alternateExpirationText = alternateExpirationText;
        return this;
    }

    public OfferBuilder withPromotionalText(String promotionalText) {
        this.promotionalText = promotionalText;
        return this;
    }

    public OfferBuilder withAdjustmentBenefits(Collection<AdjustmentBenefit> adjustmentBenefits) {
        this.adjustmentBenefits.clear();
        this.adjustmentBenefits.addAll(adjustmentBenefits);
        return this;
    }

    public OfferBuilder withBuyGetAdjustmentBenefits(Collection<BuyGetAdjustmentBenefit> buyGetAdjustmentBenefits) {
        this.buyGetAdjustmentBenefits.clear();
        this.buyGetAdjustmentBenefits.addAll(buyGetAdjustmentBenefits);
        return this;
    }

    public OfferBuilder withDecliningBalanceBenefits(Collection<DecliningBalanceBenefit> decliningBalanceBenefits) {
        this.decliningBalanceBenefits.clear();
        this.decliningBalanceBenefits.addAll(decliningBalanceBenefits);
        return this;
    }

    public OfferBuilder withAdditionalShippingFeeWaivedBenefits(
            Collection<AdditionalShippingFeeWaivedBenefit> additionalShippingFeeWaivedBenefits) {
        this.additionalShippingFeeWaivedBenefits.clear();
        this.additionalShippingFeeWaivedBenefits.addAll(additionalShippingFeeWaivedBenefits);
        return this;
    }

    public OfferBuilder withFeeAdjustmentBenefits(
            Collection<FeeAdjustmentBenefit> feeAdjustmentBenefits) {
        this.feeAdjustmentBenefits.clear();
        this.feeAdjustmentBenefits.addAll(feeAdjustmentBenefits);
        return this;
    }

    public OfferBuilder withQualifiedAdjustmentBenefits(
            Collection<QualifiedAdjustmentBenefit> qualifiedAdjustmentBenefits) {
        this.qualifiedAdjustmentBenefits.clear();
        this.qualifiedAdjustmentBenefits.addAll(qualifiedAdjustmentBenefits);
        return this;
    }

    public OfferBuilder withDiscountPercentage(String percentage) {
        if (!isNullOrEmpty(percentage)) {
            AdjustmentBenefit adjustmentBenefit = ImmutableAdjustmentBenefit.builder()
                    .withAdjustmentType(AdjustmentType.PERCENT_OFF)
                    .withAdjustmentValue(new BigDecimal(percentage))
                    .build();
            this.adjustmentBenefits.add(adjustmentBenefit);
        }
        return this;
    }

    public OfferBuilder withDiscountAmount(String amount) {
        if (!isNullOrEmpty(amount)) {
            AdjustmentBenefit adjustmentBenefit = ImmutableAdjustmentBenefit.builder()
                    .withAdjustmentType(AdjustmentType.AMOUNT_OFF)
                    .withAdjustmentValue(new BigDecimal(amount))
                    .build();
            this.adjustmentBenefits.add(adjustmentBenefit);
        }
        return this;
    }

    public OfferBuilder withRedemptionOrders(Collection<OrderKey> redemptionOrders) {
        this.redemptionOrders.addAll(redemptionOrders);
        return this;
    }

    public OfferBuilder withSourceOrders(Collection<OrderKey> sourceOrders) {
        this.sourceOrders.addAll(sourceOrders);
        return this;
    }

    public OfferBuilder withSourceOrder(OrderKey orderKey) {
        this.sourceOrders.add(orderKey);
        return this;
    }

    public OfferBuilder withOfferStores(Collection<Integer> stores) {
        this.offerStores.clear();
        this.offerStores.addAll(stores);
        return this;
    }

    public OfferBuilder withIsTrackingCodeExternallyGenerated(boolean isTrackingCodeExternallyGenerated) {
        this.isTrackingCodeExternallyGenerated = isTrackingCodeExternallyGenerated;
        return this;
    }

    public OfferBuilder withIsPromotedWhenNoApplied(boolean isPromotedWhenNotApplied) {
        this.isPromotedWhenNotApplied = isPromotedWhenNotApplied;
        return this;
    }

    public OfferBuilder withIsEmployeeEligible(boolean isEmployeeEligible) {
        this.isEmployeeEligible = isEmployeeEligible;
        return this;
    }

    public OfferBuilder withIsStackable(boolean isStackable) {
        this.isStackable = isStackable;
        return this;
    }

    public OfferBuilder withEarnedOfferBenefits(Collection<EarnedOfferBenefit> earnedOffers) {
        this.earnedOfferBenefits.clear();
        this.earnedOfferBenefits.addAll(earnedOffers);
        return this;
    }

    public OfferBuilder withEmailTemplateId(String emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
        return this;
    }

    public OfferBuilder withCustomAdjustmentValue(BigDecimal customAdjustmentValue) {
        this.customAdjustmentValue = customAdjustmentValue;
        return this;
    }

    public OfferBuilder withIsTrackedCodeReplaceable(boolean isTrackedCodeReplaceable) {
        this.isTrackedCodeReplaceable = isTrackedCodeReplaceable;
        return this;
    }

    public OfferBuilder withOfferTerms(String offerTerms) {
        this.offerTerms = offerTerms;
        return this;
    }

    public OfferBuilder withPurchasingCustomerLoyaltyTier(Integer loyaltyTier) {
        this.purchasingCustomerLoyaltyTier = loyaltyTier;
        return this;
    }

    public OfferBuilder withSkuLists(OfferSkuListCollection skuLists) {
        this.skuLists.getSkuListMap().clear();
        this.skuLists.getSkuListMap().putAll(skuLists.getSkuListMap());
        return this;
    }

    public boolean isActive() {
        return hasStarted() && !hasEnded();
    }

    boolean hasStarted() {
        DateTime now = now();
        return startDateTime != null && (startDateTime.isEqual(now) || startDateTime.isBefore(now));
    }

    boolean hasEnded() {
        return expiredAsOf(now());
    }

    boolean expiresSoon(int days) {
        return expiredAsOf(daysFromNow(days));
    }

    private DateTime now() {
        return SystemTime.asDateTime();
    }

    private DateTime daysFromNow(int days) {
        return now().plusDays(days);
    }

    private boolean expiredAsOf(DateTime moment) {
        return endDateTime != null && (endDateTime.isEqual(moment) || endDateTime.isBefore(moment));
    }

    public Long getId() {
        return id;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public String getOfferName() {
        return offerName;
    }

    public Long getTrackedOfferCodeId() {
        return trackedOfferCodeId;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public DateTime getCreateDateTime() {
        return createDateTime;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public String getCustomerQualifications() {
        return customerQualifications;
    }

    public String getOrderQualifications() {
        return orderQualifications;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public List<Integer> getOfferStores() {
        return offerStores;
    }

    public Integer getRedemptionCount() {
        return redemptionCount;
    }

    public List<AdjustmentBenefit> getAdjustmentBenefits() {
        return new ArrayList<>(adjustmentBenefits);
    }

    public List<DecliningBalanceBenefit> getDecliningBalanceBenefits() {
        return new ArrayList<>(decliningBalanceBenefits);
    }

    public List<OrderKey> getRedemptionOrders() {
        return new ArrayList<>(redemptionOrders);
    }

    public List<OrderKey> getSourceOrders() {
        return new ArrayList<>(sourceOrders);
    }

    public boolean isTracked() {
        return tracked;
    }

    public boolean isSingleUse() {
        return isSingleUse;
    }

    public boolean isTrackingCodeExternallyGenerated() {
        return isTrackingCodeExternallyGenerated;
    }

    public boolean isPromotedWhenNotApplied() {
        return isPromotedWhenNotApplied;
    }

    public boolean isEmployeeEligible() {
        return isEmployeeEligible;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public boolean hasDecliningBalance() {
        return !decliningBalanceBenefits.isEmpty();
    }

    public List<EarnedOfferBenefit> getEarnedOfferBenefits() {
        return earnedOfferBenefits;
    }

    public boolean isTrackedCodeReplaceable() {
        return isTrackedCodeReplaceable;
    }

    public String getOfferTerms() {
        return offerTerms;
    }

    public String getAlternateExpirationText() {
        return alternateExpirationText;
    }

    public String getPromotionalText() {
        return promotionalText;
    }
    public Integer getPurchasingCustomerLoyaltyTier() {
        return purchasingCustomerLoyaltyTier;
    }

    public OfferBuilder withNote(String note) {
        this.note = note;
        return this;
    }

    public OfferBuilder withAvailability(Availability availability) {
        this.availability = availability;
        return this;
    }

    public Availability getAvailability() {
        return this.availability;
    }

    public Offer build() {
        return new Offer(
                id,
                offerCode,
                offerName,
                offerScope,
                createDateTime,
                startDateTime,
                endDateTime,
                tracked,
                trackedOfferCodeId,
                durationInDays,
                customerQualifications,
                orderQualifications,
                deliveryMethod,
                isSingleUse,
                isStackable,
                redemptionCount,
                offerStores,
                adjustmentBenefits,
                buyGetAdjustmentBenefits,
                decliningBalanceBenefits,
                additionalShippingFeeWaivedBenefits,
                feeAdjustmentBenefits,
                qualifiedAdjustmentBenefits,
                isTrackingCodeExternallyGenerated,
                isPromotedWhenNotApplied,
                isEmployeeEligible,
                earnedOfferBenefits,
                sourceOrders,
                redemptionOrders,
                emailTemplateId,
                customAdjustmentValue,
                isTrackedCodeReplaceable,
                offerTerms,
                alternateExpirationText,
                promotionalText,
                purchasingCustomerLoyaltyTier,
                skuLists,
                note,
                availability);
    }
}
