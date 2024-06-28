package com.containerstore.prestonintegrations.proposal.store.utils;

import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.dto.StoreDTO;

public class StoreConverter {

    private StoreConverter(){
        throw new IllegalStateException("Utility class.");
    }

    public static StoreDTO convertStoreEntityToStoreDTO(StoreEntity store){
        return new StoreDTO(
                store.getStoreId(),
                store.getStoreCode(),
                store.getSalesforceStoreId(),
                store.getCity(),
                store.getState().getStateAbbreviation(),
                store.getZipCode(),
                store.getAdjustmentValue(),
                store.getAdjustmentType().name(),
                store.isFreightFeeEnabled(),
                store.getInstallationRate(),
                store.isInstallationFeeEnabled(),
                store.isActive()
        );
    }
}
