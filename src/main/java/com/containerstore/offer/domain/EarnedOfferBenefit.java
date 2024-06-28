package com.containerstore.offer.domain;

import com.containerstore.common.base.RequiredBy;
import com.twoqubed.bob.annotation.Built;

@Built
public class EarnedOfferBenefit {

    private Long id;
    private Long earnedOfferId;
    private String earnedOfferCode;
    private String earnedOfferName;
    private boolean isTrackedOfferCodePregenerated;

    @RequiredBy("Unmarshalling")
    @SuppressWarnings("squid:S1186") // Require by unmarshalling
    public EarnedOfferBenefit() {}

    EarnedOfferBenefit(
            Long id,
            Long earnedOfferId,
            String earnedOfferCode,
            String earnedOfferName,
            boolean isTrackedOfferCodePregenerated) {
        this.id = id;
        this.earnedOfferId = earnedOfferId;
        this.earnedOfferCode = earnedOfferCode;
        this.earnedOfferName = earnedOfferName;
        this.isTrackedOfferCodePregenerated = isTrackedOfferCodePregenerated;
    }

    public Long getId() {
        return id;
    }

    public Long getEarnedOfferId() {
        return earnedOfferId;
    }

    public String getEarnedOfferCode() {
        return earnedOfferCode;
    }

    public String getEarnedOfferName() {
        return earnedOfferName;
    }

    public boolean isTrackedOfferCodePregenerated() {
        return isTrackedOfferCodePregenerated;
    }

    public static EarnedOfferBenefitBuilder builder() {
        return EarnedOfferBenefitBuilder.builder();
    }
}
