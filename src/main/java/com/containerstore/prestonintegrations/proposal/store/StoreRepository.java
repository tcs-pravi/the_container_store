package com.containerstore.prestonintegrations.proposal.store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {

    Optional<StoreEntity> findStoreBySalesforceStoreId(String salesforceStoreId);

    Page<StoreEntity> findByStateStateAbbreviation(
            @Param("stateAbbreviation") String stateAbbreviation, Pageable pageable);
}
