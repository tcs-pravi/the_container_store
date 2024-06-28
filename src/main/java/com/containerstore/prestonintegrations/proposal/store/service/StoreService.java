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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    private final StateRepository stateRepository;

    public PaginatedAPIResponse<StoreDTO> getPaginatedStoresList(Pageable pageable, String stateAbbreviation) {
        Page<StoreEntity> storeListPage = getStoreEntities(pageable, stateAbbreviation);

        if (storeListPage.isEmpty()) return new PaginatedAPIResponse<>(0, 0, 0, 0L, Collections.emptyList());

        List<StoreDTO> storeDTOList = storeListPage.stream()
                .map(StoreConverter::convertStoreEntityToStoreDTO)
                .toList();

        return new PaginatedAPIResponse<>(
                storeListPage.getPageable().getPageNumber(),
                storeListPage.getPageable().getPageSize(),
                storeListPage.getTotalPages(),
                storeListPage.getTotalElements(),
                storeDTOList
        );
    }

    private Page<StoreEntity> getStoreEntities(Pageable pageable, String stateAbbreviation) {
        if(stateAbbreviation == null || stateAbbreviation.isEmpty() || stateAbbreviation.isBlank()){
            return storeRepository.findAll(pageable);
        }

        return storeRepository.findByStateStateAbbreviation(stateAbbreviation.toUpperCase(), pageable);
    }

    public String updateStore(StoreDTO request) {
        Optional<StoreEntity> existingStoreOptional = storeRepository.findStoreBySalesforceStoreId(request.salesforceStoreId());

        StoreEntity store = existingStoreOptional.map(existingStore -> {
            existingStore.setSalesforceStoreId(request.salesforceStoreId());
            updateOrSetStoreDetails(existingStore, request);
            existingStore.setModifiedBy("preston");
            existingStore.setModifiedTime(LocalDateTime.now());
            return existingStore;
        }).orElseGet(() -> {
            StoreEntity newStore = new StoreEntity();
            newStore.setSalesforceStoreId(request.salesforceStoreId());
            updateOrSetStoreDetails(newStore, request);
            newStore.setCreatedBy("preston");
            newStore.setCreatedTime(LocalDateTime.now());
            return newStore;
        });

        storeRepository.save(store);

        return existingStoreOptional.isPresent() ? "Updated Successfully" : "Created Successfully";
    }


    private void updateOrSetStoreDetails(StoreEntity store, StoreDTO request) {
        store.setStoreId(request.storeId());
        store.setStoreCode(request.storeCode());
        store.setCity(request.city());
        store.setZipCode(request.zipCode());
        store.setAdjustmentValue(request.adjustmentValue());
        store.setAdjustmentType(AdjustmentType.valueOf(request.adjustmentType()));
        store.setFreightFeeEnabled(request.isFreightFeeEnabled());
        store.setInstallationRate(request.installationRate());
        store.setInstallationFeeEnabled(request.isInstallationFeeEnabled());
        store.setActive(request.isActive());

        String newAbbreviation = request.stateAbbreviation();
        Optional<State> newStateOptional = stateRepository.findStateByStateAbbreviation(newAbbreviation);
        newStateOptional.ifPresentOrElse(
                store::setState,
                () -> {
                    throw new StateNotFoundException("State '%s' not found in the database".formatted(newAbbreviation));
                }
        );
    }

}
