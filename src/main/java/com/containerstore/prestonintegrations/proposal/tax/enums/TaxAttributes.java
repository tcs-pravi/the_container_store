package com.containerstore.prestonintegrations.proposal.tax.enums;

import com.containerstore.common.base.conversion.Coded;

public enum TaxAttributes {

    DELIVER("DELIVER"),
    LOCATION_CODE("WEB"),
    SOURCE_SYSTEM("PRESTON_INTEGRATION"),
    DEPARTMENT_CODE("25"),
    RING_STORE("899"),
    ADMIN_ORIGIN_TAX_AREA_ID("441139839"),
    PHYSICAL_ORIGIN_TAX_AREA_ID("441139839");

    private final String value;

    TaxAttributes(String value) {
        this.value = value;
    }

    @Coded
    public String value() {
        return value;
    }
}
