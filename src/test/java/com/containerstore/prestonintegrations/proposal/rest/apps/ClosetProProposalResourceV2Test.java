package com.containerstore.prestonintegrations.proposal.rest.apps;


import com.containerstore.offer.domain.*;
import com.containerstore.prestonintegrations.proposal.additionalfee.TearOutFeeService;
import com.containerstore.prestonintegrations.proposal.additionalfee.TransportationFeeService;
import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import com.containerstore.prestonintegrations.proposal.freightfee.entity.Zone;
import com.containerstore.prestonintegrations.proposal.freightfee.repository.StateRepository;
import com.containerstore.prestonintegrations.proposal.freightfee.service.FreightFeeService;
import com.containerstore.prestonintegrations.proposal.installationfee.InstallationFeeService;
import com.containerstore.prestonintegrations.proposal.offer.OfferService;
import com.containerstore.prestonintegrations.proposal.offer.feign.OfferServiceClient;
import com.containerstore.prestonintegrations.proposal.rest.apps.v2.ClosetProProposalResourceV2;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstants;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsRepository;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsService;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxTransactionDTO;
import com.containerstore.prestonintegrations.proposal.tax.feign.EnterpriseTaxServiceClient;
import com.containerstore.prestonintegrations.proposal.tax.service.TaxService;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import com.containerstore.spring.boot.starters.rest.DefaultExceptionHandlers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@ActiveProfiles("local-test")
@SpringBootTest(classes = { ClosetProProposalResourceV2.class, FreightFeeService.class, InstallationFeeService.class,
        TearOutFeeService.class, TransportationFeeService.class, TaxService.class, OfferService.class})
public class ClosetProProposalResourceV2Test {

    private MockMvc mockMvc;

    @MockBean
    private ProposalConstantsService proposalConstantsService;

    @MockBean
    private StateRepository stateRepository;

    @MockBean
    private StoreRepository storeRepository;

    @MockBean
    private ProposalConstantsRepository proposalConstantsRepository;

    @Autowired
    private ClosetProProposalResourceV2 closetProProposalResourceV2;

    @MockBean
    private SalesforceTearOutService salesforceTearOutService;

    @MockBean
    private SalesforceMileageService salesforceMileageService;

    @MockBean
    private EnterpriseTaxServiceClient enterpriseTaxServiceClient;
    @MockBean
    private OfferServiceClient offerServiceClient;

    private SalesforceMileageEntity salesforceMileageEntity;
    private SalesforceTearOutEntity tearOut1, tearOut2, tearOut3, tearOut4;
    private OfferOrder offerOrder;
    private ImmutablePresentedOfferRequest offerRequest1, offerRequest2;
    private OfferResult offerResult1, offerResult2;

