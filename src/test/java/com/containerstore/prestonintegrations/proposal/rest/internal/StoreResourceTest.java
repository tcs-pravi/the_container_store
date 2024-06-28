package com.containerstore.prestonintegrations.proposal.rest.internal;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import com.containerstore.prestonintegrations.proposal.store.AdjustmentType;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.dto.PaginatedAPIResponse;
import com.containerstore.prestonintegrations.proposal.store.dto.StoreDTO;
import com.containerstore.prestonintegrations.proposal.store.service.StoreService;
import com.containerstore.prestonintegrations.proposal.store.utils.StoreConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreResource.class)
@AutoConfigureMockMvc
public class StoreResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    private static List<StoreDTO> mockedStoreDtoList;

    @BeforeAll
    static void setUp(){
        State state = new State();
        state.setStateAbbreviation("NY");

        StoreEntity store = new StoreEntity();
        store.setId(UUID.fromString("e56d3214-bd10-4683-996e-ca58896b666d"));
        store.setStoreId("storeId");
        store.setStoreCode("code");
        store.setSalesforceStoreId("id-code");
        store.setCity("New York");
        store.setState(state);
        store.setZipCode("10011");
        store.setAdjustmentType(AdjustmentType.AMOUNT);
        store.setAdjustmentValue(BigDecimal.valueOf(100));
        store.setFreightFeeEnabled(true);
        store.setInstallationRate(BigDecimal.valueOf(200.00));
        store.setInstallationFeeEnabled(true);
        store.setActive(true);

        mockedStoreDtoList = new ArrayList<>();
        mockedStoreDtoList.add(StoreConverter.convertStoreEntityToStoreDTO(store));
    }

    @Test
    @DisplayName("Test for get store details")
    void shouldGetStoreDetails() throws Exception {
        PaginatedAPIResponse<StoreDTO> apiResponse = new PaginatedAPIResponse<>(0, 10, 1, 1L, mockedStoreDtoList);

        when(storeService.getPaginatedStoresList(PageRequest.of(0, 10), "NY"))
                .thenReturn(apiResponse);

        mockMvc.perform(
                        get("/internal/api/v1/stores/list")
                                .param("page", "0")
                                .param("size", "10")
                                .param("state", "NY")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].storeId").value("storeId"))
                .andExpect(jsonPath("$.content[0].storeCode").value("code"))
                .andExpect(jsonPath("$.content[0].salesforceStoreId").value("id-code"))
                .andExpect(jsonPath("$.content[0].city").value("New York"))
                .andExpect(jsonPath("$.content[0].stateAbbreviation").value("NY"))
                .andExpect(jsonPath("$.content[0].zipCode").value("10011"))
                .andExpect(jsonPath("$.content[0].adjustmentValue").value(100))
                .andExpect(jsonPath("$.content[0].adjustmentType").value("AMOUNT"))
                .andExpect(jsonPath("$.content[0].installationRate").value(200))
                .andExpect(jsonPath("$.content[0].isInstallationFeeEnabled").value(true))
                .andExpect(jsonPath("$.content[0].isActive").value(true))
                .andExpect(jsonPath("$.content[0].isFreightFeeEnabled").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("Test for get store details without request params should return all records")
    void shouldGetStoreDetails_WithoutParams() throws Exception {
        PaginatedAPIResponse<StoreDTO> apiResponse = new PaginatedAPIResponse<>(0, 20, 1, 1L, mockedStoreDtoList);

        when(storeService.getPaginatedStoresList(PageRequest.of(0,20), null)).thenReturn(apiResponse);

        mockMvc.perform(
                        get("/internal/api/v1/stores/list")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].storeId").value("storeId"))
                .andExpect(jsonPath("$.content[0].storeCode").value("code"))
                .andExpect(jsonPath("$.content[0].salesforceStoreId").value("id-code"))
                .andExpect(jsonPath("$.content[0].city").value("New York"))
                .andExpect(jsonPath("$.content[0].stateAbbreviation").value("NY"))
                .andExpect(jsonPath("$.content[0].zipCode").value("10011"))
                .andExpect(jsonPath("$.content[0].adjustmentValue").value(100))
                .andExpect(jsonPath("$.content[0].adjustmentType").value("AMOUNT"))
                .andExpect(jsonPath("$.content[0].installationRate").value(200))
                .andExpect(jsonPath("$.content[0].isInstallationFeeEnabled").value(true))
                .andExpect(jsonPath("$.content[0].isActive").value(true))
                .andExpect(jsonPath("$.content[0].isFreightFeeEnabled").value(true))
                .andDo(print());
    }

    @Test
    public void testUpdateStore() throws Exception {
        String successMessage = "Updated Successfully";
        when(storeService.updateStore(any(StoreDTO.class))).thenReturn(successMessage);

        String requestJson = "{\"storeId\":\"15\",\"storeCode\":\"ST009\",\"salesforceStoreId\":\"SF300\",\"city\":\"LosAngles\",\"stateAbbreviation\":\"CA\",\"zipCode\":\"20078\",\"adjustmentValue\":500.00,\"adjustmentType\":\"AMOUNT\",\"isFreightFeeEnabled\":true,\"installationRate\":200.00,\"isInstallationFeeEnabled\":true,\"isActive\":true}";

        mockMvc.perform(post("/internal/api/v1/stores/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage))
                .andDo(print());
    }
}
