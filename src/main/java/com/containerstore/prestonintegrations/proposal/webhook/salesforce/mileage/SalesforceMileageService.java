package com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage;

import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceMileageRequest;
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
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Slf4j
@Service
public class SalesforceMileageService implements WebHookService<com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceMileageRequest> {

    private final SalesforceMileageRepository salesforceMileageRepository;

    public SalesforceMileageService(SalesforceMileageRepository salesforceMileageRepository) {
        this.salesforceMileageRepository = salesforceMileageRepository;
    }


    @Override
    public <E extends BaseWebHookEntity> Collection<E> getWebhookPersistedDataByKeyAndApp(String key, WebHookConsumers app) {
        return this.salesforceMileageRepository.findByOpportunityIdAndApp(key,app);
    }


    @Override
    public void handleRequest(SalesForceMileageRequest request) {
        this.log(request);
        salesforceMileageRepository
                .findOneByOpportunityId(request.getOpportunityId())
                    .ifPresentOrElse(ob -> syncDatabase(request.getEventId(), prepEntity(request, ob))
                            ,() -> {
                                    var entity = new SalesforceMileageEntity();
                                    entity.setCreatedTime(ZonedDateTime.now());
                                    this.syncDatabase(request.getEventId(), prepEntity(request, entity));
                    });
    }

    private SalesforceMileageEntity prepEntity(SalesForceMileageRequest request, SalesforceMileageEntity entity) {
        entity.setOpportunityId(request.getOpportunityId());
        entity.setMiles(request.getMiles());
        entity.setInstallationStoreId(request.getInstallationStoreId());
        entity.setDurationValue(request.getDriveTime().getDuration());
        entity.setChronoUnit(ChronoUnit.valueOf(request.getDriveTime().getChronoUnit().name()));
        entity.setModifiedTime(ZonedDateTime.now());
        entity.setApp(WebHookConsumers.SALESFORCE);
        entity.setEventId(request.getEventId());
        return entity;
    }

    @Override
    public <E extends BaseWebHookEntity> void syncDatabase(String eventId, E entity) {
        log.info("Syncing database with salesforce mileage information with event id: {}", eventId);
        if (entity instanceof SalesforceMileageEntity salesForceMileageEntity) {
            salesforceMileageRepository.save(salesForceMileageEntity);
        }
    }

    @Override
    public void log(SalesForceMileageRequest request) {
        log.info("Subscribed to salesforce mileage hook - event id: {}", request.getEventId());
    }

    @Override
    @Transactional
    public int deleteEntry(String key, WebHookConsumers app) {
        return salesforceMileageRepository.deleteByOpportunityIdAndApp(key,app);
    }

    public Page<SalesforceMileageEntity> getAllMileageFee(Integer pageNumber, Integer pageSize, String opportunityId) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        return  getMileageEntities(pageable,opportunityId);
    }

    private Page<SalesforceMileageEntity> getMileageEntities(Pageable pageable, String opportunityId) {
        if(opportunityId == null || opportunityId.isEmpty() || opportunityId.isBlank()){
            return salesforceMileageRepository.findAll(pageable);
        }

        return salesforceMileageRepository.findByOpportunityId(opportunityId, pageable);
    }

}
