package com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage;

import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SalesforceMileageRepository extends JpaRepository<SalesforceMileageEntity, UUID> {

    Optional<SalesforceMileageEntity> findOneByOpportunityId(@Param("opportunityId") String id);

    <E> Collection<E> findByOpportunityIdAndApp(@Param("opportunityId") String key,@Param("app") WebHookConsumers app);

    int deleteByOpportunityIdAndApp(@Param("opportunityId") String key,@Param("app") WebHookConsumers app);

    Page<SalesforceMileageEntity> findByOpportunityId(@Param("opportunityId") String opportunityId, Pageable pageable);
}
