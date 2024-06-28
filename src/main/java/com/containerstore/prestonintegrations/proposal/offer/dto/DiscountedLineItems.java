package com.containerstore.prestonintegrations.proposal.offer.dto;

import java.math.BigDecimal;

public record DiscountedLineItems(String lineItemId, BigDecimal discountAmount) {
}
