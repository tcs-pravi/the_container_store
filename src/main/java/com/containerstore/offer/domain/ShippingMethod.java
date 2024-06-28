package com.containerstore.offer.domain;

import com.containerstore.common.base.conversion.Coded;

public enum ShippingMethod {
    ADC_EMPLOYEE_ORDERS(true),
    DFW_EMPLOYEE_ORDERS(true),
    EMPLOYEE_ORDERS(true),
    EXPEDITED,
    MOTOR_FREIGHT,
    STANDARD,
    UPS;

    private boolean employeeShipMethod;

    ShippingMethod() {
        this(false);
    }

    ShippingMethod(boolean employeeShipMethod) {
        this.employeeShipMethod = employeeShipMethod;
    }

    public boolean isEmployeeShipMethod() {
        return employeeShipMethod;
    }

    @Coded
    public String label() {
        return name().replace('_', ' ');
    }
}
