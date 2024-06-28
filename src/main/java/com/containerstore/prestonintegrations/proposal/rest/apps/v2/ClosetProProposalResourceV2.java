package com.containerstore.prestonintegrations.proposal.rest.apps.v2;

import com.containerstore.prestonintegrations.proposal.additionalfee.TearOutFeeService;
import com.containerstore.prestonintegrations.proposal.additionalfee.TransportationFeeService;
import com.containerstore.prestonintegrations.proposal.freightfee.service.FreightFeeService;
import com.containerstore.prestonintegrations.proposal.installationfee.InstallationFeeService;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFees;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.models.Space;
import com.containerstore.prestonintegrations.proposal.offer.OfferService;
import com.containerstore.prestonintegrations.proposal.offer.dto.DiscountedLineItems;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.SpaceValidationException;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.shared.exception.InstallationStoreNotPresentException;
import com.containerstore.prestonintegrations.proposal.shared.exception.MissingSellingPriceForSpaceException;
import com.containerstore.prestonintegrations.proposal.shared.exception.SellingPriceGreaterThanRetailPriceException;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.tax.service.TaxService;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;

import java.util.*;

@RequestMapping("/apps/closetpro/api")
@Slf4j
@RequiredArgsConstructor
@RestController
public class ClosetProProposalResourceV2 implements com.containerstore.prestonintegrations.proposal.controllers.V2Api {

    private final FreightFeeService freightFeeService;

    private final InstallationFeeService installationFeeService;

    private final TaxService taxService;

    private final TearOutFeeService tearOutFeeService;

    private final TransportationFeeService transportationFeeService;

    private final SalesforceMileageService salesforceMileageService;

    private final OfferService offerService;

    @Override
    public ResponseEntity<ProposalFeeResponseV2> calculateProposalFeeV2(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 proposalFeeRequest) {

        this.validateProposalFeeRequest(proposalFeeRequest);

        ProposalFeeResponseV2 proposalFeeResponse = this.buildProposalFeeResponse(proposalFeeRequest);


        var mileage = this.getSalesforceMileageEntity(proposalFeeRequest.getOpportunityId())
                .orElseThrow(() -> new InstallationStoreNotPresentException("Installation and Freight Calculator unable to process due to missing store id, please reach out to Resource Centre"));

        log.info("Mileage information found for {} with installation store {}", proposalFeeRequest.getOpportunityId(), mileage.getInstallationStoreId());

        ProposalFee proposalFee = installationFeeService.getInstallationFeeV2(proposalFeeRequest, proposalFeeResponse, mileage);
        freightFeeService.getFreightFeeForProposalV2(proposalFeeRequest, proposalFeeResponse, mileage, proposalFee);
        transportationFeeService.getMileageFee(proposalFeeRequest.getOpportunityId(), proposalFeeResponse, mileage);
        transportationFeeService.getHotelAndPerDiemFee(proposalFeeRequest, proposalFeeResponse, mileage, proposalFee);
        tearOutFeeService.getTearOutFee(proposalFeeRequest, proposalFeeResponse);

        List<DiscountedLineItems> discountedLineItemsList = new ArrayList<>();
        if (proposalFeeRequest.getOffers() != null && !proposalFeeRequest.getOffers().isEmpty())
            discountedLineItemsList = offerService.applyOnProposal(proposalFeeRequest, proposalFeeResponse);
        taxService.getTax(proposalFeeRequest, proposalFeeResponse, discountedLineItemsList);
        proposalFeeResponse.setCustomFees(proposalFeeRequest.getCustomFees());
        return ResponseEntity.status(200).body(proposalFeeResponse);
    }

    private Optional<SalesforceMileageEntity> getSalesforceMileageEntity(String opportunityId) {
        Collection<SalesforceMileageEntity> mileageEntities = salesforceMileageService
                .getWebhookPersistedDataByKeyAndApp(opportunityId, WebHookConsumers.SALESFORCE);
        return mileageEntities.stream().findFirst();
    }

    public ProposalFeeResponseV2 buildProposalFeeResponse(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 proposalFeeRequest) {
        ProposalFeeResponseV2 proposalFeeResponse = new ProposalFeeResponseV2();
        var taxableFee = new ProposalFeeResponseTaxableFees();
        proposalFeeResponse.setFees(taxableFee);
        proposalFeeResponse.setOpportunityId(proposalFeeRequest.getOpportunityId());
        return proposalFeeResponse;
    }

    private void validateProposalFeeRequest(ProposalFeeRequestV2 proposalFeeRequest) {
        checkForDuplicateSpaceIdInProposalFeeRequest(proposalFeeRequest);
        validateSellingPricesForSpaces(proposalFeeRequest);
    }

    private void validateSellingPricesForSpaces(ProposalFeeRequestV2 proposalFeeRequest) {
        List<Space> spaces = proposalFeeRequest.getSpaces();

        Optional<Space> spaceWithNoSellingPrice = spaces.stream().filter(space -> space.getSellingPrice() == null).findFirst();

        if (Boolean.TRUE.equals(ProposalHelpers.hasSellingPrice(spaces))) {
            if (spaceWithNoSellingPrice.isPresent()) {
                throw new MissingSellingPriceForSpaceException("Selling price is missing for space %s", spaceWithNoSellingPrice.get().getSpaceId());
            }

            if (proposalFeeRequest.getSpaces().stream()
                    .anyMatch(space -> space.getRetailPrice().compareTo(space.getSellingPrice()) < 0)) {
                throw new SellingPriceGreaterThanRetailPriceException("An increase in Selling Price is not allowed. Please consult with the Administrator.");
            }
            //Remove selling price if selling price & retail price are equal for all spaces
            Optional<Space> spaceWithDifferentSellingRetailPrice = spaces.stream().filter(space -> !space.getSellingPrice().equals(space.getRetailPrice())).findFirst();
            if(spaceWithDifferentSellingRetailPrice.isEmpty())
                spaces.forEach(space -> space.setSellingPrice(null));
        }
    }

    private void checkForDuplicateSpaceIdInProposalFeeRequest(ProposalFeeRequestV2 proposalFeeRequest) {
        if(proposalFeeRequest.getSpaces().size() != proposalFeeRequest.getSpaces().stream()
                .map(Space::getSpaceId).distinct().count())
            throw new SpaceValidationException("Validations failed, ProposalFeeRequest contains duplicate spaces");
    }
}
