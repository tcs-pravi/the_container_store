package com.containerstore.prestonintegrations.proposal.tax.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaxRequestDTO {
    private final String opportunityId;
    private final String streetAddress1;
    private final String streetAddress2;
    private final String city;
    private final String county;
    private final String state;
    private final String country;
    private final String zipCode;
    private List<TaxableFee> taxableFees;
}
