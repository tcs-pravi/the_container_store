package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;

import static com.containerstore.common.base.money.Money.ZERO;
import static com.containerstore.offer.domain.FeeTypes.AVERA_INSTALL_FEE;
import static com.containerstore.offer.domain.FeeTypes.ELFA_INSTALL_FEE;

public enum FeeMinimum {
    ELFA_BASIC_INSTALL(ELFA_INSTALL_FEE, new Money("180.00")),
    AVERA_BASIC_INSTALL(AVERA_INSTALL_FEE, new Money("250.00")),
    DEFAULT("", ZERO);

    private final String type;
    private final Money minimum;

    FeeMinimum(final String type, final Money minimum) {
        this.type = type;
        this.minimum = minimum;
    }

    public String getType() {
        return type;
    }

    public Money getMinimum() {
        return minimum;
    }

    public static Money minimumFeeForType(String feeType) {
        if (ELFA_BASIC_INSTALL.getType().equals(feeType)) {
            return ELFA_BASIC_INSTALL.getMinimum();
        }
        if (AVERA_BASIC_INSTALL.getType().equals(feeType)) {
            return AVERA_BASIC_INSTALL.getMinimum();
        }
        return DEFAULT.getMinimum();
    }
}
