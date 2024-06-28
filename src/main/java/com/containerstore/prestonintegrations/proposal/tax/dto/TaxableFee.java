package com.containerstore.prestonintegrations.proposal.tax.dto;

import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;

import java.math.BigDecimal;

public record TaxableFee(double retailPrice, double extendedPrice, BigDecimal unitPrice, double discountAmount, ProductType productType) {
}
