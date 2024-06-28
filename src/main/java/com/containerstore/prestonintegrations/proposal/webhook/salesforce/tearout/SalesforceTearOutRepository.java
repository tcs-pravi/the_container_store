package com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout;

import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SalesforceTearOutRepository extends JpaRepository<SalesforceTearOutEntity, UUID> {
    Optional<SalesforceTearOutEntity> findOneBySpaceIdAndOpportunityId(@Param("spaceId") String spaceId,@Param("opportunityId") String opportunityId);
    <E> Collection<E> findAllByOpportunityIdAndApp(@Param("opportunityId") String opportunityId, @Param("app") WebHookConsumers app);

    int deleteByOpportunityIdAndApp(@Param("opportunityId") String key,@Param("app") WebHookConsumers app);

    Page<SalesforceTearOutEntity> findByOpportunityId(@Param("opportunityId") String opportunityId, Pageable pageable);
}