    @BeforeEach
    public void setMockMvc() {
        loadConstants();

        tearOut1 = new SalesforceTearOutEntity();
        tearOut1.setSpaceId("9c96c0e9-d7aa-4541-a473-8acd201ed360");
        tearOut1.setTearOutFee(BigDecimal.valueOf(50.000000));

        tearOut2 = new SalesforceTearOutEntity();
        tearOut2.setSpaceId("9c96c0e9-d7aa-4541-a473-8acd201ed364");
        tearOut2.setTearOutFee(BigDecimal.valueOf(20.000000));

        tearOut3 = new SalesforceTearOutEntity();
        tearOut3.setSpaceId("9c96c0e9-d7aa-4541-a473-8acd201ed365");
        tearOut3.setTearOutFee(BigDecimal.valueOf(100.000000));

        tearOut4 = new SalesforceTearOutEntity();
        tearOut4.setSpaceId("9c96c0e9-d7aa-4541-a473-8acd201ed366");
        tearOut4.setTearOutFee(BigDecimal.valueOf(1999.800000));

        salesforceMileageEntity = new SalesforceMileageEntity();
        salesforceMileageEntity.setOpportunityId("94e6b22ea-ec9a-40fc-a247-b90b2db7fea4");
        salesforceMileageEntity.setMiles(BigDecimal.valueOf(42.890000));
        salesforceMileageEntity.setInstallationStoreId("005-AUS");
        salesforceMileageEntity.setDurationValue(BigDecimal.valueOf(7200.000000));
        salesforceMileageEntity.setChronoUnit(ChronoUnit.SECONDS);

        Offer offer1 = Offer.builder()
                .withOfferCode("PRSTNEXP")
                .withOfferName("20% Off Preston for Experts")
                .withCustomAdjustmentValue(BigDecimal.valueOf(20))
                .withAdjustmentBenefits(List.of(
                        ImmutableAdjustmentBenefit.builder()
                                .withId(1L)
                                .withAdjustmentType(AdjustmentType.PERCENT_OFF)
                                .withAdjustmentValue(BigDecimal.valueOf(20))
                                .withAppliesTo(AppliesTo.PRESTON)
                                .build()
                )).build();

        Offer offer2 = Offer.builder()
                .withOfferCode("PRSTNAMTOFF")
                .withOfferName("$500 Off $2.5k Preston Test Offer")
                .withAdjustmentBenefits(List.of(
                        ImmutableAdjustmentBenefit.builder()
                                .withId(2L)
                                .withAdjustmentType(AdjustmentType.AMOUNT_OFF)
                                .withAdjustmentValue(BigDecimal.valueOf(500))
                                .withAppliesToRule("order.totalForPrestonWithBasicInstall.isGreaterThanOrEqualTo(toMoney(2500B)) && line.sku.departmentId == 25")
                                .withAppliesTo(AppliesTo.LINE)
                                .build()
                )).build();

        OfferOrderFulfillmentGroup group = new OfferOrderFulfillmentGroup();
        group.setFulfillmentGroupId("TCSCLOSETS");
        group.setOfferOrderLines(new ArrayList<>());

        offerOrder = OfferOrderBuilder.builder()
                .withRingStore(899)
                .withPurchasingChannel(PurchasingChannel.WEB)
                .withProspectiveOffers(List.of(offer1))
                .withShippingFeeType("SHIPPING7")
                .withSourceReferenceId("94e6b22ea-ec9a-40fc-a247-b90b2db7fea4")
                .withFulfillmentGroups(List.of(group))
                .withPurchasingCustomer(null)
                .build();
        offerOrder.addWinningOffer(offer2);
        offerRequest1 = ImmutablePresentedOfferRequest
                .builder()
                .withOfferCode("PRSTNEXP")
                .withRingStore(899)
                .build();
        offerRequest2 = ImmutablePresentedOfferRequest
                .builder()
                .withOfferCode("PRSTNAMTOFF")
                .withRingStore(899)
                .build();
        offerResult1 = new OfferResult(offer1, new ArrayList<>());
        offerResult2 = new OfferResult(offer2, new ArrayList<>());

        when(offerServiceClient.getPresentedOffer(any(ImmutablePresentedOfferRequest.class))).thenReturn(null);
        when(offerServiceClient.apply(any(OfferOrder.class))).thenReturn(offerOrder);

        mockMvc = MockMvcBuilders.standaloneSetup(closetProProposalResourceV2).setControllerAdvice(new DefaultExceptionHandlers()).build();
    }

