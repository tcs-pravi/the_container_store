package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.containerstore.offer.domain.AdjustmentBenefits.withAdjustmentType;
import static com.containerstore.offer.domain.AdjustmentBenefits.withShippingAdjustmentType;
import static com.containerstore.offer.domain.AdjustmentType.*;
import static com.containerstore.offer.domain.Offers.*;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class Offer {

    private Long id;
    private String offerCode;
    private String offerName;
    private OfferScope offerScope;
    private DateTime createDateTime;
    private DateTime startDateTime;
    private DateTime endDateTime;
    private boolean tracked;
    private Long trackedOfferCodeId;
    private Integer durationInDays;
    private String customerQualifications;
    private String orderQualifications;
    private DeliveryMethod deliveryMethod;

    @JsonProperty("isSingleUse")
    private boolean isSingleUse;

    @JsonProperty("isStackable")
    private boolean isStackable;
    private Integer redemptionCount;
    private boolean isTrackingCodeExternallyGenerated;
    private boolean isPromotedWhenNotApplied;
    private String emailTemplateId;
    private BigDecimal customAdjustmentValue;
    private boolean isTrackedCodeReplaceable;
    private String offerTerms;
    private String alternateExpirationText;
    private String promotionalText;
    private Integer purchasingCustomerLoyaltyTier;

    private OfferSkuListCollection skuLists = ImmutableOfferSkuListCollection.builder().build();

    private final List<AdjustmentBenefit> adjustmentBenefits = new ArrayList<>();
    private final List<Integer> offerStores = new ArrayList<>();
    private final List<EarnedOfferBenefit> earnedOfferBenefits = new ArrayList<>();
    private final List<BuyGetAdjustmentBenefit> buyGetAdjustmentBenefits = new ArrayList<>();
    private final List<DecliningBalanceBenefit> decliningBalanceBenefits = new ArrayList<>();
    private final List<AdditionalShippingFeeWaivedBenefit> additionalShippingFeeWaivedBenefits = new ArrayList<>();
    private final List<FeeAdjustmentBenefit> feeAdjustmentBenefits = new ArrayList<>();
    private final List<QualifiedAdjustmentBenefit> qualifiedAdjustmentBenefits = new ArrayList<>();
    private final List<OrderKey> redemptionOrders = new ArrayList<>();
    private final List<OrderKey> sourceOrders = new ArrayList<>();

    private boolean isEmployeeEligible;

    private String note;

    private Availability availability;

    @RequiredBy("Unmarshalling")
    @SuppressWarnings("squid:S1186") // Required by unmarshalling
    public Offer() {}

    public void setId(Long id) {
        this.id = id;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public void setCreateDateTime(DateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(DateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    public void setTrackedOfferCodeId(Long trackedOfferCodeId) {
        this.trackedOfferCodeId = trackedOfferCodeId;
    }

    public void setDurationInDays(Integer durationInDays) {
        this.durationInDays = durationInDays;
    }

    public void setCustomerQualifications(String customerQualifications) {
        this.customerQualifications = customerQualifications;
    }

    public void setOrderQualifications(String orderQualifications) {
        this.orderQualifications = orderQualifications;
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public void setIsSingleUse(boolean isSingleUse) {
        this.isSingleUse = isSingleUse;
    }

    public void setIsStackable(boolean isStackable) {
        this.isStackable = isStackable;
    }

    public void setRedemptionCount(Integer redemptionCount) {
        this.redemptionCount = redemptionCount;
    }

    public boolean isTrackingCodeExternallyGenerated() {
        return isTrackingCodeExternallyGenerated;
    }

    public void setTrackingCodeExternallyGenerated(boolean trackingCodeExternallyGenerated) {
        isTrackingCodeExternallyGenerated = trackingCodeExternallyGenerated;
    }

    public void setEmailTemplateId(String emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
    }

    public void setCustomAdjustmentValue(BigDecimal customAdjustmentValue) {
        this.customAdjustmentValue = customAdjustmentValue;
    }

    public void setTrackedCodeReplaceable(boolean trackedCodeReplaceable) {
        isTrackedCodeReplaceable = trackedCodeReplaceable;
    }

    public void setOfferTerms(String offerTerms) {
        this.offerTerms = offerTerms;
    }

    public void setAlternateExpirationText(String alternateExpirationText) {
        this.alternateExpirationText = alternateExpirationText;
    }

    public void setPromotionalText(String promotionalText) {
        this.promotionalText = promotionalText;
    }

    public void setSkuLists(OfferSkuListCollection skuLists) {
        this.skuLists = skuLists;
    }

    public void setEmployeeEligible(boolean employeeEligible) {
        isEmployeeEligible = employeeEligible;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    Offer(
            Long id,
            String offerCode,
            String offerName,
            OfferScope offerScope,
            DateTime createDateTime,
            DateTime startDateTime,
            DateTime endDateTime,
            boolean tracked,
            Long trackedOfferCodeId,
            Integer durationInDays,
            String customerQualifications,
            String orderQualifications,
            DeliveryMethod deliveryMethod,
            boolean isSingleUse,
            boolean isStackable,
            Integer redemptionCount,
            List<Integer> offerStores,
            List<AdjustmentBenefit> adjustmentBenefits,
            List<BuyGetAdjustmentBenefit> buyGetAdjustmentBenefits,
            List<DecliningBalanceBenefit> decliningBalanceBenefits,
            List<AdditionalShippingFeeWaivedBenefit> additionalShippingFeeWaivedBenefits,
            List<FeeAdjustmentBenefit> feeAdjustmentBenefits,
            List<QualifiedAdjustmentBenefit> qualifiedAdjustmentBenefits,
            boolean isTrackingCodeExternallyGenerated,
            boolean isPromotedWhenNotApplied,
            boolean isEmployeeEligible,
            List<EarnedOfferBenefit> earnedOfferBenefits,
            List<OrderKey> sourceOrders,
            List<OrderKey> redemptionOrders,
            String emailTemplateId,
            BigDecimal customAdjustmentValue,
            boolean isTrackedCodeReplaceable,
            String offerTerms,
            String alternateExpirationText,
            String promotionalText,
            Integer purchasingCustomerLoyaltyTier,
            OfferSkuListCollection skuLists,
            String note,
            Availability availability) {
        this.id = id;
        this.offerCode = offerCode;
        this.offerName = offerName;
        this.offerScope = offerScope;
        this.createDateTime = createDateTime;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.tracked = tracked;
        this.trackedOfferCodeId = trackedOfferCodeId;
        this.durationInDays = durationInDays;
        this.customerQualifications = customerQualifications;
        this.orderQualifications = orderQualifications;
        this.deliveryMethod = deliveryMethod;
        this.isSingleUse = isSingleUse;
        this.isStackable = isStackable;
        this.redemptionCount = redemptionCount;
        this.isEmployeeEligible = isEmployeeEligible;
        this.customAdjustmentValue = customAdjustmentValue;
        this.isTrackedCodeReplaceable = isTrackedCodeReplaceable;
        this.offerTerms = offerTerms;
        this.alternateExpirationText = alternateExpirationText;
        this.promotionalText = promotionalText;
        this.offerStores.addAll(offerStores);
        this.adjustmentBenefits.addAll(adjustmentBenefits);
        this.buyGetAdjustmentBenefits.addAll(buyGetAdjustmentBenefits);
        this.decliningBalanceBenefits.addAll(decliningBalanceBenefits);
        this.isTrackingCodeExternallyGenerated = isTrackingCodeExternallyGenerated;
        this.isPromotedWhenNotApplied = isPromotedWhenNotApplied;
        this.earnedOfferBenefits.addAll(earnedOfferBenefits);
        this.sourceOrders.addAll(sourceOrders);
        this.redemptionOrders.addAll(redemptionOrders);
        this.emailTemplateId = emailTemplateId;
        this.additionalShippingFeeWaivedBenefits.addAll(additionalShippingFeeWaivedBenefits);
        this.feeAdjustmentBenefits.addAll(feeAdjustmentBenefits);
        this.qualifiedAdjustmentBenefits.addAll(qualifiedAdjustmentBenefits);
        this.purchasingCustomerLoyaltyTier = purchasingCustomerLoyaltyTier;
        this.skuLists.getSkuListMap().putAll(skuLists.getSkuListMap());
        this.note = note;
        this.availability = availability;
    }

    public static OfferBuilder builder() {
        return new OfferBuilder();
    }

    public List<OrderKey> getRedemptionOrders() {
        return ImmutableList.copyOf(redemptionOrders);
    }

    public List<OrderKey> getSourceOrders() {
        return ImmutableList.copyOf(sourceOrders);
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

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public boolean isSingleUse() {
        return isSingleUse;
    }

    public DateTime getCreateDateTime() {
        return createDateTime;
    }
    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public boolean isTracked() {
        return tracked;
    }

    public Long getTrackedOfferCodeId() {
        return trackedOfferCodeId;
    }

    public boolean hasTrackedOfferCodeId() {
        return trackedOfferCodeId != null;
    }

    public Integer getDurationInDays() {
        return durationInDays;
    }

    public String getCustomerQualifications() {
        return customerQualifications;
    }

    public String getOrderQualifications() {
        return orderQualifications;
    }

    public Optional<BigDecimal> getCustomAdjustmentValue() {
        return Optional.ofNullable(customAdjustmentValue);
    }

    public boolean hasOrderQualifications() {
        return !isNullOrEmpty(getOrderQualifications());
    }

    public List<Integer> getOfferStores() {
        return ImmutableList.copyOf(offerStores);
    }

    public Integer getRedemptionCount() {
        return redemptionCount;
    }

    public List<AdjustmentBenefit> getAdjustmentBenefits() {
        return unmodifiableList(adjustmentBenefits);
    }

    /**
     * @return a list of benefits that could potentially provide an adjustment to applicable
     * order line items. These benefits could include amount-off or percent-off order adjustment benefits
     * as well as declining balance benefits which are converted into amount-off adjustment benefits.
     */
    public List<AdjustmentBenefit> combinedAdjustmentBenefits() {
        List<AdjustmentBenefit> allAdjustmentBenefits = new ArrayList<>();
        allAdjustmentBenefits.addAll(adjustmentBenefits);
        allAdjustmentBenefits.addAll(decliningBalanceBenefits.stream()
                .map(toAmountOffBenefit())
                .collect(toList()));
        return allAdjustmentBenefits;
    }

    public String getOfferTerms() {
        return Optional.ofNullable(this.offerTerms).orElse("");
    }

    public Optional<String> getAlternateExpirationText() {
        return Optional.ofNullable(this.alternateExpirationText);
    }

    public Optional<String> getPromotionalText() {
        return Optional.ofNullable(this.promotionalText);
    }

    @JsonGetter
    public String getBenefitSummary() {
        return new OfferBenefitSummaryGenerator().getOfferBenefitSummary(this);
    }

    public List<BuyGetAdjustmentBenefit> getBuyGetAdjustmentBenefits() {
        return ImmutableList.copyOf(buyGetAdjustmentBenefits);
    }

    public List<DecliningBalanceBenefit> getDecliningBalanceBenefits() {
        return ImmutableList.copyOf(decliningBalanceBenefits);
    }

    public List<AdditionalShippingFeeWaivedBenefit> getAdditionalShippingFeeWaivedBenefits() {
        return ImmutableList.copyOf(additionalShippingFeeWaivedBenefits);
    }

    public List<FeeAdjustmentBenefit> getFeeAdjustmentBenefits() {
        return ImmutableList.copyOf(feeAdjustmentBenefits);
    }

    public List<QualifiedAdjustmentBenefit> getQualifiedAdjustmentBenefits() {
        return unmodifiableList(qualifiedAdjustmentBenefits);
    }

    public boolean isActive() {
        return hasStarted(this) && !hasEnded(this);
    }

    public void overrideOfferCode(String overrideCode) {
        this.offerCode = overrideCode;
    }

    public OfferScope getOfferScope() {
        return offerScope;
    }

    public void setOfferScope(OfferScope offerScope) {
        this.offerScope = offerScope;
    }

    public boolean offerMayBeExternallyGenerated() {
        return isTrackingCodeExternallyGenerated;
    }

    public boolean isPromotedWhenNotApplied() {
        return isPromotedWhenNotApplied;
    }

    public void setPromotedWhenNotApplied(boolean isPromotedWhenNotApplied) {
        this.isPromotedWhenNotApplied = isPromotedWhenNotApplied;
    }

    public boolean isEmployeeEligible() {
        return isEmployeeEligible;
    }

    public List<EarnedOfferBenefit> getEarnedOfferBenefits() {
        return ImmutableList.copyOf(earnedOfferBenefits);
    }

    public boolean isStackable() {
        return isStackable;
    }

    public boolean hasAmountOff() {
        return combinedAdjustmentBenefits().stream()
                .anyMatch(withAdjustmentType(AMOUNT_OFF));
    }

    /**
     * @deprecated use hasAmountOff
     */
    @Deprecated
    public boolean isAmountOff() {
        return hasAmountOff();
    }

    public boolean hasPercentOff() {
        return combinedAdjustmentBenefits().stream()
                .anyMatch(withAdjustmentType(PERCENT_OFF));
    }

    /**
     * @deprecated use hasPercentOff
     */
    @Deprecated
    public boolean isPercentOff() {
        return hasPercentOff();
    }

    public boolean hasNonAmountBenefits() {
        return hasPercentOff()
                || hasFixedPriceBenefit()
                || hasBuyGetAdjustmentBenefit()
                || hasShippingAdjustments()
                || hasAdditionalShippingFeeWaivedBenefit()
                || hasFeeAdjustmentBenefit()
                || hasQualifiedAdjustmentBenefits();
    }

    public boolean hasFixedPriceBenefit() {
        return combinedAdjustmentBenefits().stream()
                .anyMatch(withAdjustmentType(FIXED_PRICE));
    }

    public boolean hasShippingAdjustments() {
        return combinedAdjustmentBenefits().stream()
                .anyMatch(withShippingAdjustmentType());
    }

    public boolean hasDecliningBalanceBenefit() {
        return !getDecliningBalanceBenefits().isEmpty();
    }

    public boolean hasBuyGetAdjustmentBenefit() {
        return !getBuyGetAdjustmentBenefits().isEmpty();
    }

    public boolean hasAdjustmentBenefit() {
        return !combinedAdjustmentBenefits().isEmpty();
    }

    public boolean hasAdditionalShippingFeeWaivedBenefit() {
        return !additionalShippingFeeWaivedBenefits.isEmpty();
    }

    public boolean hasFeeAdjustmentBenefit() {
        return !getFeeAdjustmentBenefits().isEmpty();
    }

    public boolean hasQualifiedAdjustmentBenefits() {
        return !getQualifiedAdjustmentBenefits().isEmpty();
    }

    public boolean isTrackedCodeReplaceable() {
        return isTrackedCodeReplaceable;
    }

    public String getEmailTemplateId() {
        return emailTemplateId;
    }

    public void setPurchasingCustomerLoyaltyTier(Integer purchasingCustomerLoyaltyTier) {
        this.purchasingCustomerLoyaltyTier = purchasingCustomerLoyaltyTier;
    }

    public Integer getPurchasingCustomerLoyaltyTier() {
        return this.purchasingCustomerLoyaltyTier;
    }

    public int customerLoyaltyTier() {
        return this.getPurchasingCustomerLoyaltyTier() != null
                ? this.getPurchasingCustomerLoyaltyTier().intValue()
                : 0;
    }

    public boolean forLoyaltyTier(int tier) {
        return Integer.valueOf(tier).equals(getPurchasingCustomerLoyaltyTier());
    }

    public OfferSkuListCollection getSkuLists() {
        return this.skuLists;
    }

    public String getNote() {
        return this.note;
    }

    public Availability getAvailability() {
        return this.availability;
    }

    public OfferBuilder copy() {
        return new OfferBuilder()
                .withId(getId())
                .withOfferCode(getOfferCode())
                .withOfferName(getOfferName())
                .withCreateDateTime(getCreateDateTime())
                .withStartDateTime(getStartDateTime())
                .withEndDateTime(getEndDateTime())
                .withIsTracked(isTracked())
                .withTrackedOfferCodeId(getTrackedOfferCodeId())
                .withIsSingleUse(isSingleUse())
                .withDuration(getDurationInDays())
                .withCustomerQualifications(getCustomerQualifications())
                .withOrderQualifications(getOrderQualifications())
                .withDeliveryMethod(getDeliveryMethod())
                .withRedemptionCount(getRedemptionCount())
                .withOfferStores(getOfferStores())
                .withAdjustmentBenefits(getAdjustmentBenefits())
                .withBuyGetAdjustmentBenefits(getBuyGetAdjustmentBenefits())
                .withDecliningBalanceBenefits(getDecliningBalanceBenefits())
                .withAdditionalShippingFeeWaivedBenefits(getAdditionalShippingFeeWaivedBenefits())
                .withFeeAdjustmentBenefits(getFeeAdjustmentBenefits())
                .withQualifiedAdjustmentBenefits(getQualifiedAdjustmentBenefits())
                .withOfferScope(getOfferScope())
                .withIsTrackingCodeExternallyGenerated(offerMayBeExternallyGenerated())
                .withIsPromotedWhenNoApplied(isPromotedWhenNotApplied())
                .withIsEmployeeEligible(isEmployeeEligible())
                .withIsStackable(isStackable())
                .withEmailTemplateId(getEmailTemplateId())
                .withEarnedOfferBenefits(getEarnedOfferBenefits())
                .withRedemptionOrders(getRedemptionOrders())
                .withSourceOrders(getSourceOrders())
                .withCustomAdjustmentValue(getCustomAdjustmentValue().orElse(null))
                .withIsTrackedCodeReplaceable(isTrackedCodeReplaceable())
                .withOfferTerms(getOfferTerms())
                .withAlternateExpirationText(getAlternateExpirationText().orElse(null))
                .withPromotionalText(getPromotionalText().orElse(null))
                .withPurchasingCustomerLoyaltyTier(getPurchasingCustomerLoyaltyTier())
                .withSkuLists(getSkuLists())
                .withNote(getNote())
                .withAvailability(getAvailability());
    }

    @Override
    public String toString() {
        return toStringHelper(this.getClass())
                .add("id", id)
                .add("offerCode", offerCode)
                .toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Offer)) {
            return false;
        }

        Offer other = (Offer) o;

        return Objects.equal(this.id, other.id)
                && Objects.equal(this.offerCode, other.offerCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, offerCode);
    }
}
