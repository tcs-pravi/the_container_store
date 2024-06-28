package com.containerstore.prestonintegrations.proposal.salesforceintegration;

import com.containerstore.prestonintegrations.proposal.models.ProposalSpaceRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalSpaceRequestSpaceFeesInner;
import com.containerstore.prestonintegrations.proposal.models.SalesforceSaveProposalRequest;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequest;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequestFeesHeaderFeesInner;
import com.containerstore.prestonintegrations.proposal.offer.OfferService;
import com.containerstore.prestonintegrations.proposal.offer.dto.OfferCodeResponseEnvelope;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.InvalidOfferCodeInProposalException;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.SalesforceServiceClient;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.mapper.SaveProposalRequestMapper;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.SpaceValidationException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesforceIntegrationService {

    private final SalesforceServiceClient salesforceServiceClient;

    private final OfferService offerService;

    /**
     * This method validates offer code & space details in SaveProposalRequest.
     * If validation is successful, Create SalesforceSaveProposalRequest object & send it to salesforce.
     *
     * @param proposalRequest
     */
    public void sendProposalToSalesforce(SaveProposalRequest proposalRequest) {
        this.checkDuplicateSpaceIdInSaveProposalRequest(proposalRequest);
        this.validateOffers(proposalRequest);
        this.validateSpaces(proposalRequest);
        SalesforceSaveProposalRequest salesforceSaveProposalRequest = this.transformSaveProposalRequestForSalesforce(proposalRequest);
        salesforceServiceClient.saveProposal(salesforceSaveProposalRequest);
    }

    private void validateOffers(SaveProposalRequest proposalRequest) {
        if(proposalRequest.getOffers()!=null && !proposalRequest.getOffers().isEmpty()) {
            var invalidOffers = offerService.getPresentedOffer(proposalRequest.getOffers()).stream()
                    .filter(o -> !o.isValid() || o.hasValidationErrors()).toList();
            if (!invalidOffers.isEmpty())
                throw new InvalidOfferCodeInProposalException("Invalid offer codes: %s", invalidOffers.stream().map(OfferCodeResponseEnvelope::offerCode).toList().toString());
        }
    }

    public SalesforceSaveProposalRequest transformSaveProposalRequestForSalesforce(SaveProposalRequest saveProposalRequest) {
        SalesforceSaveProposalRequest salesforceSaveProposalRequest = this.createSalesforceSaveProposalRequest(saveProposalRequest);
        if(saveProposalRequest.getFees() != null && saveProposalRequest.getFees().getHeaderFees() != null){
            saveProposalRequest.getFees().getHeaderFees().forEach(fee ->
                    this.splitHeaderFee(fee, salesforceSaveProposalRequest.getSpaces())
            );
        }
        return salesforceSaveProposalRequest;
    }

    /**
     * This method splits header fee equally among the spaces & add it to spaceFees.
     * In case of unequal division due to rounding off, difference amount will be added to first space.
     *
     * @param headerFee
     * @param spaces
     */
    private void splitHeaderFee(SaveProposalRequestFeesHeaderFeesInner headerFee, Set<ProposalSpaceRequest> spaces) {
        var totalSpaceFee = BigDecimal.ZERO;
        var counter = 1;
        var firstSpaceFee = new ProposalSpaceRequestSpaceFeesInner();
        for(ProposalSpaceRequest space : spaces){
            if (headerFee.getName().equalsIgnoreCase(FeeType.ADDITIONAL_SERVICES_FEE.name())
                    || headerFee.getName().equalsIgnoreCase(FeeType.ADDITIONAL_SERVICES_TRANSPORTATION.name())) {
                var spaceFeeItem = this.getSpaceFeeItem(headerFee, spaces.size());
                space.addSpaceFeesItem(spaceFeeItem);

                totalSpaceFee = totalSpaceFee.add(spaceFeeItem.getFee());
                if(counter == 1)
                    firstSpaceFee = spaceFeeItem;
            }
            counter++;
        }
        this.addDifferenceAmountToFirstSpace(firstSpaceFee, headerFee.getFee(), totalSpaceFee);
    }

    private ProposalSpaceRequestSpaceFeesInner getSpaceFeeItem(SaveProposalRequestFeesHeaderFeesInner headerFee, int spacesSize) {
        var feePerSpace = headerFee.getFee().divide(BigDecimal.valueOf(spacesSize), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.DOWN);
        var spaceFee = new ProposalSpaceRequestSpaceFeesInner();
        spaceFee.setFee(feePerSpace);
        spaceFee.setName(headerFee.getName());
        return spaceFee;
    }

    private void addDifferenceAmountToFirstSpace(ProposalSpaceRequestSpaceFeesInner spaceFee, BigDecimal headerFee, BigDecimal totalSpaceFee) {
        spaceFee.setFee(spaceFee.getFee().add(headerFee.subtract(totalSpaceFee)));
    }

    /**
     * This method validates space details in SaveProposalRequest. If validation fails, SpaceValidationException will be thrown.
     *
     * @param saveProposalRequest
     * @throws SpaceValidationException
     */
    public void validateSpaces(SaveProposalRequest saveProposalRequest) throws SpaceValidationException {
        var validateSpaceResponses = salesforceServiceClient.validateSpaces(saveProposalRequest.getOpportunityId());
        if (validateSpaceResponses == null || validateSpaceResponses.isEmpty()) {
            throw new SpaceValidationException("Validations failed, Please check the CSD status in Salesforce for opportunity id: %s", saveProposalRequest.getOpportunityId());
        }
    }

    private void checkDuplicateSpaceIdInSaveProposalRequest(SaveProposalRequest saveProposalRequest) {
        if(saveProposalRequest.getSpaces().size() != saveProposalRequest.getSpaces().stream()
                .map(ProposalSpaceRequest::getSpaceId).distinct().count())
            throw new SpaceValidationException("Validations failed, SaveProposalRequest contains duplicate spaces");
    }

    private SalesforceSaveProposalRequest createSalesforceSaveProposalRequest(SaveProposalRequest saveProposalRequest){
        return SaveProposalRequestMapper.SAVE_PROPOSAL_REQUEST_MAPPER.saveProposalRequestToSalesforceSaveProposalRequest(saveProposalRequest);
    }
}
