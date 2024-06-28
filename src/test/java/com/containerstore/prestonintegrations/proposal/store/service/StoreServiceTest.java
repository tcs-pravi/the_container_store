package com.containerstore.prestonintegrations.proposal.store.service;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import com.containerstore.prestonintegrations.proposal.freightfee.exception.StateNotFoundException;
import com.containerstore.prestonintegrations.proposal.freightfee.repository.StateRepository;
import com.containerstore.prestonintegrations.proposal.store.AdjustmentType;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.store.dto.PaginatedAPIResponse;
import com.containerstore.prestonintegrations.proposal.store.dto.StoreDTO;
import com.containerstore.prestonintegrations.proposal.store.utils.StoreConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {StoreServiceTest.class, StoreRepository.class })
public class StoreServiceTest {

    @Spy
    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StateRepository stateRepository;

    private PageRequest pageable;
    private Page<StoreEntity> page;
    private List<StoreDTO> storeDTOList;

    @BeforeEach
    void setUp(){
        pageable = PageRequest.of(0, 10);

        State state = new State();
        state.setStateName("New York");
        state.setStateAbbreviation("NY");

        StoreEntity store1 = new StoreEntity();
        store1.setId(UUID.fromString("e56d3214-bd10-4683-996e-ca58896b666d"));
        store1.setStoreId("storeId");
        store1.setStoreCode("code");
        store1.setSalesforceStoreId("id-code");
        store1.setCity("New York");
        store1.setState(state);
        store1.setZipCode("10011");
        store1.setAdjustmentType(AdjustmentType.AMOUNT);
        store1.setAdjustmentValue(BigDecimal.valueOf(100));
        store1.setFreightFeeEnabled(true);
        store1.setActive(true);

        StoreEntity store2 = new StoreEntity();
        store2.setId(UUID.fromString("e56d3214-bd10-4683-996e-ca58896b666d"));
        store2.setStoreId("storeId");
        store2.setStoreCode("code");
        store2.setSalesforceStoreId("id-code");
        store2.setCity("New York");
        store2.setState(state);
        store2.setZipCode("10011");
        store2.setAdjustmentType(AdjustmentType.AMOUNT);
        store2.setAdjustmentValue(BigDecimal.valueOf(100));
        store2.setFreightFeeEnabled(true);
        store2.setActive(true);

        page = new PageImpl<>(List.of(store1, store2), pageable, 2);

        storeDTOList = List.of(
                StoreConverter.convertStoreEntityToStoreDTO(store1),
                StoreConverter.convertStoreEntityToStoreDTO(store2)
        );
    }

    @Test
    @DisplayName("Test for getPaginatedStoresList() method")
    void testGetPaginatedStoresList(){
        when(storeRepository.findByStateStateAbbreviation("NY", pageable)).thenReturn(page);

        PaginatedAPIResponse<StoreDTO> result = storeService.getPaginatedStoresList(pageable, "NY");

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(2, result.totalElements());
        assertEquals(storeDTOList, result.content());

        verify(storeRepository, times(1)).findByStateStateAbbreviation(anyString(), any(PageRequest.class));
    }
    @Test
    @DisplayName("Test for getPaginatedStoresList() method when repo returns empty list")
    void testGetPaginatedStoresList_RepoReturnsEmptyList(){
        when(storeRepository.findByStateStateAbbreviation(anyString(), any(PageRequest.class))).thenReturn(Page.empty());

        PaginatedAPIResponse<StoreDTO> result = storeService.getPaginatedStoresList(pageable, "NY");

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(0, result.size());
        assertEquals(0, result.totalPages());
        assertEquals(0, result.totalElements());
        assertEquals(Collections.emptyList(), result.content());

        verify(storeRepository, times(1)).findByStateStateAbbreviation(anyString(), any(PageRequest.class));
    }

    @ParameterizedTest
    @MethodSource("getDifferentStateValues")
    @DisplayName("Test for getPaginatedStoresList() method with empty and null state")
    void testGetPaginatedStoresList_DifferentStateValues(String stateAbbreviation){
        when(storeRepository.findAll(pageable)).thenReturn(page);

        PaginatedAPIResponse<StoreDTO> result = storeService.getPaginatedStoresList(pageable, stateAbbreviation);

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(2, result.totalElements());
        assertEquals(storeDTOList, result.content());

        verify(storeRepository, times(1)).findAll(pageable);
    }
    private static Stream<Arguments> getDifferentStateValues() {
        return Stream.of(Arguments.of(""), null);
    }

