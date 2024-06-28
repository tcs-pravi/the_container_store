package com.containerstore.prestonintegrations.proposal.rest;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import com.containerstore.prestonintegrations.proposal.freightfee.entity.Zone;
import com.containerstore.prestonintegrations.proposal.freightfee.repository.StateRepository;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse;
import com.containerstore.prestonintegrations.proposal.offer.OfferService;
import com.containerstore.prestonintegrations.proposal.offer.feign.OfferServiceClient;
import com.containerstore.prestonintegrations.proposal.rest.apps.ClosetProProposalResource;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.SalesforceServiceClient;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.ValidateSpaceResponse;
import com.containerstore.prestonintegrations.proposal.shared.exception.ProposalGlobalExceptionHandler;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstants;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsRepository;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsService;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.tax.feign.EnterpriseTaxServiceClient;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import com.containerstore.spring.boot.starters.rest.RestError;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ProposalControllerTest {

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
    private ClosetProProposalResource closetProProposalResource;

    @MockBean
    private SalesforceTearOutService salesforceTearOutService;

    @MockBean
    private SalesforceMileageService salesforceMileageService;

    @MockBean
    private EnterpriseTaxServiceClient enterpriseTaxServiceClient;

    @MockBean
    private SalesforceServiceClient salesforceServiceClient;

    @MockBean
    private OfferServiceClient offerServiceClient;

    private SalesforceMileageEntity salesforceMileageEntity;

    @MockBean
    private OfferService offerService;

    @BeforeEach
    public void setMockMvc(){
        mockMvc = MockMvcBuilders.standaloneSetup(closetProProposalResource).setControllerAdvice(new ProposalGlobalExceptionHandler()).build();
        salesforceMileageEntity = new SalesforceMileageEntity();
        salesforceMileageEntity.setOpportunityId("94e6b22ea-ec9a-40fc-a247-b90b2db7fea4");
        salesforceMileageEntity.setMiles(BigDecimal.valueOf(42.890000));
        salesforceMileageEntity.setInstallationStoreId("005-AUS");
        salesforceMileageEntity.setDurationValue(BigDecimal.valueOf(7200.000000));
        salesforceMileageEntity.setChronoUnit(ChronoUnit.SECONDS);
    }

    @Test
    public void test_calculate_proposal_fee() throws Exception {
        Optional<State> state = getState("NY", BigDecimal.valueOf(1000));
        when(stateRepository.findStateByStateAbbreviation("NY")).thenReturn(state);
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(state.get()));
        when(salesforceMileageService.getWebhookPersistedDataByKeyAndApp(any(), any()))
                .thenReturn(List.of(salesforceMileageEntity));
        ProposalConstants maxRetailValue = new ProposalConstants();
        maxRetailValue.setKey(MAX_RETAIL_VALUE_PER_CRATE.name());
        maxRetailValue.setValue(BigDecimal.valueOf(9999.99));

        ProposalConstants minimumInstallFee = new ProposalConstants();
        minimumInstallFee.setKey(MINIMUM_INSTALLATION_FEE.name());
        minimumInstallFee.setValue(BigDecimal.valueOf(1000));

        ProposalConstants installRate = new ProposalConstants();
        installRate.setKey(INSTALLATION_RATE.name());
        installRate.setValue(BigDecimal.valueOf(0.415));

        when(proposalConstantsService.getProposalConstants(MAX_RETAIL_VALUE_PER_CRATE)).thenReturn(maxRetailValue);
        when(proposalConstantsRepository.findById(MAX_RETAIL_VALUE_PER_CRATE.name())).thenReturn(Optional.of(maxRetailValue));
        when(proposalConstantsService.getProposalConstants(MINIMUM_INSTALLATION_FEE)).thenReturn(minimumInstallFee);
        when(proposalConstantsService.getProposalConstants(INSTALLATION_RATE)).thenReturn(installRate);
        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v1/proposal/fees/get-fees").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                {
                   "opportunityId": "4e6b22ea-ec9a-40fc-a247-b90b2db7fea",
                   "customerAddress": {
                     "address1": "Appartment 3",
                     "address2": "That building, This Block",
                     "city": "Brooklyn",
                     "state": "NY",
                     "zipCode": "NY 10328"
                   },
                   "storeAddress": {
                     "address1": "Staten Island",
                     "address2": "283 Platinum Ave",
                     "city": "Staten Island",
                     "state": "NY",
                     "zipCode": "NY 10314"
                   },
                   "spaces": [
                     {
                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                       "retailPrice": 5000
                     },
                     {
                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed366",
                       "retailPrice": 3
                     },
                     {
                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed367",
                       "retailPrice": 2
                     }
                   ]
                 }""")).andExpect(status().is(200)).andReturn();
        ProposalFeeResponse response = new Gson().fromJson(result.getResponse().getContentAsString(), ProposalFeeResponse.class);
        assertNotNull(response);
    }

    @Test
    public void test_calculate_proposal_fee_success_NJ_ThreeSpaces() throws Exception {
        when(stateRepository.findStateByStateAbbreviation("NJ")).thenReturn(getState("NJ", BigDecimal.valueOf(1000)));
        when(salesforceMileageService.getWebhookPersistedDataByKeyAndApp(any(), any()))
                .thenReturn(List.of(salesforceMileageEntity));
        ProposalConstants maxRetailValue = new ProposalConstants();
        maxRetailValue.setKey(MAX_RETAIL_VALUE_PER_CRATE.name());
        maxRetailValue.setValue(BigDecimal.valueOf(9999.99));

        ProposalConstants minimumInstallFee = new ProposalConstants();
        minimumInstallFee.setKey(MINIMUM_INSTALLATION_FEE.name());
        minimumInstallFee.setValue(BigDecimal.valueOf(1000));

        ProposalConstants installRate = new ProposalConstants();
        installRate.setKey(INSTALLATION_RATE.name());
        installRate.setValue(BigDecimal.valueOf(0.415));

        when(proposalConstantsService.getProposalConstants(MAX_RETAIL_VALUE_PER_CRATE)).thenReturn(maxRetailValue);
        when(proposalConstantsRepository.findById(MAX_RETAIL_VALUE_PER_CRATE.name())).thenReturn(Optional.of(maxRetailValue));
        when(proposalConstantsService.getProposalConstants(MINIMUM_INSTALLATION_FEE)).thenReturn(minimumInstallFee);
        when(proposalConstantsService.getProposalConstants(INSTALLATION_RATE)).thenReturn(installRate);

        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v1/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                     "proposalId": "4e6b22ea-ec9a-40fc-a247-b90b2db7fea0",
                                     "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                     "customerAddress": {
                                       "address1": "Bridgewater, 391",
                                       "address2": "Station Road",
                                       "city": "Bridgewater",
                                       "state": "NJ",
                                       "zipCode": "NJ 10328"
                                     },
                                     "storeAddress": {
                                       "address1": "Staten Island",
                                       "address2": "283 Platinum Ave",
                                       "city": "Staten Island",
                                       "state": "NJ",
                                       "zipCode": "NJ 10314"
                                     },
                                     "spaces": [
                                       {
                                         "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                         "retailPrice": 6569.67
                                       },
                                       {
                                         "spaceId": "9c96c0e9-d7aa-4541-A785-8acd201ed365",
                                         "retailPrice": 4104.09
                                       },
                                       {
                                         "spaceId": "9c96c0e9-d7aa-4541-A785-8scd201ed365",
                                         "retailPrice": 3468.83
                                       }
                                     ]
                                   }"""))
                .andExpect(status().is(200))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String expectedResponse = """
                {
                      "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                      "taxableFees": {
                          "HeaderFees": [
                              {
                                  "name": "FREIGHT_FEE",
                                  "description": "Calculated Freight fees for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                  "fee": 2000.00,
                                  "percentage": null
                              },
                              {
                                  "name": "INSTALLATION_FEE",
                                  "description": "Installation calculated for proposal with opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                  "fee": 5869.17,
                                  "percentage": null
                              }
                          ],
                          "LineFees": [
                              {
                                  "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                  "name": "INSTALLATION_FEE",
                                  "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed365 ",
                                  "fee": 2726.41,
                                  "percentage": null
                              },
                              {
                                  "spaceId": "9c96c0e9-d7aa-4541-A785-8acd201ed365",
                                  "name": "INSTALLATION_FEE",
                                  "description": "Installation fee for space 9c96c0e9-d7aa-4541-A785-8acd201ed365 ",
                                  "fee": 1703.20,
                                  "percentage": null
                              },
                              {
                                  "spaceId": "9c96c0e9-d7aa-4541-A785-8scd201ed365",
                                  "name": "INSTALLATION_FEE",
                                  "description": "Installation fee for space 9c96c0e9-d7aa-4541-A785-8scd201ed365 ",
                                  "fee": 1439.56,
                                  "percentage": null
                              }
                          ]
                      },
                      "nonTaxableFee": {
                          "headerFees": null,
                          "lineFees": null
                      },
                      "tax": null
                  }""";

        JsonElement actualJson = JsonParser.parseString(actualResponse);
        JsonElement expectedJson = JsonParser.parseString(expectedResponse);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void test_calculate_proposal_fee_success_CA_TwoSpaces() throws Exception {
        Optional<State> state = getState("CA", BigDecimal.valueOf(1200));
        when(stateRepository.findStateByStateAbbreviation("CA")).thenReturn(state);
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(state.get()));
        when(salesforceMileageService.getWebhookPersistedDataByKeyAndApp(any(), any()))
                .thenReturn(List.of(salesforceMileageEntity));
        ProposalConstants maxRetailValue = new ProposalConstants();
        maxRetailValue.setKey(MAX_RETAIL_VALUE_PER_CRATE.name());
        maxRetailValue.setValue(BigDecimal.valueOf(9999.99));

        ProposalConstants minimumInstallFee = new ProposalConstants();
        minimumInstallFee.setKey(MINIMUM_INSTALLATION_FEE.name());
        minimumInstallFee.setValue(BigDecimal.valueOf(1000));

        ProposalConstants installRate = new ProposalConstants();
        installRate.setKey(INSTALLATION_RATE.name());
        installRate.setValue(BigDecimal.valueOf(0.415));

        when(proposalConstantsService.getProposalConstants(MAX_RETAIL_VALUE_PER_CRATE)).thenReturn(maxRetailValue);
        when(proposalConstantsRepository.findById(MAX_RETAIL_VALUE_PER_CRATE.name())).thenReturn(Optional.of(maxRetailValue));
        when(proposalConstantsService.getProposalConstants(MINIMUM_INSTALLATION_FEE)).thenReturn(minimumInstallFee);
        when(proposalConstantsService.getProposalConstants(INSTALLATION_RATE)).thenReturn(installRate);

        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v1/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                      "proposalId": "4e6b22ea-ec9a-40fc-a247-b90b2db7fea0",
                                      "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                      "customerAddress": {
                                        "address1": "Willows Apartment",
                                        "address2": "Floor No 2, 41, Station Road",
                                        "city": "Brooklyn",
                                        "state": "CA",
                                        "zipCode": "CA 10328"
                                      },
                                      "storeAddress": {
                                        "address1": "Staten Island",
                                        "address2": "283 Platinum Ave",
                                        "city": "Staten Island",
                                        "state": "CA",
                                        "zipCode": "CA 10314"
                                      },
                                      "spaces": [
                                        {
                                          "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                          "retailPrice": 8545.69
                                        },
                                        {
                                          "spaceId": "9c96c0e9-d7aa-4541-A785-8acd201ed365",
                                          "retailPrice": 26000.64
                                        }
                                      ]
                                    }"""))
                .andExpect(status().is(200))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String expectedResponse = """
                {
                        "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                        "taxableFees": {
                            "HeaderFees": [
                                {
                                    "name": "FREIGHT_FEE",
                                    "description": "Calculated Freight fees for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                    "fee": 4800.00,
                                    "percentage": null
                                },
                                {
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation calculated for proposal with opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                    "fee": 14336.73,
                                    "percentage": null
                                }
                            ],
                            "LineFees": [
                                {
                                    "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed365 ",
                                    "fee": 3546.46,
                                    "percentage": null
                                },
                                {
                                    "spaceId": "9c96c0e9-d7aa-4541-A785-8acd201ed365",
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation fee for space 9c96c0e9-d7aa-4541-A785-8acd201ed365 ",
                                    "fee": 10790.27,
                                    "percentage": null
                                }
                            ]
                        },
                        "nonTaxableFee": {
                            "headerFees": null,
                            "lineFees": null
                        },
                        "tax": null
                    }""";

        JsonElement actualJson = JsonParser.parseString(actualResponse);
        JsonElement expectedJson = JsonParser.parseString(expectedResponse);
        assertEquals(expectedJson, actualJson);

    }

    @Test
    public void test_calculate_proposal_fee_success_CA_ThreeSpaces() throws Exception {
        Optional<State> state = getState("CA", BigDecimal.valueOf(1200));
        when(stateRepository.findStateByStateAbbreviation("CA")).thenReturn(state);
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(state.get()));
        when(salesforceMileageService.getWebhookPersistedDataByKeyAndApp(any(), any()))
                .thenReturn(List.of(salesforceMileageEntity));
        ProposalConstants maxRetailValue = new ProposalConstants();
        maxRetailValue.setKey(MAX_RETAIL_VALUE_PER_CRATE.name());
        maxRetailValue.setValue(BigDecimal.valueOf(9999.99));

        ProposalConstants minimumInstallFee = new ProposalConstants();
        minimumInstallFee.setKey(MINIMUM_INSTALLATION_FEE.name());
        minimumInstallFee.setValue(BigDecimal.valueOf(1000));

        ProposalConstants installRate = new ProposalConstants();
        installRate.setKey(INSTALLATION_RATE.name());
        installRate.setValue(BigDecimal.valueOf(0.415));

        when(proposalConstantsService.getProposalConstants(MAX_RETAIL_VALUE_PER_CRATE)).thenReturn(maxRetailValue);
        when(proposalConstantsRepository.findById(MAX_RETAIL_VALUE_PER_CRATE.name())).thenReturn(Optional.of(maxRetailValue));
        when(proposalConstantsService.getProposalConstants(MINIMUM_INSTALLATION_FEE)).thenReturn(minimumInstallFee);
        when(proposalConstantsService.getProposalConstants(INSTALLATION_RATE)).thenReturn(installRate);

        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v1/proposal/fees/get-fees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                      "proposalId": "4e6b22ea-ec9a-40fc-a247-b90b2db7fea0",
                                      "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                      "customerAddress": {
                                        "address1": "Willows Apartment",
                                        "address2": "Floor No 2, 41, Station Road",
                                        "city": "Brooklyn",
                                        "state": "CA",
                                        "zipCode": "CA 10328"
                                      },
                                      "storeAddress": {
                                        "address1": "Staten Island",
                                        "address2": "283 Platinum Ave",
                                        "city": "Staten Island",
                                        "state": "CA",
                                        "zipCode": "CA 10314"
                                      },
                                      "spaces": [
                                        {
                                          "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                          "retailPrice": 200
                                        },
                                        {
                                          "spaceId": "9c96c0e9-d7aa-4541-A785-8acd201ed365",
                                          "retailPrice": 452
                                        },
                                        {
                                          "spaceId": "9c96c0e9-d7aa-4542-A785-8acd201ed365",
                                          "retailPrice": 580
                                        }
                                      ]
                                    }"""))
                .andExpect(status().is(200))
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();

        String expectedResponse = """
                {
                        "opportunityId": "94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                        "taxableFees": {
                            "HeaderFees": [
                                {
                                    "name": "FREIGHT_FEE",
                                    "description": "Calculated Freight fees for opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                    "fee": 1200.00,
                                    "percentage": null
                                },
                                {
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation calculated for proposal with opportunity id: 94e6b22ea-ec9a-40fc-a247-b90b2db7fea4",
                                    "fee": 1000.00,
                                    "percentage": null
                                }
                            ],
                            "LineFees": [
                                {
                                    "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation fee for space 9c96c0e9-d7aa-4541-a473-8acd201ed365 ",
                                    "fee": 162.34,
                                    "percentage": null
                                },
                                {
                                    "spaceId": "9c96c0e9-d7aa-4541-A785-8acd201ed365",
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation fee for space 9c96c0e9-d7aa-4541-A785-8acd201ed365 ",
                                    "fee": 366.88,
                                    "percentage": null
                                },
                                {
                                    "spaceId": "9c96c0e9-d7aa-4542-A785-8acd201ed365",
                                    "name": "INSTALLATION_FEE",
                                    "description": "Installation fee for space 9c96c0e9-d7aa-4542-A785-8acd201ed365 ",
                                    "fee": 470.78,
                                    "percentage": null
                                }
                            ]
                        },
                        "nonTaxableFee": {
                            "headerFees": null,
                            "lineFees": null
                        },
                        "tax": null
                    }""";

        JsonElement actualJson = JsonParser.parseString(actualResponse);
        JsonElement expectedJson = JsonParser.parseString(expectedResponse);
        assertEquals(expectedJson, actualJson);

    }

    @Test
    public void testInvalidRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/apps/closetpro/api/v1/proposal/fees/get-fees").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                {
                   "opportunityId": "4e6b22ea-ec9a-40fc-a247-b90b2db7fea",
                   "customerAddress": {
                     "address1": "Appartment 3",
                     "address2": "That building, This Block",
                     "city": "Brooklyn",
                     "state": "",
                     "zipCode": "NY 10328"
                   },
                   "storeAddress": {
                     "address1": "Staten Island",
                     "address2": "283 Platinum Ave",
                     "city": "Staten Island",
                     "state": "NY",
                     "zipCode": "NY 10314"
                   },
                   "spaces": [
                     {
                       "spaceId": "9c96c0e9-d7aa-4541-a473-8acd201ed365",
                       "retailPrice": 5000
                     }
                   ]
                }""")).andExpect(status().is(400)).andReturn();
        RestError response = new Gson().fromJson(result.getResponse().getContentAsString(), RestError.class);
        assertNotNull(response);
        assertNotEquals("", response.getMessage());
    }

    @Test
    public void test_sendProposalToSalesforce() throws Exception {
        var validateSpaceResponse1 = new ValidateSpaceResponse("id", "open", "7724244");
        var validateSpaceResponse2 = new ValidateSpaceResponse("id", "open", "7724245");
        when(salesforceServiceClient.validateSpaces(anyString())).thenReturn(List.of(validateSpaceResponse1, validateSpaceResponse2));
        when(offerService.getPresentedOffer(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                     {
                          "opportunityId": "0064w00001991VfAAI",
                          "proposalId": "5647372",
                          "spaces": [
                            {
                              "spaceId": "7724244",
                              "retailPrice": 8000.12,
                              "finish": "Oak",
                              "color": "RED",
                              "spaceFees": [   \s
                                {
                                  "name": "INSTALLATION_FEE",
                                  "fee": 3320.15
                                },
                                {
                                  "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                  "fee": 200.14
                                },
                                {
                                  "name": "FREIGHT_FEE",
                                  "fee": 415.02
                                }
                              ]
                            },
                            {
                              "spaceId": "7724245",
                              "retailPrice": 3000,
                              "finish": "Oak",
                              "color": "RED",
                              "spaceFees": [   \s
                                {
                                  "name": "INSTALLATION_FEE",
                                  "fee": 1320.15
                                },
                                {
                                  "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                  "fee": 100.14
                                },
                                {
                                  "name": "FREIGHT_FEE",
                                  "fee": 215.02
                                }
                              ]
                            }
                          ],
                          "fees": {
                            "headerFees": [
                              {
                                "name": "ADDITIONAL_SERVICES_FEE",
                                "fee": 700
                              },
                              {
                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                "fee": 500
                              }
                            ]
                          },
                          "tax": [
                            {
                              "taxName": "SALES_TAX",
                              "amount": 1500
                            }
                          ],
                          "customFees": [
                            {
                              "name": "Discount1",
                              "fee": -200,
                              "percentage": null
                            }
                          ],
                          "offers": [
                            {
                              "offerCode": "PRESTBP",
                              "amountOff": 500
                            },
                            {
                              "offerCode": "PRESTVIP",
                              "amountOff": 1500
                            }
                          ]
                        }
                """))
                .andExpect(status().is(200)).andReturn();
    }

    @Test
    public void test_sendProposal_without_fees() throws Exception {
        var validateSpaceResponse1 = new ValidateSpaceResponse("id", "open", "7724244");
        var validateSpaceResponse2 = new ValidateSpaceResponse("id", "open", "7724245");
        when(salesforceServiceClient.validateSpaces(anyString())).thenReturn(List.of(validateSpaceResponse1, validateSpaceResponse2));
        when(offerService.getPresentedOffer(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                     {
                          "opportunityId": "0064w00001991VfAAI",
                          "proposalId": "5647372",
                          "spaces": [
                            {
                              "spaceId": "7724244",
                              "retailPrice": 8000.12,
                              "finish": "Oak",
                              "color": "RED",
                              "spaceFees": [   \s
                                {
                                  "name": "INSTALLATION_FEE",
                                  "fee": 3320.15
                                },
                                {
                                  "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                  "fee": 200.14
                                },
                                {
                                  "name": "FREIGHT_FEE",
                                  "fee": 415.02
                                }
                              ]
                            },
                            {
                              "spaceId": "7724245",
                              "retailPrice": 3000,
                              "finish": "Oak",
                              "color": "RED",
                              "spaceFees": [   \s
                                {
                                  "name": "INSTALLATION_FEE",
                                  "fee": 1320.15
                                },
                                {
                                  "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                  "fee": 100.14
                                },
                                {
                                  "name": "FREIGHT_FEE",
                                  "fee": 215.02
                                }
                              ]
                            }
                          ],
                          "fees": null,
                          "tax": [
                            {
                              "taxName": "SALES_TAX",
                              "amount": 1500
                            }
                          ],
                          "customFees": [
                            {
                              "name": "Discount1",
                              "fee": -200,
                              "percentage": null
                            }
                          ],
                          "offers": [
                            {
                              "offerCode": "PRESTBP",
                              "amountOff": 500
                            },
                            {
                              "offerCode": "PRESTVIP",
                              "amountOff": 1500
                            }
                          ]
                        }
                """))
                .andExpect(status().is(200)).andReturn();
    }

    @Test
    public void test_sendProposal_without_headerFees() throws Exception {
        var validateSpaceResponse1 = new ValidateSpaceResponse("id", "open", "7724244");
        var validateSpaceResponse2 = new ValidateSpaceResponse("id", "open", "7724245");
        when(salesforceServiceClient.validateSpaces(anyString())).thenReturn(List.of(validateSpaceResponse1, validateSpaceResponse2));
        when(offerService.getPresentedOffer(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                     {
                          "opportunityId": "0064w00001991VfAAI",
                          "proposalId": "5647372",
                          "spaces": [
                            {
                              "spaceId": "7724244",
                              "retailPrice": 8000.12,
                              "finish": "Oak",
                              "color": "RED",
                              "spaceFees": [   \s
                                {
                                  "name": "INSTALLATION_FEE",
                                  "fee": 3320.15
                                },
                                {
                                  "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                  "fee": 200.14
                                },
                                {
                                  "name": "FREIGHT_FEE",
                                  "fee": 415.02
                                }
                              ]
                            },
                            {
                              "spaceId": "7724245",
                              "retailPrice": 3000,
                              "finish": "Oak",
                              "color": "RED",
                              "spaceFees": [   \s
                                {
                                  "name": "INSTALLATION_FEE",
                                  "fee": 1320.15
                                },
                                {
                                  "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                  "fee": 100.14
                                },
                                {
                                  "name": "FREIGHT_FEE",
                                  "fee": 215.02
                                }
                              ]
                            }
                          ],
                          "fees": {
                            "headerFees": null
                          },
                          "tax": [
                            {
                              "taxName": "SALES_TAX",
                              "amount": 1500
                            }
                          ],
                          "customFees": [
                            {
                              "name": "Discount1",
                              "fee": -200,
                              "percentage": null
                            }
                          ],
                          "offers": [
                            {
                              "offerCode": "PRESTBP",
                              "amountOff": 500
                            },
                            {
                              "offerCode": "PRESTVIP",
                              "amountOff": 1500
                            }
                          ]
                        }
                """))
                .andExpect(status().is(200)).andReturn();
    }


    @Test
    public void test_sendProposalToSalesforce_ShouldThrowArgumentValidationException() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal")
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "opportunityId": "0067e00000QDIyXAAX",
                                    "proposalId": "0067e00000QDIyXAAXoo",
                                    "spaces": [
                                        {
                                            "spaceId": "564283",
                                            "retailPrice": 8000,
                                            "finish": "Oak",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 3320.15
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 200.14
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 415.02
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerFees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 500
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 300
                                            }
                                        ]
                                    },
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 1500.80
                                        }
                                    ],
                                    "customFees": [
                                        {
                                            "name": "Discount1",
                                            "fee": -200,
                                            "percentage": null
                                        }
                                    ],
                                    "offers": [
                                        {
                                            "offerCode": "STDDSGNSPC",
                                            "amountOff": 500
                                        }
                                    ]
                                }"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Validation error in fields [spaces[0].color]"))
                .andDo(print());
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
    public void test_buildProposalFeeResponse(){
        Map<String, BigDecimal> spaces = new HashMap<>();
        spaces.put("1", BigDecimal.valueOf(4000));
        ProposalFeeRequest request = ProposalTestHelpers.getProposalRequest("NY", spaces);
        assertNotNull(closetProProposalResource.buildProposalFeeResponse(Mockito.mock(ProposalFeeRequest.class)));
        assertNotNull(closetProProposalResource.buildProposalFeeResponse(Mockito.mock(ProposalFeeRequest.class)).getTaxableFees());
        assertNotNull(closetProProposalResource.buildProposalFeeResponse(Mockito.mock(ProposalFeeRequest.class)).getNonTaxableFee());
        assertNotNull(closetProProposalResource.buildProposalFeeResponse(request).getOpportunityId());

    }
}
