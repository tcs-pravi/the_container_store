package com.containerstore.prestonintegrations.proposal.rest.apps;

import com.containerstore.prestonintegrations.proposal.shared.exception.ProposalGlobalExceptionHandler;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local-test")
@SpringBootTest(classes = { SalesForceHooksResource.class, SalesforceMileageService.class, SalesforceTearOutService.class})
class SalesForceHooksResourceTest {

    private MockMvc mockMvc;

    @MockBean
    private SalesforceMileageRepository salesforceMileageRepository;

    @MockBean
    private SalesforceTearOutRepository salesforceTearOutRepository;

    @Autowired
    private SalesForceHooksResource salesForceHooksResource;

    @MockBean
    private SalesforceMileageService salesforceMileageService;

    @MockBean
    private SalesforceTearOutService salesforceTearOutService;

    @BeforeEach
    public void setMockMvc(){
        mockMvc = MockMvcBuilders.standaloneSetup(salesForceHooksResource).setControllerAdvice(new ProposalGlobalExceptionHandler()).build();
    }

    @Test
    public void test_salesforce_mileage_webhook_success() throws Exception {
        when(salesforceMileageRepository.save(any())).thenReturn(new SalesforceMileageEntity());
        mockMvc.perform(post("/apps/salesforce/api/v1/webhooks/mileage").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "eventId": "12131",
                  "opportunityId": "123123123",
                  "miles": 10,
                  "installationStoreId": "899",
                  "driveTime": {
                    "duration": 10,
                    "chronoUnit": "HOURS"
                  }
                }""")).andExpect(status().is(201)).andReturn();
        verify(salesforceMileageService).handleRequest(any());
    }

    @Test
    public void test_salesforce_mileage_tear_out_success() throws Exception {
        when(salesforceTearOutRepository.save(any())).thenReturn(new SalesforceTearOutEntity());
        mockMvc.perform(post("/apps/salesforce/api/v1/webhooks/tear-out").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "eventId": "123141",
                  "opportunityId": "14124124",
                  "spaces": [
                    {
                      "spaceId": "1244141",
                      "tearOutFee": 200
                    },
                    {
                      "spaceId": "1244142",
                      "tearOutFee": 200
                    }
                  ]
                }""")).andExpect(status().is(201)).andReturn();
        verify(salesforceTearOutService).handleRequest(any());
    }

    @Test
    public void test_salesforce_mileage_tear_out_bad_request() throws Exception {
        when(salesforceTearOutRepository.save(any())).thenReturn(new SalesforceTearOutEntity());
        mockMvc.perform(post("/apps/salesforce/api/v1/webhooks/tear-out").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "eventId": "123141",
                  "opportunityId": "14124124",
                  "spaces": [
                    {
                      "tearOutFee": 200
                    },
                    {
                      "spaceId": "1244141",
                      "tearOutFee": 200
                    }
                  ]
                }""")).andExpect(status().is(400)).andReturn();
    }

    @Test
    public void test_salesforce_mileage_webhook_bad_requests() throws Exception {
        when(salesforceMileageRepository.save(any())).thenReturn(new SalesforceMileageEntity());
        mockMvc.perform(post("/apps/salesforce/api/v1/webhooks/mileage").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "eventId": "12131",
                  "opportunityId": "123123123",
                  "miles": 10,
                  "installationStoreId": "899",
                  "driveTime": {
                    "duration": 10,
                    "chronoUnit": "INVALID"
                  }
                }""")).andExpect(status().is(400)).andReturn();
    }

}
