package com.containerstore.prestonintegrations.proposal.rest.apps;

import com.containerstore.prestonintegrations.proposal.closetpro.feign.ClosetproServiceClient;
import com.containerstore.prestonintegrations.proposal.freightfee.service.FreightFeeService;
import com.containerstore.prestonintegrations.proposal.installationfee.InstallationFeeService;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequest;
import com.containerstore.prestonintegrations.proposal.models.Space;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.SalesforceIntegrationService;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.SpaceValidationException;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.SalesforceServiceClient;
import com.containerstore.prestonintegrations.proposal.shared.exception.InstallationStoreNotPresentException;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RequestMapping("/apps/closetpro/api")
@Slf4j
@RequiredArgsConstructor
@RestController
public class ClosetProProposalResource implements com.containerstore.prestonintegrations.proposal.controllers.V1Api {

    private final FreightFeeService freightFeeService;

    private final InstallationFeeService installationFeeService;

    private final SalesforceMileageService salesforceMileageService;

    private final SalesforceIntegrationService salesforceIntegrationService;

    private final ClosetproServiceClient closetproServiceClient;

    private final SalesforceServiceClient salesforceServiceClient;

    @Override
    public ResponseEntity<ProposalFeeResponse> calculateProposalFee(ProposalFeeRequest proposalFeeRequest) {
        this.checkForDuplicateSpaceIdInProposalFeeRequest(proposalFeeRequest);
        if(proposalFeeRequest.getSpaces().stream().anyMatch(space -> space.getSellingPrice() != null))
            throw new UnsupportedOperationException("Selling Price is not allowed in V1 get-fees request.");

        ProposalFeeResponse proposalFeeResponse = this.buildProposalFeeResponse(proposalFeeRequest);
        var mileage = this.getSalesforceMileageEntity(proposalFeeRequest.getOpportunityId())
                .orElseThrow(() -> new InstallationStoreNotPresentException("Installation and Freight Calculator unable to process due to missing store id, please reach out to Resource Centre"));
        log.info("Mileage information found for {} with installation store {}", proposalFeeRequest.getOpportunityId(), mileage.getInstallationStoreId());

        freightFeeService.getFreightFeeForProposal(proposalFeeRequest, proposalFeeResponse, mileage);
        installationFeeService.getInstallationFee(proposalFeeRequest, proposalFeeResponse, mileage);
        return ResponseEntity.status(200).body(proposalFeeResponse);
    }

    @Override
    public ResponseEntity<Void> eventToSendProposalPdf(String opportunityId, String proposalId) {
        var response = closetproServiceClient.downloadPdf(proposalId,"{}");
        var fileNameFromClosetPro = response.getHeaders().getContentDisposition().getFilename();
        salesforceServiceClient.uploadPdf(response.getBody(), opportunityId,fileNameFromClosetPro);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> sendProposalToSalesforce(SaveProposalRequest saveProposalRequest){
        salesforceIntegrationService.sendProposalToSalesforce(saveProposalRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    public ProposalFeeResponse buildProposalFeeResponse(ProposalFeeRequest proposalFeeRequest) {
        ProposalFeeResponse proposalFeeResponse = new ProposalFeeResponse();
        var taxableFee = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFees();
        var nonTaxableFee = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseNonTaxableFee();
        proposalFeeResponse.setTaxableFees(taxableFee);
        proposalFeeResponse.setNonTaxableFee(nonTaxableFee);
        proposalFeeResponse.setOpportunityId(proposalFeeRequest.getOpportunityId());
        return proposalFeeResponse;
    }

    private Optional<SalesforceMileageEntity> getSalesforceMileageEntity(String opportunityId) {
        Collection<SalesforceMileageEntity> mileageEntities = salesforceMileageService
                .getWebhookPersistedDataByKeyAndApp(opportunityId, WebHookConsumers.SALESFORCE);
        return mileageEntities.stream().findFirst();
    }

    private void checkForDuplicateSpaceIdInProposalFeeRequest(ProposalFeeRequest proposalFeeRequest) {
        if(proposalFeeRequest.getSpaces().size() != proposalFeeRequest.getSpaces().stream()
                .map(Space::getSpaceId).distinct().count())
            throw new SpaceValidationException("Validations failed, ProposalFeeRequest contains duplicate spaces");
    }
}
