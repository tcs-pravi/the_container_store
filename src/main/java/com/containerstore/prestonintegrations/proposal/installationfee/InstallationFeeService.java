package com.containerstore.prestonintegrations.proposal.installationfee;

import com.containerstore.common.base.exception.BusinessException;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.installationfee.dto.SpaceInstallationFee;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.models.Space;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsService;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"squid:S103","java:S121"})
@RequiredArgsConstructor
@Slf4j
@Service
public class InstallationFeeService {


    private final ProposalConstantsService proposalConstantsService;

    private final StoreRepository storeRepository;

    /***
     * This method calculates installation fee based on Retail & Selling price of spaces.
     * If we receive only retail price in proposal fee request, Installation fee calculated based on retail price will be added to proposal fee response.
     * If we receive both retail & selling price in proposal fee request, Installation fee calculated based on selling price will be added to proposal fee response.
     *
     * @param feeRequest Proposal fee request
     * @param feeResponse Proposal fee response
     * @param mileage Mileage details received from Salesforce
     * @return ProposalFee
     */
	public ProposalFee getInstallationFeeV2(ProposalFeeRequestV2 feeRequest, ProposalFeeResponseV2 feeResponse,
                                            SalesforceMileageEntity mileage) {
		var totalSellingPriceInProposal = ProposalHelpers.getTotalSellingPriceFromProposal(feeRequest);
		var installationFeeBasedOnSellingPrice = totalSellingPriceInProposal.compareTo(BigDecimal.ZERO) > 0 ? this.getInstallationFeeBasedOnSellingPrice(feeRequest, feeResponse, mileage, totalSellingPriceInProposal) : BigDecimal.ZERO;
		var installationFeeBasedOnRetailPrice = this.getInstallationFeeBasedOnRetailPrice(feeRequest, feeResponse, mileage, totalSellingPriceInProposal);

        return this.getProposalFeeObject(feeRequest, totalSellingPriceInProposal, installationFeeBasedOnSellingPrice, installationFeeBasedOnRetailPrice);
	}

    /***
     * This method calculates installation fee based on Retail price of spaces.
     * If we do not receive selling price in proposal fee request, Installation fee calculated based on retail price will be added to proposal fee response.
     *
     * @param feeRequest
     * @param feeResponse
     * @param mileage
     * @param totalSellingPriceInProposal
     */
    public BigDecimal getInstallationFeeBasedOnRetailPrice(ProposalFeeRequestV2 feeRequest, ProposalFeeResponseV2 feeResponse, SalesforceMileageEntity mileage, BigDecimal totalSellingPriceInProposal) {
        var totalRetailFeeInProposal = ProposalHelpers.getTotalRetailFeeFromProposalV2(feeRequest);
        var installationFeeBasedOnRetailPrice = this.calculateInstallationFeeForRetailPrice(totalRetailFeeInProposal, mileage.getInstallationStoreId()).setScale(2,RoundingMode.HALF_EVEN);
        if(totalSellingPriceInProposal.compareTo(BigDecimal.ZERO) == 0) {
            addHeaderAndLineFees(feeRequest, feeResponse, totalRetailFeeInProposal, installationFeeBasedOnRetailPrice);
        }
        return installationFeeBasedOnRetailPrice;
    }

    /**
     * This method calculates installation fee based on selling price of spaces and will add it to proposal fee response.
     *
     * @param feeRequest
     * @param feeResponse
     * @param mileage
     * @param totalSellingPriceInProposal
     */
    public BigDecimal getInstallationFeeBasedOnSellingPrice(ProposalFeeRequestV2 feeRequest, ProposalFeeResponseV2 feeResponse, SalesforceMileageEntity mileage, BigDecimal totalSellingPriceInProposal) {
        var installationFeeBasedOnSellingPrice = this.calculateInstallationFeeForRetailPrice(totalSellingPriceInProposal, mileage.getInstallationStoreId()).setScale(2,RoundingMode.HALF_EVEN);
        addHeaderAndLineFees(feeRequest, feeResponse, totalSellingPriceInProposal, installationFeeBasedOnSellingPrice);
        return installationFeeBasedOnSellingPrice;
    }

