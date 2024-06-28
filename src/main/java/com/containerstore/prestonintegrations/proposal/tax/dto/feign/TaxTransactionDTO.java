package com.containerstore.prestonintegrations.proposal.tax.dto.feign;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class TaxTransactionDTO {
    private String transactionId;
    private String sourceSystem;
    private String orderId;
    private ZonedDateTime orderDate;
    private Integer ringStore;
    private LocalDate taxDate;
    private List<TaxLineItemDTO> lineItems;
}
