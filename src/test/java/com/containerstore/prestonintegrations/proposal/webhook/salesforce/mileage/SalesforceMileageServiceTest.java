package com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage;

import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceMileageRequest;
import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceMileageRequestDriveTime;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesforceMileageServiceTest {

    @InjectMocks
    private SalesforceMileageService salesforceMileageService;
    @Mock
    private SalesforceMileageRepository salesforceMileageRepository;

    @Test
    public void testGetWebhookPersistedDataByKeyAndApp() {
        SalesforceMileageEntity entity = new SalesforceMileageEntity();
        when(salesforceMileageRepository.findByOpportunityIdAndApp(any(), any())).thenReturn(List.of(entity));
        Collection<SalesforceMileageEntity> result = salesforceMileageService.getWebhookPersistedDataByKeyAndApp("opp_id", WebHookConsumers.SALESFORCE);
        assertNotNull(result);
        assertTrue(result.contains(entity));
    }

    @Test
    public void testHandleRequest() {
        SalesforceMileageEntity entity = new SalesforceMileageEntity();
        entity.setEventId("event_id");
        entity.setApp(WebHookConsumers.SALESFORCE);
        when(salesforceMileageRepository.findOneByOpportunityId(any())).thenReturn(Optional.of(entity));
        SalesForceMileageRequest request = getMileageRequest();
        salesforceMileageService.handleRequest(request);
        assertNotNull(entity.getDuration());
        assertNotNull(entity.getOpportunityId());
        assertNotNull(entity.getMiles());
        assertNotNull(entity.getChronoUnit());
        assertNotNull(entity.getDurationValue());
        assertNotNull(entity.getModifiedTime());
        assertNotNull(entity.getEventId());
        assertNotNull(entity.getApp());
        assertNotNull(entity.getInstallationStoreId());
        verify(salesforceMileageRepository).save(any());
    }

    @Test
    public void testInvalidHandleRequest() {
        SalesForceMileageRequest request = getMileageRequest();

        when(salesforceMileageRepository.findOneByOpportunityId(any())).thenReturn(Optional.empty());

        salesforceMileageService.handleRequest(request);

        verify(salesforceMileageRepository).findOneByOpportunityId(any());

        ArgumentCaptor<SalesforceMileageEntity> entityCaptor = ArgumentCaptor.forClass(SalesforceMileageEntity.class);
        verify(salesforceMileageRepository).save(entityCaptor.capture());

        SalesforceMileageEntity savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity.getCreatedTime());
        assertEquals(request.getOpportunityId(), savedEntity.getOpportunityId());
        assertEquals(request.getMiles(), savedEntity.getMiles());
        assertEquals(request.getInstallationStoreId(), savedEntity.getInstallationStoreId());
        assertEquals(request.getDriveTime().getDuration(), savedEntity.getDurationValue());
        assertEquals(request.getDriveTime().getChronoUnit().name(), savedEntity.getChronoUnit().name());
        assertNotNull(savedEntity.getModifiedTime());
        assertEquals(request.getEventId(), savedEntity.getEventId());
        assertEquals(WebHookConsumers.SALESFORCE, savedEntity.getApp());
    }

    @Test
    public void testHandleRequest1() {
        when(salesforceMileageRepository.findOneByOpportunityId(any())).thenReturn(Optional.of(new SalesforceMileageEntity()));
        salesforceMileageService.handleRequest(getMileageRequest());
        verify(salesforceMileageRepository).findOneByOpportunityId(any());
        verify(salesforceMileageRepository).save(any());
    }

    @Test
    void testDeleteEntry() {
        String key = "someKey";
        WebHookConsumers app = WebHookConsumers.SALESFORCE;
        int expectedDeletedCount = 1;

        when(salesforceMileageRepository.deleteByOpportunityIdAndApp(key, app)).thenReturn(expectedDeletedCount);

        int deletedCount = salesforceMileageService.deleteEntry(key, app);

        assertEquals(expectedDeletedCount, deletedCount);
        verify(salesforceMileageRepository).deleteByOpportunityIdAndApp(key, app);
    }

    @Test
    void testGetAllMileageFee_WhenOpportunityIdIsEmpty_ReturnsAllMileageEntities() {
        int pageNumber = 0;
        int pageSize = 10;
        String opportunityId = "";

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<SalesforceMileageEntity> expectedPage = new PageImpl<>(Collections.emptyList());

        when(salesforceMileageRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<SalesforceMileageEntity> result = salesforceMileageService.getAllMileageFee(pageNumber, pageSize, opportunityId);

        assertEquals(expectedPage, result);
        verify(salesforceMileageRepository).findAll(pageable);
    }

    @Test
    void testGetAllMileageFee_WhenOpportunityIdIsNotEmpty_ReturnsMileageEntitiesByOpportunityId() {
        int pageNumber = 0;
        int pageSize = 10;
        String opportunityId = "someOpportunityId";

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<SalesforceMileageEntity> expectedPage = new PageImpl<>(Collections.emptyList());

        when(salesforceMileageRepository.findByOpportunityId(opportunityId, pageable)).thenReturn(expectedPage);

        Page<SalesforceMileageEntity> result = salesforceMileageService.getAllMileageFee(pageNumber, pageSize, opportunityId);

        assertEquals(expectedPage, result);
        verify(salesforceMileageRepository).findByOpportunityId(opportunityId, pageable);
    }

    private SalesForceMileageRequest getMileageRequest() {
        SalesForceMileageRequestDriveTime driveTime = new SalesForceMileageRequestDriveTime();
        driveTime.setDuration(BigDecimal.valueOf(1.2));
        driveTime.setChronoUnit(SalesForceMileageRequestDriveTime.ChronoUnitEnum.DAYS);
        SalesForceMileageRequest request = new SalesForceMileageRequest();
        request.setDriveTime(driveTime);
        request.setEventId("event_id");
        request.setOpportunityId("opp_id");
        request.setMiles(BigDecimal.valueOf(35));
        request.setInstallationStoreId("1");
        return request;
    }
}