    @Test
    @DisplayName("Test for calculateProposalFeeV2() with only retail price for each space")
    public void test_calculate_proposal_fee_V2_success() throws Exception {
        loadConstantsForSuccessScenario();

        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v2/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                       "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                       "customerAddress": {
                                         "address1": "Some Apartment 3",
                                         "address2": "That building, This Block",
                                         "city": "Brooklyn",
                                         "state": "NY",
                                         "zipCode": "10328"
                                       },
                                       "spaces": [
                                         {
                                           "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                           "retailPrice": 8000.12
                                         },
                                         {
                                           "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                           "retailPrice": 1000.21
                                         },
                                         {
                                           "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed367",
                                           "retailPrice": 1000.12
                                         }
                                       ],
                                       "customFees": [
                                         {
                                           "name": "Additional Freight",
                                           "fee": 2000,
                                           "percentage": null
                                         },
                                         {
                                           "name": "Additional Installation",
                                           "fee": 2000,
                                           "percentage": null
                                         }
                                       ],
                                       "offers": [
                                         {
                                           "offerCode": "PRSTNEXP"
                                         },
                                         {
                                           "offerCode": "PRSTNAMTOFF"
                                         }
                                       ]
                                     }"""))
                .andExpect(status().is(200))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String expectedResponse = """
                {
                           "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                           "fees": {
                               "HeaderFees": [
                                   {
                                       "name": "INSTALLATION_FEE",
                                       "description": "Installation calculated for proposal with opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                       "fee": 4150.19,
                                       "percentage": null
                                   },
                                   {
                                       "name": "FREIGHT_FEE",
                                       "description": "Calculated Freight fees for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                       "fee": 2000.00,
                                       "percentage": null
                                   },
                                   {
                                       "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                       "description": "Calculated Mileage fee for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                       "fee": 62.62,
                                       "percentage": null
                                   },
                                   {
                                       "name": "ADDITIONAL_SERVICES_FEE",
                                       "description": "Calculated Additional services fee for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                       "fee": 440.00,
                                       "percentage": null
                                   },
                                   {
                                       "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                       "description": "Total Tear out fee for the proposal",
                                       "fee": 2099.80,
                                       "percentage": null
                                   }
                               ],
                               "LineFees": [
                                   {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                       "name": "INSTALLATION_FEE",
                                       "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed365 ",
                                       "fee": 3320.05,
                                       "percentage": null
                                   },
                                   {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                       "name": "INSTALLATION_FEE",
                                       "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed366 ",
                                       "fee": 415.09,
                                       "percentage": null
                                   },
                                   {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed367",
                                       "name": "INSTALLATION_FEE",
                                       "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed367 ",
                                       "fee": 415.05,
                                       "percentage": null
                                   },
                                   {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                       "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                       "description": "Demolition charges provided by salesforce for space 9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                       "fee": 100.00,
                                       "percentage": null
                                   },
                                   {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                       "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                       "description": "Demolition charges provided by salesforce for space 9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                       "fee": 1999.80,
                                       "percentage": null
                                   }
                               ]
                           },
                           "customFees": [
                               {
                                   "name": "Additional Freight",
                                   "fee": 2000,
                                   "percentage": null
                               },
                               {
                                   "name": "Additional Installation",
                                   "fee": 2000,
                                   "percentage": null
                               }
                           ],
                           "offers": [
                               {
                                   "offerCode": "PRSTNEXP",
                                   "offerName": "20% Off Preston for Experts",
                                   "adjustmentValue": 20,
                                   "adjustmentType": "PERCENT_OFF",
                                   "amountOff": null,
                                   "hasApplied": false,
                                   "hasError": true,
                                   "errorMessage": "Offer code cannot be applied"
                               },
                               {
                                   "offerCode": "PRSTNAMTOFF",
                                   "offerName": "$500 Off $2.5k Preston Test Offer",
                                   "adjustmentValue": 500,
                                   "adjustmentType": "AMOUNT_OFF",
                                   "amountOff": 500,
                                   "hasApplied": true,
                                   "hasError": false,
                                   "errorMessage": null
                               }
                           ],
                           "tax": [
                               {
                                   "taxName": "Total Tax",
                                   "amount": 1460.24,
                                   "hasError": false,
                                   "errorMessage": null
                               }
                           ]
                       }""";

        JsonElement actualJson = JsonParser.parseString(actualResponse);
        JsonElement expectedJson = JsonParser.parseString(expectedResponse);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    @DisplayName("Test for calculateProposalFeeV2() with selling price for each space")
    public void test_calculate_proposal_fee_V2_success_2() throws Exception {
        loadConstantsForSuccessScenario();

        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v2/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                       "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                       "customerAddress": {
                                         "address1": "Some Apartment 3",
                                         "address2": "That building, This Block",
                                         "city": "Brooklyn",
                                         "state": "NY",
                                         "zipCode": "10328"
                                       },
                                       "spaces": [
                                         {
                                           "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                           "retailPrice": 8000.12,
                                           "sellingPrice": 7000
                                         },
                                         {
                                           "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                           "retailPrice": 1000,
                                           "sellingPrice": 1000
                                         },
                                         {
                                           "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed367",
                                           "retailPrice": 1000,
                                           "sellingPrice": 700
                                         }
                                       ],
                                       "customFees": [
                                         {
                                           "name": "Additional Freight",
                                           "fee": 2000,
                                           "percentage": null
                                         },
                                         {
                                           "name": "Additional Installation",
                                           "fee": 2000,
                                           "percentage": null
                                         }
                                       ],
                                       "offers": [
                                         {
                                           "offerCode": "PRSTNEXP"
                                         },
                                         {
                                           "offerCode": "PRSTNAMTOFF"
                                         }
                                       ]
                                     }"""))
                .andExpect(status().is(200))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String expectedResponse = """
                {
                            "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                            "fees": {
                                "HeaderFees": [
                                    {
                                        "name": "INSTALLATION_FEE",
                                        "description": "Installation calculated for proposal with opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                        "fee": 3610.50,
                                        "percentage": null
                                    },
                                    {
                                        "name": "FREIGHT_FEE",
                                        "description": "Calculated Freight fees for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                        "fee": 3839.67,
                                        "percentage": null
                                    },
                                    {
                                        "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                        "description": "Calculated Mileage fee for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                        "fee": 62.62,
                                        "percentage": null
                                    },
                                    {
                                        "name": "ADDITIONAL_SERVICES_FEE",
                                        "description": "Calculated Additional services fee for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                        "fee": 440.00,
                                        "percentage": null
                                    },
                                    {
                                        "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                        "description": "Total Tear out fee for the proposal",
                                        "fee": 2099.80,
                                        "percentage": null
                                    }
                                ],
                                "LineFees": [
                                    {
                                        "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                        "name": "INSTALLATION_FEE",
                                        "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed365 ",
                                        "fee": 2905.00,
                                        "percentage": null
                                    },
                                    {
                                        "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                        "name": "INSTALLATION_FEE",
                                        "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed366 ",
                                        "fee": 415.00,
                                        "percentage": null
                                    },
                                    {
                                        "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed367",
                                        "name": "INSTALLATION_FEE",
                                        "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed367 ",
                                        "fee": 290.50,
                                        "percentage": null
                                    },
                                    {
                                        "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                        "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                        "description": "Demolition charges provided by salesforce for space 9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                        "fee": 100.00,
                                        "percentage": null
                                    },
                                    {
                                        "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                        "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                        "description": "Demolition charges provided by salesforce for space 9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                        "fee": 1999.80,
                                        "percentage": null
                                    }
                                ]
                            },
                            "customFees": [
                                {
                                    "name": "Additional Freight",
                                    "fee": 2000,
                                    "percentage": null
                                },
                                {
                                    "name": "Additional Installation",
                                    "fee": 2000,
                                    "percentage": null
                                }
                            ],
                            "offers": [
                                {
                                    "offerCode": "PRSTNEXP",
                                    "offerName": "20% Off Preston for Experts",
                                    "adjustmentValue": 20,
                                    "adjustmentType": "PERCENT_OFF",
                                    "amountOff": null,
                                    "hasApplied": false,
                                    "hasError": true,
                                    "errorMessage": "Offer code cannot be applied"
                                },
                                {
                                    "offerCode": "PRSTNAMTOFF",
                                    "offerName": "$500 Off $2.5k Preston Test Offer",
                                    "adjustmentValue": 500,
                                    "adjustmentType": "AMOUNT_OFF",
                                    "amountOff": 500,
                                    "hasApplied": true,
                                    "hasError": false,
                                    "errorMessage": null
                                }
                            ],
                            "tax": [
                                {
                                    "taxName": "Total Tax",
                                    "amount": 1460.24,
                                    "hasError": false,
                                    "errorMessage": null
                                }
                            ]
                        }""";

        JsonElement actualJson = JsonParser.parseString(actualResponse);
        JsonElement expectedJson = JsonParser.parseString(expectedResponse);
        assertEquals(expectedJson, actualJson);
    }

    private void loadConstantsForSuccessScenario() throws JsonProcessingException {
        Optional<State> state = getState("NY", BigDecimal.valueOf(1000));
        when(stateRepository.findStateByStateAbbreviation("NY")).thenReturn(state);
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(state.get()));

        when(enterpriseTaxServiceClient.calculateTax(any(TaxTransactionDTO.class)))
                .thenReturn(new ObjectMapper().readTree("""
                        {
                          "totalTax": 1460.24
                        }"""));

        when(salesforceMileageService.getWebhookPersistedDataByKeyAndApp(any(), any()))
                .thenReturn(List.of(salesforceMileageEntity));
        when(salesforceTearOutService.getWebhookPersistedDataByKeyAndApp("94e6b22ea-ec9a-40fc-a247-b90b2db7fea4", WebHookConsumers.SALESFORCE))
                .thenReturn(List.of(tearOut1, tearOut2, tearOut3, tearOut4));

        when(offerServiceClient.getPresentedOffer(offerRequest1)).thenReturn(offerResult1);
        when(offerServiceClient.getPresentedOffer(offerRequest2)).thenReturn(offerResult2);

        when(offerServiceClient.apply(any(OfferOrder.class))).thenReturn(offerOrder);
    }


    private void loadConstants() {
        ProposalConstants averageHotelFee = new ProposalConstants();
        averageHotelFee.setValue(BigDecimal.valueOf(150));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.AVERAGE_HOTEL_FEE))
                .thenReturn(averageHotelFee);
        ProposalConstants perDiemPerInstaller = new ProposalConstants();
        perDiemPerInstaller.setValue(BigDecimal.valueOf(35));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.AVERAGE_PER_DIEM_FEE))
                .thenReturn(perDiemPerInstaller);
        ProposalConstants noOfInstallers = new ProposalConstants();
        noOfInstallers.setValue(BigDecimal.valueOf(2));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.NO_OF_INSTALLERS))
                .thenReturn(noOfInstallers);
        ProposalConstants threshold = new ProposalConstants();
        threshold.setValue(BigDecimal.valueOf(9000));
        when(proposalConstantsService
                .getProposalConstants(ProposalConstantKeys.THRESHOLD_AMOUNT_TO_CALCULATE_INSTALL_DAYS))
                .thenReturn(threshold);
        ProposalConstants minimumDriveTime = new ProposalConstants();
        minimumDriveTime.setValue(BigDecimal.valueOf(1.5));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_DRIVE_TIME))
                .thenReturn(minimumDriveTime);
        ProposalConstants minimumMileage = new ProposalConstants();
        minimumMileage.setValue(BigDecimal.valueOf(25));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_MILEAGE))
                .thenReturn(minimumMileage);
        ProposalConstants costPerMile = new ProposalConstants();
        costPerMile.setValue(BigDecimal.valueOf(3.5));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.COST_PER_MILE)).thenReturn(costPerMile);
        ProposalConstants maxRetailValue = new ProposalConstants();
        maxRetailValue.setKey(MAX_RETAIL_VALUE_PER_CRATE.name());
        maxRetailValue.setValue(BigDecimal.valueOf(9999.99));
        when(proposalConstantsService.getProposalConstants(MAX_RETAIL_VALUE_PER_CRATE)).thenReturn(maxRetailValue);
        when(proposalConstantsRepository.findById(MAX_RETAIL_VALUE_PER_CRATE.name())).thenReturn(Optional.of(maxRetailValue));
        ProposalConstants minimumInstallFee = new ProposalConstants();
        minimumInstallFee.setKey(MINIMUM_INSTALLATION_FEE.name());
        minimumInstallFee.setValue(BigDecimal.valueOf(1000));
        when(proposalConstantsService.getProposalConstants(MINIMUM_INSTALLATION_FEE)).thenReturn(minimumInstallFee);
        ProposalConstants installRate = new ProposalConstants();
        installRate.setKey(INSTALLATION_RATE.name());
        installRate.setValue(BigDecimal.valueOf(0.415));
        when(proposalConstantsService.getProposalConstants(INSTALLATION_RATE)).thenReturn(installRate);
    }

    private Optional<StoreEntity> getStore(State state) {
        StoreEntity store = new StoreEntity();
        store.setFreightFeeEnabled(false);
        store.setAdjustmentType(com.containerstore.prestonintegrations.proposal.store.AdjustmentType.AMOUNT);
        store.setAdjustmentValue(BigDecimal.valueOf(100));
        store.setSalesforceStoreId("store_id");
        store.setState(state);
        store.setActive(true);
        return Optional.of(store);
    }

    private Optional<State> getState(String stateName, BigDecimal costPerCrate) {
        Zone zone = null;
        if (costPerCrate != null) {
            zone = new Zone();
            zone.setZoneNumber(1);
            zone.setId(UUID.randomUUID());
            zone.setCostPerCrate(costPerCrate);
        }

        State stateObj;
        Optional<State> state = Optional.empty();
        if (stateName != null) {
            stateObj = new State();
            stateObj.setStateName(stateName);
            stateObj.setId(UUID.randomUUID());
            stateObj.setStateAbbreviation(stateName);
            stateObj.setZone(zone);
            state = Optional.of(stateObj);
        }
        return state;
    }

    @Test
    @DisplayName("Test for calculateProposalFeeV2() should throw SellingPriceGreaterThanRetailPriceException")
    void testCalculateProposalFeeV2_shouldThrowSellingPriceGreaterThanRetailPriceException() throws Exception {
            mockMvc.perform(post("/apps/closetpro/api/v2/proposal/fees/get-fees")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                   "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                   "customerAddress": {
                                     "address1": "Some Apartment 3",
                                     "address2": "That building, This Block",
                                     "city": "Brooklyn",
                                     "state": "NY",
                                     "zipCode": "10328"
                                   },
                                   "spaces": [
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                       "retailPrice": 8000.12,
                                       "sellingPrice": 9000
                                     },
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                       "retailPrice": 1000.21,
                                        "sellingPrice": 9000
                                     },
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed367",
                                       "retailPrice": 1000.12,
                                       "sellingPrice": 9000
                                     }
                                   ],
                                   "customFees": [
                                     {
                                       "name": "Additional Freight",
                                       "fee": 2000,
                                       "percentage": null
                                     },
                                     {
                                       "name": "Additional Installation",
                                       "fee": 2000,
                                       "percentage": null
                                     }
                                   ],
                                   "offers": [
                                     {
                                       "offerCode": "PRSTNEXP"
                                     },
                                     {
                                       "offerCode": "PRSTNAMTOFF"
                                     }
                                   ]
                                 }"""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                    .andExpect(jsonPath("$.message").value("An increase in Selling Price is not allowed. Please consult with the Administrator."))
                    .andDo(print());
    }

    @Test
    @DisplayName("Test for calculateProposalFeeV2() should throw MissingSellingPriceForSpaceException")
    void testCalculateProposalFeeV2_shouldThrowMissingSellingPriceForSpaceException() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v2/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                   "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                   "customerAddress": {
                                     "address1": "Some Apartment 3",
                                     "address2": "That building, This Block",
                                     "city": "Brooklyn",
                                     "state": "NY",
                                     "zipCode": "10328"
                                   },
                                   "spaces": [
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                       "retailPrice": 8000.12,
                                       "sellingPrice": 9000
                                     },
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                       "retailPrice": 1000.21
                                     }
                                   ],
                                   "customFees": [
                                     {
                                       "name": "Additional Freight",
                                       "fee": 2000,
                                       "percentage": null
                                     },
                                     {
                                       "name": "Additional Installation",
                                       "fee": 2000,
                                       "percentage": null
                                     }
                                   ],
                                   "offers": [
                                     {
                                       "offerCode": "PRSTNEXP"
                                     },
                                     {
                                       "offerCode": "PRSTNAMTOFF"
                                     }
                                   ]
                                 }"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Selling price is missing for space 9c96c0e9-d7aa-4541-a473-8acd201ed366"))
                .andDo(print());
    }

    @Test
    @DisplayName("Test for calculateProposalFeeV2() should throw InstallationStoreNotPresentException")
    void testCalculateProposalFeeV2_shouldThrowInstallationStoreNotPresentException() throws Exception {
        when(salesforceMileageService.getWebhookPersistedDataByKeyAndApp("94e6b22ea-ec9a-40fc-a247-b90b2db7fea4", WebHookConsumers.SALESFORCE)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/apps/closetpro/api/v2/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                   "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                   "customerAddress": {
                                     "address1": "Some Apartment 3",
                                     "address2": "That building, This Block",
                                     "city": "Brooklyn",
                                     "state": "NY",
                                     "zipCode": "10328"
                                   },
                                   "spaces": [
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                       "retailPrice": 8000.12,
                                       "sellingPrice": 7000
                                     },
                                     {
                                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                                       "retailPrice": 1000.21,
                                       "sellingPrice": 700
                                     }
                                   ],
                                   "customFees": [
                                     {
                                       "name": "Additional Freight",
                                       "fee": 2000,
                                       "percentage": null
                                     },
                                     {
                                       "name": "Additional Installation",
                                       "fee": 2000,
                                       "percentage": null
                                     }
                                   ],
                                   "offers": [
                                     {
                                       "offerCode": "PRSTNEXP"
                                     },
                                     {
                                       "offerCode": "PRSTNAMTOFF"
                                     }
                                   ]
                                 }"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Installation and Freight Calculator unable to process due to missing store id, please reach out to Resource Centre"))
                .andDo(print());
    }

}
