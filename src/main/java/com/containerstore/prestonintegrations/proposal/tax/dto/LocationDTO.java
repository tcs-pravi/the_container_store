package com.containerstore.prestonintegrations.proposal.tax.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDTO {
    private String streetAddress;
    private String streetAddress2;
    private String city;
    private String state;
    private String county;
    private String zipCode;
    private String country;
    private int taxAreaId;
}
