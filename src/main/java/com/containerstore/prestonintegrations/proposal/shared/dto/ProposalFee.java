package com.containerstore.prestonintegrations.proposal.shared.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProposalFee{
	
    BigDecimal installationFeeBasedOnRetailPrice;
    
    BigDecimal installationFeeBasedOnSellingPrice;
    
    BigDecimal differenceBetweenRetailAndSellingPrice;
    
    BigDecimal differenceBetweenInstallFeeBasedOnRetailAndSellingPrice;
    
    BigDecimal totalSellingPriceInProposal;
    
    BigDecimal freightFeeBasedOnRetailPrice;
}
