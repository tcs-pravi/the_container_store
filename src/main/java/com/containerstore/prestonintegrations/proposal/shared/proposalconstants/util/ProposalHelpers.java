package com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util;

import com.containerstore.common.base.exception.BusinessException;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesHeaderFeesInner;
import com.containerstore.prestonintegrations.proposal.models.Space;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"java:S1192"})
public class ProposalHelpers {

    private ProposalHelpers() {
    }

    public static Boolean hasSellingPrice(Collection<Space> spaces){
        return spaces.stream().anyMatch(o -> o.getSellingPrice()!=null && o.getSellingPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    public static BigDecimal getHeaderFees(String feeName, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 proposalFeeResponse) {
        return proposalFeeResponse.getFees().getHeaderFees().stream()
                .filter(innerFee -> innerFee.getName().equalsIgnoreCase(feeName)).findFirst()
                .orElseThrow(() -> new BusinessException("Error while getting Fee : %s",feeName)).getFee();
    }

    public static BigDecimal getTotalRetailFeeFromProposalV2(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 proposalFeeRequest) {
        return getMapOfSpaceAndRetailFee(proposalFeeRequest).values().stream().reduce(BigDecimal::add)
                .orElseThrow(() -> new BusinessException("Error while calculating sum of retail prices"));
    }

    public static BigDecimal getTotalSellingPriceFromProposal(ProposalFeeRequestV2 proposalFeeRequest) {
        return getMapOfSpaceAndSellingFee(proposalFeeRequest).values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Map<String, BigDecimal> getMapOfSpaceAndSellingFee(ProposalFeeRequestV2 proposalFeeRequest) {
        return proposalFeeRequest.getSpaces().stream().filter(space -> space.getSellingPrice() != null)
                .collect(Collectors.toMap(Space::getSpaceId, Space::getSellingPrice, (x, y) -> x, LinkedHashMap::new));
    }

    public static Map<String, BigDecimal> getMapOfSpaceAndRetailFee(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 proposalFeeRequest) {
        return proposalFeeRequest.getSpaces().stream()
                .collect(Collectors.toMap(com.containerstore.prestonintegrations.proposal.models.Space::getSpaceId,
                        com.containerstore.prestonintegrations.proposal.models.Space::getRetailPrice, (x, y) -> x, LinkedHashMap::new));
    }

    public static Map<String, BigDecimal> getMapOfSpaceAndRetailFee(ProposalFeeRequest proposalFeeRequest) {
        return proposalFeeRequest.getSpaces().stream()
                .collect(Collectors.toMap(com.containerstore.prestonintegrations.proposal.models.Space::getSpaceId,
                        com.containerstore.prestonintegrations.proposal.models.Space::getRetailPrice, (x, y) -> x, LinkedHashMap::new));
    }

    public static BigDecimal getTotalRetailFeeFromProposal(ProposalFeeRequest proposalFeeRequest) {
        return getMapOfSpaceAndRetailFee(proposalFeeRequest).values().stream().reduce(BigDecimal::add)
                .orElseThrow(() -> new BusinessException("Error while calculating sum of retail prices"));
    }

    public static com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesHeaderFeesInner buildHeaderFees(BigDecimal fee, String feeName, String feeDescription) {
        var headerFees = new ProposalFeeResponseTaxableFeesHeaderFeesInner();
        headerFees.setName(feeName);
        headerFees.setDescription(feeDescription);
        headerFees.setFee(fee.setScale(2, RoundingMode.HALF_EVEN));
        return headerFees;
    }

    public static BigDecimal getTaxableTotalAdditionalFee(com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 proposalFeeResponse) {
        BigDecimal totalAdditionalFee = BigDecimal.ZERO;
        totalAdditionalFee = proposalFeeResponse.getFees().getHeaderFees().stream()
                .filter(innerFee -> innerFee.getName().equalsIgnoreCase(FeeType.ADDITIONAL_SERVICES_FEE.name())
                        || innerFee.getName().equalsIgnoreCase(FeeType.ADDITIONAL_SERVICES_TRANSPORTATION.name()))
                .map(ProposalFeeResponseTaxableFeesHeaderFeesInner::getFee).reduce(totalAdditionalFee, BigDecimal::add);
        return totalAdditionalFee;
    }

    public static BigDecimal getFeePerSpace(BigDecimal totalFee, BigDecimal totalRetailPrice, BigDecimal retailPrice) {
        return totalFee.multiply(retailPrice.divide(totalRetailPrice, 12, RoundingMode.FLOOR));
    }

    public static BigDecimal getLineFee(String spaceId, String feeName, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 proposalFeeResponse) {
        return proposalFeeResponse.getFees().getLineFees().stream()
                .filter(innerFee -> innerFee.getSpaceId().equalsIgnoreCase(spaceId) && innerFee.getName().equalsIgnoreCase(feeName))
                .map(com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner::getFee)
                .findFirst().orElse(BigDecimal.ZERO);
    }

    public static String makeLineItemId(String spaceId, String productType) {
        return spaceId.concat(productType);
    }

}
