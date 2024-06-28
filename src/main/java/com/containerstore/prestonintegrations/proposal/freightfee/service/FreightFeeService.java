package com.containerstore.prestonintegrations.proposal.freightfee.service;

import com.containerstore.prestonintegrations.proposal.freightfee.exception.StateNotFoundException;
import com.containerstore.prestonintegrations.proposal.freightfee.repository.StateRepository;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstants;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsRepository;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.exception.ProposalConstantNotFoundException;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreightFeeService {

    private final ProposalConstantsRepository proposalConstantsRepository;

    private final StateRepository stateRepository;

    private final StoreRepository storeRepository;

    /**
     * This method calculates freight fee.
     * If we receive only retail price in proposal fee request, calculate freight fee based on total retail price.
     * If we receive both retail & selling price, Use below formula to calculate freight fee.
     * Freight fee =  Freight fee based on total retail price + (Total Retail price - Total Selling price) + (installation fee using total retail price - installation fee using total selling price)
     *
     * @param proposalFeeRequest
     * @param proposalFeeResponse
     * @param mileageEntity
     * @param proposalFee
     */
    public void getFreightFeeForProposalV2(ProposalFeeRequestV2 proposalFeeRequest, ProposalFeeResponseV2 proposalFeeResponse, SalesforceMileageEntity mileageEntity, ProposalFee proposalFee) {
		var freightFeeBasedOnRetailPrice = this.calculateFreightFee(ProposalHelpers.getTotalRetailFeeFromProposalV2(proposalFeeRequest),proposalFeeRequest.getCustomerAddress().getState(), mileageEntity.getInstallationStoreId())
                .setScale(2, RoundingMode.HALF_EVEN);
        proposalFee.setFreightFeeBasedOnRetailPrice(freightFeeBasedOnRetailPrice);
        var freightFeeBasedOnSellingPrice = freightFeeBasedOnRetailPrice.add(getAdjustedValueBasedOnInstallationFee(proposalFee)).setScale(2, RoundingMode.HALF_EVEN);
		proposalFeeResponse.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(freightFeeBasedOnSellingPrice, FeeType.FREIGHT_FEE.name(), "Calculated Freight fees for opportunity id: %s".formatted(proposalFeeRequest.getOpportunityId())));
    }

    /**
     * This method calculates adjusted value based on below formula,
     * Adjusted value = (Total Retail price - Total Selling price) + (installation fee using total retail price - installation fee using total selling price)
     * @param proposalFee
     */
	private BigDecimal getAdjustedValueBasedOnInstallationFee(ProposalFee proposalFee) {
		if (proposalFee.getTotalSellingPriceInProposal().compareTo(BigDecimal.ZERO) > 0) {
			return proposalFee.getDifferenceBetweenRetailAndSellingPrice().add(proposalFee.getDifferenceBetweenInstallFeeBasedOnRetailAndSellingPrice());
		}
		return BigDecimal.ZERO;
	}

    public void getFreightFeeForProposal(ProposalFeeRequest proposalFeeRequest, ProposalFeeResponse proposalFeeResponse, SalesforceMileageEntity mileageEntity) {
        var freightFee = this.calculateFreightFee(ProposalHelpers.getTotalRetailFeeFromProposal(proposalFeeRequest), proposalFeeRequest.getCustomerAddress().getState(), mileageEntity.getInstallationStoreId()).setScale(2,RoundingMode.HALF_EVEN);
        proposalFeeResponse.getTaxableFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(freightFee, FeeType.FREIGHT_FEE.name(),
                "Calculated Freight fees for opportunity id: %s".formatted(proposalFeeRequest.getOpportunityId())));
    }

    private BigDecimal calculateFreightFee(BigDecimal totalRetailFee, String state, String installationStoreId) {
        return this.calculateFreightFee(totalRetailFee, this.getCostPerCrate(state.toUpperCase().trim(), installationStoreId));
    }

    private BigDecimal calculateFreightFee(BigDecimal totalRetailPrice, BigDecimal costPerCrate) {
        return costPerCrate.multiply(findNumberOfCratesNeeded(totalRetailPrice));
    }

    private BigDecimal getCostPerCrate(String stateAbbreviation, String installationStoreId) {
        return storeRepository.findStoreBySalesforceStoreId(installationStoreId)
                .filter(store -> store.isActive() && store.isFreightFeeEnabled())
                .map(this::getAdjustedCostPerCrate)
                .orElseGet(() -> getCostPerCrateForState(stateAbbreviation));
    }

    private BigDecimal getCostPerCrateForState(String stateAbbreviation) {
        return stateRepository.findStateByStateAbbreviation(stateAbbreviation)
                .map(s -> s.getZone().getCostPerCrate())
                .orElseThrow(() -> new StateNotFoundException("State '%s' not found in database".formatted(stateAbbreviation)));
    }

    private BigDecimal getAdjustedCostPerCrate(StoreEntity store) {
        BigDecimal costPerCrate = store.getState().getZone().getCostPerCrate();
        return switch (store.getAdjustmentType()) {
            case AMOUNT -> costPerCrate.add(store.getAdjustmentValue());
            case PERCENTAGE -> {
                BigDecimal percentAmount = (costPerCrate.multiply(store.getAdjustmentValue())).divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);
                yield costPerCrate.add(percentAmount);
            }
        };
    }

    private BigDecimal findNumberOfCratesNeeded(BigDecimal retailPrice) {
        return retailPrice.divide(this.getMaximumProductRetailValuePerCrate(), MathContext.DECIMAL32)
                .setScale(0, RoundingMode.CEILING);
    }

    private BigDecimal getMaximumProductRetailValuePerCrate() {
        return proposalConstantsRepository
                .findById(ProposalConstantKeys.MAX_RETAIL_VALUE_PER_CRATE.name())
                .map(ProposalConstants::getValue)
                .orElseThrow(() -> new ProposalConstantNotFoundException("Missing proposal constant in dabatase '%s'"
                        .formatted(ProposalConstantKeys.MAX_RETAIL_VALUE_PER_CRATE.name())));
    }
}
