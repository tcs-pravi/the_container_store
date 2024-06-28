package com.containerstore.offer.domain;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.containerstore.offer.domain.AdjustmentType.*;

public class OfferBenefitSummaryGenerator {
    DecimalFormat percentFormat = new DecimalFormat("##0.##");

    public String getOfferBenefitSummary(final Offer offer) {
        return String.join(", ", getBenefitSummariesForOffer(offer));
    }
    
    private List<String> getBenefitSummariesForOffer(Offer offer) {
        List<String> benefitSummaries = new ArrayList<>();
        benefitSummaries.addAll(getPercentOffSummaries(offer));
        benefitSummaries.addAll(getAmountOffSummaries(offer));
        benefitSummaries.addAll(getBuyGetAdjustmentSummaries(offer));
        getFreeShippingSummary(offer).ifPresent(benefitSummaries::add);
        getFlatRateShippingSummary(offer).ifPresent(benefitSummaries::add);
        getAdditionalShippingFeeSummary(offer).ifPresent(benefitSummaries::add);

        return benefitSummaries;
    }
    
    private List<String> getPercentOffSummaries(final Offer offer) {
        return offer.combinedAdjustmentBenefits().stream()
                .filter(AdjustmentBenefits.withAdjustmentType(PERCENT_OFF))
                .map(b -> String.format("%s%% off%s", b.getAdjustmentValue().toBigInteger(), appliesToDescription(b)))
                .collect(Collectors.toList());
    }

    private List<String> getAmountOffSummaries(final Offer offer) {
        return offer.combinedAdjustmentBenefits().stream()
                .filter(AdjustmentBenefits.withAdjustmentType(AMOUNT_OFF))
                .map(b -> String.format("$%s off%s", b.getAdjustmentValue(), appliesToDescription(b)))
                .collect(Collectors.toList());
    }

    private List<String> getBuyGetAdjustmentSummaries(final Offer offer) {
        return offer.getBuyGetAdjustmentBenefits().stream()
                .map(benefit -> String.format("Buy %s of (%s) get up to %s of (%s)%s for %s.%s",
                        benefit.getQualificationGroupSize(),
                        benefit.getQualificationGroupRule(),
                        benefit.getAdjustmentGroupSize(),
                        benefit.getAdjustmentGroupRule(),
                        benefit.allowAdjustmentLinesAboveQualificationMinimum() ? " of any price" : "",
                        benefit.getAdjustmentType() == FIXED_PRICE
                                ? "$" + benefit.getAdjustmentValue()
                                : percentFormat.format(benefit.getAdjustmentValue()) + "% off",
                        benefit.getGroupCountLimit().map(l -> " Limited to " + l + " group(s).").orElse("")))
                .collect(Collectors.toList());
    }

    private Optional<String> getFreeShippingSummary(final Offer offer) {
        return offer.combinedAdjustmentBenefits().stream()
                .filter(AdjustmentBenefits.withAdjustmentType(FREE_SHIPPING))
                .findAny()
                .map(b -> "Free shipping");
    }

    private Optional<String> getFlatRateShippingSummary(final Offer offer) {
        return offer.combinedAdjustmentBenefits().stream()
                .filter(AdjustmentBenefits.withAdjustmentType(FLAT_RATE_SHIPPING))
                .findAny()
                .map(b -> String.format("$%s shipping", b.getAdjustmentValue()));
    }

    private Optional<String> getAdditionalShippingFeeSummary(final Offer offer) {
        return offer.getAdditionalShippingFeeWaivedBenefits().stream()
                .findAny()
                .map(b -> "Waive additional shipping fees");
    }

    private String appliesToDescription(final AdjustmentBenefit b) {
        if (b.getAppliesTo() != null) {
            switch (b.getAppliesTo()) {
                case HIGHEST_PRICED_UNIT:
                    return " highest priced item";
                case TCS_CLOSETS:
                    return " TCS closets";
                case PRESTON:
                    return " Preston";
                default :
                    return "";
            }
        }
        return "";
    }
}
