package com.containerstore.offer.domain;

public class EarnedOfferBenefitBuilder {

    private Long id;
    private Long earnedOfferId;
    private String earnedOfferCode;
    private String earnedOfferName;
    private boolean isTrackedOfferCodePregenerated;

    public static EarnedOfferBenefitBuilder builder() {
        return new EarnedOfferBenefitBuilder();
    }

    public EarnedOfferBenefitBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public EarnedOfferBenefitBuilder withEarnedOfferId(Long earnedOfferId) {
        this.earnedOfferId = earnedOfferId;
        return this;
    }

    public EarnedOfferBenefitBuilder withEarnedOfferCode(String earnedOfferCode) {
        this.earnedOfferCode = earnedOfferCode;
        return this;
    }

    public EarnedOfferBenefitBuilder withEarnedOfferName(String earnedOfferName) {
        this.earnedOfferName = earnedOfferName;
        return this;
    }

    public EarnedOfferBenefitBuilder withIsTrackedOfferCodePregenerated(boolean isTrackedOfferCodePregenerated) {
        this.isTrackedOfferCodePregenerated = isTrackedOfferCodePregenerated;
        return this;
    }

    public EarnedOfferBenefit build() {
        return new EarnedOfferBenefit(
                id,
                earnedOfferId,
                earnedOfferCode,
                earnedOfferName,
                isTrackedOfferCodePregenerated
        );
    }
}
