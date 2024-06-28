package com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout;

import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequest;
import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequestSpacesInner;
import com.containerstore.prestonintegrations.proposal.webhook.BaseWebHookEntity;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Collection;

@Slf4j
@Service
public class SalesforceTearOutService implements WebHookService<com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequest> {

    private final SalesforceTearOutRepository salesforceTearoutRepository;

    public SalesforceTearOutService(SalesforceTearOutRepository salesforceTearoutRepository) {
        this.salesforceTearoutRepository = salesforceTearoutRepository;
    }


    @Override
    public <E extends BaseWebHookEntity> Collection<E> getWebhookPersistedDataByKeyAndApp(String key, WebHookConsumers app) {
        return salesforceTearoutRepository.findAllByOpportunityIdAndApp(key,app);
    }

    @Override
    public void handleRequest(com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequest request) {
        this.log(request);
        request.getSpaces()
                .forEach(spaces ->
                        salesforceTearoutRepository
                                .findOneBySpaceIdAndOpportunityId(spaces.getSpaceId(),request.getOpportunityId())
                                .ifPresentOrElse(ob -> syncDatabase(request.getEventId(), prepEntity(request, ob, spaces))
                                        , () -> {
                                            var entity = new SalesforceTearOutEntity();
                                            entity.setCreatedTime(ZonedDateTime.now());
                                            this.syncDatabase(request.getEventId(), this.prepEntity(request, entity, spaces));
                                        }));
    }

    private SalesforceTearOutEntity prepEntity(SalesForceTearOutRequest request, SalesforceTearOutEntity entity, SalesForceTearOutRequestSpacesInner spaces) {
        entity.setOpportunityId(request.getOpportunityId());
        entity.setSpaceId(spaces.getSpaceId());
        entity.setTearOutFee(spaces.getTearOutFee());
        entity.setModifiedTime(ZonedDateTime.now());
        entity.setApp(WebHookConsumers.SALESFORCE);
        entity.setEventId(request.getEventId());
        return entity;
    }

    @Override
    public <E extends BaseWebHookEntity> void syncDatabase(String eventId, E entity) {
        log.info("Syncing database with salesforce mileage information with event id: {}", eventId);
        if (entity instanceof SalesforceTearOutEntity salesforceTearoutEntity) {
            salesforceTearoutRepository.save(salesforceTearoutEntity);
        }
    }

    @Override
    public void log(com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequest request) {
        log.info("Subscribed to salesforce tear out hook - event id: {}", request.getEventId());
    }

    @Override
    @Transactional
    public int deleteEntry(String key, WebHookConsumers app) {
        return salesforceTearoutRepository.deleteByOpportunityIdAndApp(key,app);
    }

    public Page<SalesforceTearOutEntity> getAllTearOutFee(Integer pageNumber, Integer pageSize, String opportunityId) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        return getTearOutEntities(pageable,opportunityId);
    }

    private Page<SalesforceTearOutEntity> getTearOutEntities(Pageable pageable, String opportunityId){
        if(opportunityId == null || opportunityId.isEmpty() || opportunityId.isBlank()){
            return salesforceTearoutRepository.findAll(pageable);
        }

        return salesforceTearoutRepository.findByOpportunityId(opportunityId, pageable);
    }

}
