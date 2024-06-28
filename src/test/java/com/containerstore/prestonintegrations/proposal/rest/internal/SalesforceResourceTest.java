package com.containerstore.prestonintegrations.proposal.rest.internal;

import com.containerstore.prestonintegrations.proposal.rest.internal.dto.ResponseDTO;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.SalesforceServiceClient;
import com.containerstore.prestonintegrations.proposal.shared.exception.ProposalGlobalExceptionHandler;
import com.containerstore.prestonintegrations.proposal.store.dto.PaginatedAPIResponse;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@ActiveProfiles("local-test")
@SpringBootTest(classes = { SalesforceMileageService.class, SalesforceTearOutService.class, SalesforceResource.class, SalesforceServiceClient.class})
public class SalesforceResourceTest {
    private MockMvc mockMvc;

    @MockBean
    SalesforceMileageService salesforceMileageService;

    @Autowired
    SalesforceTearOutService salesforceTearOutService;

    @MockBean
    SalesforceMileageRepository salesforceMileageRepository;

    @MockBean
    SalesforceTearOutRepository salesforceTearOutRepository;

    @Autowired
    SalesforceResource salesforceResource;

    @MockBean
    SalesforceServiceClient salesforceServiceClient;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(salesforceResource)
                .setControllerAdvice(new ProposalGlobalExceptionHandler())
                .build();
    }

    @Test
    public void testDeleteMileageFee() throws Exception {
        String opportunityId = "opportunityId123";
        int numberOfRowsDeleted = 1;
        when(salesforceMileageRepository.deleteByOpportunityIdAndApp(opportunityId, WebHookConsumers.SALESFORCE)).thenReturn(numberOfRowsDeleted);
        var result = mockMvc.perform(delete("/api/v1/salesforce/mileage/delete/opportunityId123").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        ResponseDTO response = new Gson().fromJson(result.getResponse().getContentAsString(), ResponseDTO.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(0.0,response.data());
    }

    @Test
    public void testDeleteTearOutFee() throws Exception {
        String opportunityId = "opportunityId123";
        int numberOfRowsDeleted = 1;
        when(salesforceTearOutRepository.deleteByOpportunityIdAndApp(opportunityId, WebHookConsumers.SALESFORCE)).thenReturn(numberOfRowsDeleted);
        var result = mockMvc.perform(delete("/api/v1/salesforce/tear-out/delete/opportunityId123").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        ResponseDTO response = new Gson().fromJson(result.getResponse().getContentAsString(), ResponseDTO.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(1.0,response.data());
    }

    @Test
    public void testGetAllMileageRecord() throws Exception {

        Integer pageNumber = 0;
        Integer pageSize = 20;
        String opportunityId = "94e6b22ea-ec9a-40fc-a247";

        SalesforceMileageEntity entity1 = new SalesforceMileageEntity();
        entity1.setOpportunityId("94e6b22ea-ec9a-40fc-a247");
        entity1.setMiles(BigDecimal.valueOf(10));
        entity1.setInstallationStoreId("StoreId1");
        entity1.setDurationValue(BigDecimal.valueOf(100));
        entity1.setChronoUnit(ChronoUnit.SECONDS);

        SalesforceMileageEntity entity2 = new SalesforceMileageEntity();
        entity2.setOpportunityId("94e6b22ea-ec9a-40fc-a247");
        entity2.setMiles(BigDecimal.valueOf(20));
        entity2.setInstallationStoreId("StoreId2");
        entity2.setDurationValue(BigDecimal.valueOf(200));
        entity2.setChronoUnit(ChronoUnit.SECONDS);

        List<SalesforceMileageEntity> list = List.of(entity1, entity2);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<SalesforceMileageEntity> paginatedMileageResponse = new PageImpl<>(list, pageable,2);
        when(salesforceMileageService.getAllMileageFee(pageNumber,pageSize,opportunityId))
                .thenReturn(paginatedMileageResponse);

        var result = mockMvc.perform(
                        get("/api/v1/salesforce/list-mileage-fee").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "20")
                                .param("opportunity-id", "94e6b22ea-ec9a-40fc-a247")
                )
                .andExpect(status().isOk()).andReturn();

        PaginatedAPIResponse response = new Gson().fromJson(result.getResponse().getContentAsString(), PaginatedAPIResponse.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(0,response.page());
        Assert.assertEquals(20,response.size());
        Assert.assertEquals(1,response.totalPages());
        Assert.assertEquals(2,response.totalElements());
        Assert.assertEquals(2, response.content().size());


    }

    @Test
    public void testGetAllTearOutRecord() throws Exception {

        Integer pageNumber = 0;
        Integer pageSize = 20;
        String opportunityId = "94e6b22ea-ec9a-40fc-a247";

        Page<SalesforceTearOutEntity> paginatedTearOutResponse = getSalesforceTearOutEntities(pageNumber, pageSize);
        when(salesforceTearOutService.getAllTearOutFee(pageNumber,pageSize,opportunityId))
                .thenReturn(paginatedTearOutResponse);

        var result = mockMvc.perform(
                        get("/api/v1/salesforce/list-tearOut-fee").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "20")
                                .param("opportunity-id", "94e6b22ea-ec9a-40fc-a247")
                )
                .andExpect(status().isOk()).andReturn();

        PaginatedAPIResponse response = new Gson().fromJson(result.getResponse().getContentAsString(), PaginatedAPIResponse.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(0,response.page());
        Assert.assertEquals(20,response.size());
        Assert.assertEquals(1,response.totalPages());
        Assert.assertEquals(2,response.totalElements());
        Assert.assertEquals(2, response.content().size());


    }

    @NotNull
    private static Page<SalesforceTearOutEntity> getSalesforceTearOutEntities(Integer pageNumber, Integer pageSize) {
        SalesforceTearOutEntity entity1 = new SalesforceTearOutEntity();
        entity1.setOpportunityId("94e6b22ea-ec9a-40fc-a247");
        entity1.setSpaceId("SpaceId1");
        entity1.setTearOutFee(BigDecimal.valueOf(100));

        SalesforceTearOutEntity entity2 = new SalesforceTearOutEntity();
        entity2.setOpportunityId("94e6b22ea-ec9a-40fc-a247");
        entity2.setSpaceId("SpaceId2");
        entity2.setTearOutFee(BigDecimal.valueOf(200));

        List<SalesforceTearOutEntity> list = List.of(entity1, entity2);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<SalesforceTearOutEntity> paginatedTearOutResponse = new PageImpl<>(list, pageable,2);
        return paginatedTearOutResponse;
    }
}
