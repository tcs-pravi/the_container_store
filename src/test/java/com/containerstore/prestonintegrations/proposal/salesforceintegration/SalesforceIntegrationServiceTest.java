package com.containerstore.prestonintegrations.proposal.salesforceintegration;

import com.containerstore.common.base.validation.ValidationResult;
import com.containerstore.common.base.validation.ValidationSeverity;
import com.containerstore.prestonintegrations.proposal.models.ProposalSpaceRequest;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequest;
import com.containerstore.prestonintegrations.proposal.offer.OfferService;
import com.containerstore.prestonintegrations.proposal.offer.OfferServiceTestHelper;
import com.containerstore.prestonintegrations.proposal.offer.dto.OfferCodeResponseEnvelope;
import com.containerstore.prestonintegrations.proposal.offer.feign.OfferServiceClient;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.InvalidOfferCodeInProposalException;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.SpaceValidationException;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.SalesforceServiceClient;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.ValidateSpaceResponse;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequestFees;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequestFeesHeaderFeesInner;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.mapper.SaveProposalRequestMapper;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.models.Offer;
import com.containerstore.prestonintegrations.proposal.models.SalesforceSaveProposalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SalesforceIntegrationServiceTest {

    @InjectMocks
    SalesforceIntegrationService service;

    @Mock
    private SalesforceServiceClient salesforceServiceClient;

    @Mock
    private OfferServiceClient offerServiceClient;

    @Mock
    private OfferService offerService;

    @Test
    public void test_EmptySpaceResponse(){
        when(salesforceServiceClient.validateSpaces(anyString())).thenReturn(Collections.emptyList());
        assertThrows(SpaceValidationException.class,
                () -> service.validateSpaces(getSaveProposalRequest()),
                "Validations failed, Please check the CSD status in Salesforce for opportunity id: id");
    }

    @Test
    public void test_invalidSaveProposalRequest(){
        var request = getSaveProposalRequest();
        var spaces = new ArrayList<>(request.getSpaces());
        ProposalSpaceRequest space = new ProposalSpaceRequest();
        space.setSpaceId("7724245");
        space.setRetailPrice(BigDecimal.valueOf(100));
        spaces.add(space);
        request.setSpaces(spaces);
        assertThrows(SpaceValidationException.class,
                () -> service.sendProposalToSalesforce(request),"Validations failed, SaveProposalRequest contains duplicate spaces");
    }

    @Test
    public void test_NullSFResponse(){
        when(salesforceServiceClient.validateSpaces(anyString())).thenReturn(null);
        assertThrows(SpaceValidationException.class,
                () -> service.validateSpaces(getSaveProposalRequest()),
                "Validations failed, Please check the CSD status in Salesforce for opportunity id: id");
    }

    @Test
    public void test_Invalid_OfferCode(){
        SaveProposalRequest request = getSaveProposalRequest();
        Offer offer1 = new Offer();
        offer1.setOfferCode("PRESTOC");
        var offers = new ArrayList<Offer>();
        offers.add(offer1);
        request.setOffers(offers);

        List<ValidationResult> validationResults = new ArrayList<>();
        ValidationResult result = new ValidationResult();
        result.setCode("OFFER-EXPIRED");
        result.setSeverity(ValidationSeverity.ERROR);
        result.setMessage("Offer PRESTOC is no longer valid; it expired on Nov 20, 2023");
        validationResults.add(result);

        com.containerstore.offer.domain.Offer nonWinningOffer = com.containerstore.offer.domain.Offer.builder().withOfferName("20% Off Preston")
                .withOfferCode("PRESTOC")
                .withAdjustmentBenefits(OfferServiceTestHelper.getAdjustmentBenefitsForAmountOff())
                .build();

        var offer = new OfferCodeResponseEnvelope(Optional.ofNullable(nonWinningOffer), "PRESTOC", "OK", validationResults);
        when(offerService.getPresentedOffer(any())).thenReturn(List.of(offer));
        assertThrows(InvalidOfferCodeInProposalException.class,
                () -> service.sendProposalToSalesforce(request),"Invalid offer codes: [PRESTOC]");
    }

    @Test
    public void test_Invalid_OfferCode2(){
        SaveProposalRequest request = getSaveProposalRequest();
        Offer offer1 = new Offer();
        offer1.setOfferCode("PRESTOC");
        var offers = new ArrayList<Offer>();
        offers.add(offer1);
        request.setOffers(offers);

        List<ValidationResult> validationResults = new ArrayList<>();
        ValidationResult result = new ValidationResult();
        result.setCode("OFFER-EXPIRED");
        result.setSeverity(ValidationSeverity.ERROR);
        result.setMessage("Offer PRESTOC is no longer valid; it expired on Nov 20, 2023");
        validationResults.add(result);
        var offer = new OfferCodeResponseEnvelope(Optional.empty(), "PRESTOC", "OK", validationResults);
        when(offerService.getPresentedOffer(any())).thenReturn(List.of(offer));
        assertThrows(InvalidOfferCodeInProposalException.class,
                () -> service.sendProposalToSalesforce(request),"Invalid offer codes: [PRESTOC]");
    }

    @Test
    public void test_transformSaveProposalRequest(){
        SaveProposalRequest request = getSaveProposalRequest();
        var result = service.transformSaveProposalRequestForSalesforce(request);
        assertEquals(getExpectedSaveProposalRequest(request), result);
        assertNotNull(result);
        assertNotNull(result.getSpaces().stream().findAny().get().getSpaceFees().get(0));
        assertNotNull(result.getSpaces().stream().findAny().get().getSpaceFees().get(0).getFee());
        assertNotNull(result.getSpaces().stream().findAny().get().getSpaceFees().get(0).getName());
    }

    private SalesforceSaveProposalRequest getExpectedSaveProposalRequest(SaveProposalRequest saveProposalRequest) {
        return SaveProposalRequestMapper.SAVE_PROPOSAL_REQUEST_MAPPER.saveProposalRequestToSalesforceSaveProposalRequest(saveProposalRequest);
    }

    @Test
    public void test_sendProposal(){
        var validateSpaceResponse1 = new ValidateSpaceResponse("id", "open", "7724244");
        var validateSpaceResponse2 = new ValidateSpaceResponse("id", "open", "7724245");
        var validateSpaceResponse3 = new ValidateSpaceResponse("id", "open", "7724246");
        when(salesforceServiceClient.validateSpaces(anyString())).thenReturn(List.of(validateSpaceResponse1, validateSpaceResponse2,
                validateSpaceResponse3));
        doNothing().when(salesforceServiceClient).saveProposal(any());
        service.sendProposalToSalesforce(getSaveProposalRequest());
        verify(salesforceServiceClient).validateSpaces(anyString());
        verify(salesforceServiceClient).saveProposal(any());
    }

    private SaveProposalRequest getSaveProposalRequest(){
        var saveProposalRequest = new SaveProposalRequest();
        saveProposalRequest.setOpportunityId("id");
        ProposalSpaceRequest space1 = new ProposalSpaceRequest();
        space1.setSpaceId("7724244");
        ProposalSpaceRequest space2 = new ProposalSpaceRequest();
        space2.setSpaceId("7724245");
        ProposalSpaceRequest space3 = new ProposalSpaceRequest();
        space3.setSpaceId("7724246");
        var spaces = List.of(space1, space2, space3);
        saveProposalRequest.setSpaces(spaces);
        SaveProposalRequestFees fees = new SaveProposalRequestFees();
        SaveProposalRequestFeesHeaderFeesInner headerFee = new SaveProposalRequestFeesHeaderFeesInner();
        headerFee.setFee(BigDecimal.valueOf(1000));
        headerFee.setName(FeeType.ADDITIONAL_SERVICES_FEE.name());
        var headerFeeList = List.of(headerFee);
        fees.setHeaderFees(headerFeeList);
        saveProposalRequest.setFees(fees);
        return  saveProposalRequest;
    }
}
