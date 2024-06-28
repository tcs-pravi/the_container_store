package com.containerstore.prestonintegrations.proposal.offer.dto;

import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;

import java.math.BigDecimal;

public record OfferOrderLineItems(String lineItemId, ProductType productType, BigDecimal linePrice) {
}