    private ProposalFee getProposalFeeObject(ProposalFeeRequestV2 feeRequest, BigDecimal totalSellingPriceInProposal, BigDecimal installationFeeBasedOnSellingPrice, BigDecimal installationFeeBasedOnRetailPrice) {
        var proposalFee = new ProposalFee();
        proposalFee.setInstallationFeeBasedOnRetailPrice(installationFeeBasedOnRetailPrice);
        proposalFee.setInstallationFeeBasedOnSellingPrice(installationFeeBasedOnSellingPrice);
        proposalFee.setDifferenceBetweenRetailAndSellingPrice(ProposalHelpers.getTotalRetailFeeFromProposalV2(feeRequest).subtract(totalSellingPriceInProposal));
        proposalFee.setDifferenceBetweenInstallFeeBasedOnRetailAndSellingPrice(installationFeeBasedOnRetailPrice.subtract(installationFeeBasedOnSellingPrice));
        proposalFee.setTotalSellingPriceInProposal(totalSellingPriceInProposal);
        return proposalFee;
    }

    public void addHeaderAndLineFees(ProposalFeeRequestV2 feeRequest,ProposalFeeResponseV2 feeResponse, BigDecimal totalPriceInProposal, BigDecimal installationFee){
        this.buildLineInstallationFeeV2(feeRequest,feeResponse, totalPriceInProposal, installationFee);
        feeResponse.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(installationFee, FeeType.INSTALLATION_FEE.name(),
                "Installation calculated for proposal with opportunity id: %s".formatted(feeRequest.getOpportunityId())));
    }

    public ProposalFeeResponse getInstallationFee(ProposalFeeRequest feeRequest, ProposalFeeResponse feeResponse, SalesforceMileageEntity mileage) {
        var totalRetailFeeInProposal = ProposalHelpers.getTotalRetailFeeFromProposal(feeRequest);
        var calculatedInstallationFeeForProposal = this.calculateInstallationFeeForRetailPrice(totalRetailFeeInProposal, mileage.getInstallationStoreId()).setScale(2,RoundingMode.HALF_EVEN);

        this.buildLineInstallationFee(feeRequest,feeResponse, totalRetailFeeInProposal, calculatedInstallationFeeForProposal);

        feeResponse.getTaxableFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(calculatedInstallationFeeForProposal, FeeType.INSTALLATION_FEE.name(),
                "Installation calculated for proposal with opportunity id: %s".formatted(feeRequest.getOpportunityId())));
        return feeResponse;
    }

    private BigDecimal calculateInstallationFeeForRetailPrice(BigDecimal retailPrice, String installationStoreId) {
        BigDecimal actualCalculatedCost = retailPrice.multiply(this.getInstallationRate(installationStoreId));
        return proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_INSTALLATION_FEE).getValue().max(actualCalculatedCost);
    }

    private BigDecimal getInstallationRate(String installationStoreId) {
        return storeRepository.findStoreBySalesforceStoreId(installationStoreId)
                .filter(store -> store.isActive() && store.isInstallationFeeEnabled())
                .map(StoreEntity::getInstallationRate)
                .orElseGet(() -> proposalConstantsService.getProposalConstants(ProposalConstantKeys.INSTALLATION_RATE).getValue());
    }

    private Set<SpaceInstallationFee> calculateMultiSpaceInstallationFee(List<Space> spaceIdPriceMap, BigDecimal totalInstallationFee, BigDecimal totalRetailPrice) {
        Set<SpaceInstallationFee> spaceInstallationFeeCollection = new LinkedHashSet<>();
        if(spaceIdPriceMap.size() > 1) {
            int maxCount = spaceIdPriceMap.size()-1;
            int counter = 1;
            BigDecimal totalInstallSpaceFee = BigDecimal.valueOf(0.0);
            for (Space space : spaceIdPriceMap) {
                var spacePrice = (space.getSellingPrice() != null && space.getSellingPrice().compareTo(BigDecimal.ZERO) > 0)  ? space.getSellingPrice() : space.getRetailPrice();
                SpaceInstallationFee spaceInstallationFee = null;
                if (counter <= maxCount) {
                    spaceInstallationFee = new SpaceInstallationFee(space.getSpaceId(), spacePrice,
                            this.getRatioInstallationFeePerSpace(totalInstallationFee, totalRetailPrice, spacePrice));
                    totalInstallSpaceFee= totalInstallSpaceFee.add(spaceInstallationFee.InstallationFee());

                }else{
                    spaceInstallationFee = new SpaceInstallationFee(space.getSpaceId(), spacePrice, totalInstallationFee.subtract(totalInstallSpaceFee));
                }
                spaceInstallationFeeCollection.add(spaceInstallationFee);
                counter++;
            }
        }
        if(spaceInstallationFeeCollection.isEmpty())
            throw new BusinessException("Failed to calculate Multiple space installation Fee");
        return spaceInstallationFeeCollection;
    }

    private BigDecimal getRatioInstallationFeePerSpace(BigDecimal totalInstallationFee, BigDecimal totalRetailPrice, BigDecimal retailPrice) {
        return totalInstallationFee.multiply(retailPrice.divide(totalRetailPrice, 12, RoundingMode.FLOOR)).setScale(2, RoundingMode.HALF_EVEN);
    }

    private void buildLineInstallationFeeV2(ProposalFeeRequestV2 feeRequest, ProposalFeeResponseV2 feeResponse, BigDecimal totalRetailFeeInProposal, BigDecimal calculatedInstallationFeeForProposal) {
        var spaces = feeRequest.getSpaces().stream().toList();
        if(spaces.isEmpty())
            throw new BusinessException("No spaces found while calculating line fee for opportunity id {}",feeRequest.getOpportunityId());
        if(feeRequest.getSpaces().size()>1) {
            var installationFeeSplitForSpaces = this.calculateMultiSpaceInstallationFee(spaces, calculatedInstallationFeeForProposal, totalRetailFeeInProposal);
            if(installationFeeSplitForSpaces.isEmpty())
                throw new BusinessException("Failed to split installation fee for multiple spaces");
            for (SpaceInstallationFee spaceInstallationFee : installationFeeSplitForSpaces) {
                this.setLineFeeV2(feeResponse, spaceInstallationFee.spaceId(), spaceInstallationFee.InstallationFee());
            }
        } else {
            this.setLineFeeV2(feeResponse, spaces.get(0).getSpaceId(), calculatedInstallationFeeForProposal);
        }
        if(feeResponse.getFees().getLineFees().isEmpty())
            throw new BusinessException("Failed to build Line Fee response for Installation Fee");
    }


    private void buildLineInstallationFee(ProposalFeeRequest feeRequest, ProposalFeeResponse feeResponse, BigDecimal totalRetailFeeInProposal, BigDecimal calculatedInstallationFeeForProposal) {
        var spaces = feeRequest.getSpaces().stream().toList();
        if(spaces.isEmpty())
            throw new BusinessException("No spaces found while calculating line fee for opportunity id {}",feeRequest.getOpportunityId());
        if(feeRequest.getSpaces().size()>1) {
            var installationFeeSplitForSpaces = this.calculateMultiSpaceInstallationFee(spaces, calculatedInstallationFeeForProposal, totalRetailFeeInProposal);
            if(installationFeeSplitForSpaces.isEmpty())
                throw new BusinessException("Failed to split installation fee for multiple spaces");
            for (SpaceInstallationFee spaceInstallationFee : installationFeeSplitForSpaces) {
                this.setLineFee(feeResponse, spaceInstallationFee.spaceId(), spaceInstallationFee.InstallationFee());
            }
        } else {
            this.setLineFee(feeResponse, spaces.get(0).getSpaceId(), calculatedInstallationFeeForProposal);
        }
        if(feeResponse.getTaxableFees().getLineFees().isEmpty())
            throw new BusinessException("Failed to build Line Fee response for Installation Fee");
    }

    private void setLineFee(ProposalFeeResponse feeResponse, String spaceId, BigDecimal installationLineFee) {
        var lineFee = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner();
        lineFee.setSpaceId(spaceId);
        lineFee.setName(FeeType.INSTALLATION_FEE.name());
        lineFee.setDescription("Installation fee for space %s ".formatted(spaceId));
        lineFee.setFee(installationLineFee.setScale(2,RoundingMode.HALF_EVEN));
        feeResponse.getTaxableFees().addLineFeesItem(lineFee);
    }

    private void setLineFeeV2(ProposalFeeResponseV2 feeResponse, String spaceId, BigDecimal installationLineFee) {
        var lineFee = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner();
        lineFee.setSpaceId(spaceId);
        lineFee.setName(FeeType.INSTALLATION_FEE.name());
        lineFee.setDescription("Installation fee for space %s ".formatted(spaceId));
        lineFee.setFee(installationLineFee.setScale(2,RoundingMode.HALF_EVEN));
        feeResponse.getFees().addLineFeesItem(lineFee);
    }

}
