package com.containerstore.prestonintegrations.proposal.store.dto;

import java.math.BigDecimal;

public record StoreDTO (
        String storeId,
        String storeCode,
        String salesforceStoreId,
        String city,
        String stateAbbreviation,
        String zipCode,
        BigDecimal adjustmentValue,
        String adjustmentType,
        boolean isFreightFeeEnabled,
        BigDecimal installationRate,
        boolean isInstallationFeeEnabled,
        boolean isActive
) {}

