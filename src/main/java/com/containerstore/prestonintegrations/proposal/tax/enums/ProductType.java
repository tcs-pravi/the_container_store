package com.containerstore.prestonintegrations.proposal.tax.enums;

import com.containerstore.common.base.conversion.Coded;

public enum ProductType {
    CW_PRODUCT("cw_product"),
    CW_INSTALL("cw_install"),
    CW_DEMO("cw_demo");

    private final String type;

    ProductType(String type) {
        this.type = type;
    }

    @Coded
    public String type() {
        return type;
    }
}
