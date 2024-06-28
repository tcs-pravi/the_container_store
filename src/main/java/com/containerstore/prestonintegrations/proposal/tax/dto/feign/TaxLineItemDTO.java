package com.containerstore.prestonintegrations.proposal.tax.dto.feign;

import com.containerstore.prestonintegrations.proposal.tax.dto.LocationDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TaxLineItemDTO {
    private String lineItemId;
    private Boolean constructionItem;
    private BigDecimal quantity;
    private int departmentCode;
    private String fulfillmentType;
    private Integer adminOriginTaxAreaId;
    private Integer physicalOriginTaxAreaId;
    private String sku;
    private String locationCode;
    private BigDecimal unitPrice;
    private double extendedPrice;
    private double retailPrice;
    private double discountAmount;
    private LocationDTO customerLocationDTO;
}
