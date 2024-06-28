package com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout;

import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequest;
import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequestSpacesInner;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesforceTearOutServiceTest {

    @InjectMocks
    private SalesforceTearOutService salesforceTearOutService;
    @Mock
    private SalesforceTearOutRepository salesforceTearOutRepository;

    @Test
    public void testGetWebhookPersistedDataByKeyAndApp() {
        SalesforceTearOutEntity entity = new SalesforceTearOutEntity();
        entity.setEventId("event_id");
        when(salesforceTearOutRepository.findAllByOpportunityIdAndApp(any(), any())).thenReturn(List.of(entity));
        Collection<SalesforceTearOutEntity> result = salesforceTearOutService.getWebhookPersistedDataByKeyAndApp("opp_id", WebHookConsumers.SALESFORCE);
        assertNotNull(result);
        assertTrue(result.contains(entity));
    }

    @Test
    public void testHandleRequestForTearOutUpdate() {
        SalesforceTearOutEntity entity = new SalesforceTearOutEntity();
        when(salesforceTearOutRepository.findOneBySpaceIdAndOpportunityId(any(), any())).thenReturn(Optional.of(entity));
        salesforceTearOutService.handleRequest(getTearOutRequest());
        assertNotNull(entity.getTearOutFee());
        assertNotNull(entity.getOpportunityId());
        assertNotNull(entity.getApp());
        assertNotNull(entity.getModifiedTime());
        assertNotNull(entity.getSpaceId());
        assertNotNull(entity.getEventId());
        verify(salesforceTearOutRepository).findOneBySpaceIdAndOpportunityId(any(), any());
        verify(salesforceTearOutRepository).save(any());
    }

    @Test
    public void testHandleRequestForNewTearOutRequest() {
        SalesforceTearOutEntity entity = new SalesforceTearOutEntity();
        SalesForceTearOutRequest tearOutRequest = getTearOutRequest();

        when(salesforceTearOutRepository.findOneBySpaceIdAndOpportunityId(any(),any())).thenReturn(Optional.empty());

        salesforceTearOutService.handleRequest(tearOutRequest);

        assertNull(entity.getCreatedTime());
        verify(salesforceTearOutRepository).findOneBySpaceIdAndOpportunityId(any(), any());

        ArgumentCaptor<SalesforceTearOutEntity> entityCaptor = ArgumentCaptor.forClass(SalesforceTearOutEntity.class);
        verify(salesforceTearOutRepository).save(entityCaptor.capture());

        SalesforceTearOutEntity savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity.getCreatedTime());
        assertNotNull(savedEntity.getModifiedTime());
        assertEquals(tearOutRequest.getOpportunityId(), savedEntity.getOpportunityId());
        assertEquals(tearOutRequest.getSpaces().get(0).getSpaceId(), savedEntity.getSpaceId());
        assertEquals(tearOutRequest.getSpaces().get(0).getTearOutFee(), savedEntity.getTearOutFee());
        assertEquals(tearOutRequest.getEventId(), savedEntity.getEventId());
        assertEquals(WebHookConsumers.SALESFORCE, savedEntity.getApp());
    }

    private SalesForceTearOutRequest getTearOutRequest() {
        SalesForceTearOutRequest request = new SalesForceTearOutRequest();
        request.setEventId("event_id");
        request.setOpportunityId("opp_id");
        ArrayList<SalesForceTearOutRequestSpacesInner> spacesInnerArrayList = new ArrayList<>();
        SalesForceTearOutRequestSpacesInner spaces = new SalesForceTearOutRequestSpacesInner();
        spaces.setSpaceId("space_id");
        spaces.setTearOutFee(BigDecimal.valueOf(150));
        spacesInnerArrayList.add(spaces);
        request.setSpaces(spacesInnerArrayList);
        return request;
    }

    @Test
    public void testDeleteEntry() {
        String key = "opportunityId";
        WebHookConsumers app = WebHookConsumers.SALESFORCE;

        when(salesforceTearOutRepository.deleteByOpportunityIdAndApp(key, app)).thenReturn(1);

        int result = salesforceTearOutService.deleteEntry(key, app);

        verify(salesforceTearOutRepository, times(1)).deleteByOpportunityIdAndApp(key, app);

        assertEquals(1, result);
    }

    @Test
    public void testGetAllTearOutFee() {
        int pageNumber = 0;
        int pageSize = 10;
        String opportunityId = "opportunityId";

        SalesforceTearOutEntity entity1 = new SalesforceTearOutEntity();
        entity1.setOpportunityId(opportunityId);
        entity1.setSpaceId("SpaceId1");
        entity1.setTearOutFee(BigDecimal.valueOf(100));

        SalesforceTearOutEntity entity2 = new SalesforceTearOutEntity();
        entity2.setOpportunityId(opportunityId);
        entity2.setSpaceId("SpaceId2");
        entity2.setTearOutFee(BigDecimal.valueOf(200));

        List<SalesforceTearOutEntity> list = List.of(entity1, entity2);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<SalesforceTearOutEntity> mockPage = new PageImpl<>(list, pageable,2);

        when(salesforceTearOutRepository.findByOpportunityId(opportunityId, pageable))
                .thenReturn(mockPage);

        Page<SalesforceTearOutEntity> resultPage = salesforceTearOutService.getAllTearOutFee(pageNumber, pageSize, opportunityId);

        verify(salesforceTearOutRepository, times(1)).findByOpportunityId(opportunityId, pageable);

        assertEquals(mockPage, resultPage);
    }

    @Test
    void testGetAllTearOutFee_WhenOpportunityIdIsEmpty_ReturnsAllTearOutEntities() {
        int pageNumber = 0;
        int pageSize = 10;
        String opportunityId = "";

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<SalesforceTearOutEntity> expectedPage = new PageImpl<>(Collections.emptyList());

        when(salesforceTearOutRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<SalesforceTearOutEntity> result = salesforceTearOutService.getAllTearOutFee(pageNumber, pageSize, opportunityId);

        assertEquals(expectedPage, result);
        verify(salesforceTearOutRepository).findAll(pageable);
    }
}