    @Test
    public void testUpdateStore_ExistingStore() {

        StoreEntity existingStore = Mockito.mock(StoreEntity.class);
        State state = new State();
        state.setStateAbbreviation("CA");
        existingStore.setId(UUID.randomUUID());
        Mockito.when(stateRepository.findStateByStateAbbreviation(Mockito.anyString())).thenReturn(Optional.of(state));
        Mockito.when(storeRepository.findStoreBySalesforceStoreId(Mockito.anyString())).thenReturn(Optional.of(existingStore));
        StoreDTO request = createSampleStoreDTO();
        String result = storeService.updateStore(request);
        Assertions.assertEquals("Updated Successfully", result);
        verify(existingStore, times(1)).setSalesforceStoreId(Mockito.anyString());
        verify(existingStore, times(1)).setModifiedBy("preston");
        verify(existingStore, times(1)).setModifiedTime(Mockito.any(LocalDateTime.class));
        verify(existingStore, times(1)).setStoreId(request.storeId());
        verify(existingStore, times(1)).setStoreCode(request.storeCode());
        verify(existingStore, times(1)).setCity(request.city());
        verify(existingStore, times(1)).setZipCode(request.zipCode());
        verify(existingStore, times(1)).setAdjustmentValue(request.adjustmentValue());
        verify(existingStore, times(1)).setAdjustmentType(AdjustmentType.valueOf(request.adjustmentType()));
        verify(existingStore, times(1)).setFreightFeeEnabled(request.isFreightFeeEnabled());
        verify(existingStore, times(1)).setInstallationRate(request.installationRate());
        verify(existingStore, times(1)).setInstallationFeeEnabled(request.isInstallationFeeEnabled());
        verify(existingStore, times(1)).setActive(request.isActive());
        verify(storeRepository, times(1)).save(existingStore);
    }


    @Test
    public void testUpdateStore_NewStore() {
        StoreEntity existingStore = Mockito.mock(StoreEntity.class);
        State state = new State();
        state.setStateAbbreviation("CA");
        existingStore.setId(UUID.randomUUID());
        Mockito.when(stateRepository.findStateByStateAbbreviation(Mockito.anyString())).thenReturn(Optional.of(state));
        Mockito.when(storeRepository.findStoreBySalesforceStoreId(Mockito.anyString())).thenReturn(Optional.of(existingStore));
        StoreDTO request = createSampleStoreDTO();
        String result = storeService.updateStore(request);
        Assertions.assertEquals("Updated Successfully", result);
        verify(existingStore, times(1)).setSalesforceStoreId(Mockito.anyString());
        verify(existingStore, times(1)).setModifiedBy("preston");
        verify(existingStore, times(1)).setModifiedTime(Mockito.any(LocalDateTime.class));
        verify(existingStore, times(1)).setStoreId(request.storeId());
        verify(existingStore, times(1)).setStoreCode(request.storeCode());
        verify(existingStore, times(1)).setCity(request.city());
        verify(existingStore, times(1)).setZipCode(request.zipCode());
        verify(existingStore, times(1)).setAdjustmentValue(request.adjustmentValue());
        verify(existingStore, times(1)).setAdjustmentType(AdjustmentType.valueOf(request.adjustmentType()));
        verify(existingStore, times(1)).setFreightFeeEnabled(request.isFreightFeeEnabled());
        verify(existingStore, times(1)).setInstallationRate(request.installationRate());
        verify(existingStore, times(1)).setInstallationFeeEnabled(request.isInstallationFeeEnabled());
        verify(existingStore, times(1)).setActive(request.isActive());
        verify(storeRepository, times(1)).save(existingStore);
    }

    @Test
    public void testUpdateStore_StateNotFoundException() {
        StoreEntity existingStore = new StoreEntity();
        Mockito.when(stateRepository.findStateByStateAbbreviation("CA")).thenReturn(Optional.empty());
        Mockito.when(storeRepository.findStoreBySalesforceStoreId(Mockito.anyString())).thenReturn(Optional.of(existingStore));
        StoreDTO req = createSampleStoreDTO();
        StateNotFoundException exception = assertThrows(StateNotFoundException.class, () -> storeService.updateStore(req));
        assertEquals("State 'CA' not found in the database", exception.getMessage());
        verify(storeRepository, never()).save(any(StoreEntity.class));
    }


    private StoreDTO createSampleStoreDTO() {
        BigDecimal adjustmentValue = new BigDecimal("500.00");
        BigDecimal installationRate = new BigDecimal("200.00");
        return new StoreDTO("245", "CA-906", "SF300", "LosAngles", "CA", "20078", adjustmentValue, "AMOUNT", true, installationRate, true, true);
    }

}
