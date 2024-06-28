package com.containerstore.prestonintegrations.proposal.tax.service;

import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2TaxInner;
import com.containerstore.prestonintegrations.proposal.offer.dto.DiscountedLineItems;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.tax.dto.LocationDTO;
import com.containerstore.prestonintegrations.proposal.tax.dto.TaxRequestDTO;
import com.containerstore.prestonintegrations.proposal.tax.dto.TaxableFee;
import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxLineItemDTO;
import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxTransactionDTO;
import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;
import com.containerstore.prestonintegrations.proposal.tax.enums.TaxAttributes;
import com.containerstore.prestonintegrations.proposal.tax.feign.EnterpriseTaxServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

import static org.apache.commons.lang.StringUtils.EMPTY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxService {

    private final EnterpriseTaxServiceClient enterpriseTaxServiceClient;

    public void getTax(ProposalFeeRequestV2 proposalFeeRequest, ProposalFeeResponseV2 proposalFeeResponse, List<DiscountedLineItems> discountedLineItemsList) {

        var spaces = proposalFeeRequest.getSpaces().stream().toList();

        var totalSpacePrice = Boolean.TRUE.equals(ProposalHelpers.hasSellingPrice(spaces))
                ? ProposalHelpers.getTotalSellingPriceFromProposal(proposalFeeRequest):
                ProposalHelpers.getTotalRetailFeeFromProposalV2(proposalFeeRequest);

        var totalFreightFee = ProposalHelpers.getHeaderFees(FeeType.FREIGHT_FEE.name(), proposalFeeResponse);
        var totalAdditionalFee = ProposalHelpers.getTaxableTotalAdditionalFee(proposalFeeResponse);

        List<TaxableFee> taxableFees = new ArrayList<>();
        spaces.forEach(space -> {
            TaxableFee cwProductFee = this.getCWProductFeeWithDiscount(discountedLineItemsList, space);

            var totalInstallAmount = this.getTotalInstallAmount(proposalFeeResponse, totalSpacePrice, totalFreightFee, space);
            TaxableFee cwInstallFee = this.getCWInstallFeeWithDiscount(discountedLineItemsList, space, totalInstallAmount);

            taxableFees.addAll(Arrays.asList(cwProductFee, cwInstallFee));
            TaxableFee taxAdditional = this.getCWDemoFee(totalSpacePrice, totalAdditionalFee, space, proposalFeeResponse);
            if (!Objects.isNull(taxAdditional)) {
                taxableFees.add(taxAdditional);
            }
        });


        var tax = TaxRequestDTO.builder()
                .opportunityId(proposalFeeRequest.getOpportunityId())
                .state(proposalFeeRequest.getCustomerAddress().getState())
                .city(proposalFeeRequest.getCustomerAddress().getCity())
                .taxableFees(taxableFees)
                .streetAddress2(getStreetAddress2(proposalFeeRequest))
                .streetAddress1(getStreetAddress1(proposalFeeRequest))
                .country("USA")
                .county("")
                .zipCode(proposalFeeRequest.getCustomerAddress().getZipCode())
                .build();

        var taxResponse = new ProposalFeeResponseV2TaxInner();
        taxResponse.setTaxName("Total Tax");
        taxResponse.setAmount(BigDecimal.valueOf(this.calculateTax(tax).get("totalTax").asDouble()));
        taxResponse.setHasError(false);
        proposalFeeResponse.setTax(Collections.singletonList(taxResponse));
    }

    private String getStreetAddress2(ProposalFeeRequestV2 proposalFeeRequest) {
        String streetAddress2 = proposalFeeRequest.getCustomerAddress().getAddress2();
        return null != streetAddress2 ? streetAddress2 : EMPTY;
    }

    private String getStreetAddress1(ProposalFeeRequestV2 proposalFeeRequest) {
        String streetAddress1 = proposalFeeRequest.getCustomerAddress().getAddress1();
        return null != streetAddress1 ? streetAddress1 : EMPTY;
    }

    private TaxableFee getCWDemoFee(BigDecimal totalRetailFee, BigDecimal totalAdditionalFee, com.containerstore.prestonintegrations.proposal.models.Space space, ProposalFeeResponseV2 proposalFeeResponse) {
        var spacePrice = space.getSellingPrice()!=null && space.getSellingPrice().compareTo(BigDecimal.ZERO) > 0  ? space.getSellingPrice() : space.getRetailPrice();
        var cwDemo = (ProposalHelpers.getFeePerSpace(totalAdditionalFee, totalRetailFee, spacePrice)
                .add(ProposalHelpers.getLineFee(space.getSpaceId(), FeeType.ADDITIONAL_SERVICES_DEMOLITION.name(),
                        proposalFeeResponse)))
                .setScale(2, RoundingMode.HALF_EVEN);

        if (cwDemo.compareTo(BigDecimal.ZERO) > 0) {
            return new TaxableFee(cwDemo.doubleValue(), cwDemo.doubleValue(), cwDemo, 0.00, ProductType.CW_DEMO);
        }
        return null;
    }

    private BigDecimal getTotalInstallAmount(ProposalFeeResponseV2 proposalFeeResponse, BigDecimal totalRetailFee, BigDecimal totalFreightFee, com.containerstore.prestonintegrations.proposal.models.Space space) {
        var installationFee = ProposalHelpers.getLineFee(space.getSpaceId(), FeeType.INSTALLATION_FEE.name(), proposalFeeResponse);
        var spacePrice = space.getSellingPrice()!=null && space.getSellingPrice().compareTo(BigDecimal.ZERO) > 0  ? space.getSellingPrice() : space.getRetailPrice();
        var freightFee = ProposalHelpers.getFeePerSpace(totalFreightFee, totalRetailFee, spacePrice);
        return freightFee.add(installationFee);
    }

    private TaxableFee getCWProductFeeWithDiscount(List<DiscountedLineItems> discountedLineItemsList, com.containerstore.prestonintegrations.proposal.models.Space space) {
        String productKey = space.getSpaceId() + ProductType.CW_PRODUCT;
        var cwProduct = space.getSellingPrice()!=null && space.getSellingPrice().compareTo(BigDecimal.ZERO) > 0  ? space.getSellingPrice() : space.getRetailPrice();
        var cwProductDiscountAmount = this.getDiscountAmount(discountedLineItemsList, productKey);
        var cwProductAfterDiscount = cwProduct.subtract(cwProductDiscountAmount.abs()).setScale(2, RoundingMode.HALF_EVEN);
        return new TaxableFee(cwProductAfterDiscount.doubleValue(), cwProductAfterDiscount.doubleValue(), cwProductAfterDiscount, cwProductDiscountAmount.abs().doubleValue(), ProductType.CW_PRODUCT);
    }

    private TaxableFee getCWInstallFeeWithDiscount(List<DiscountedLineItems> discountedLineItemsList, com.containerstore.prestonintegrations.proposal.models.Space space, BigDecimal cwInstall) {
        String installKey = space.getSpaceId() + ProductType.CW_INSTALL;
        var cwInstallDiscountAmount = this.getDiscountAmount(discountedLineItemsList, installKey);
        var cwInstallAfterDiscount = cwInstall.subtract(cwInstallDiscountAmount.abs()).setScale(2, RoundingMode.HALF_EVEN);
        return new TaxableFee(cwInstallAfterDiscount.doubleValue(), cwInstallAfterDiscount.doubleValue(), cwInstallAfterDiscount, cwInstallDiscountAmount.abs().doubleValue(), ProductType.CW_INSTALL);
    }

    private BigDecimal getDiscountAmount(List<DiscountedLineItems> discountedLineItemsList, String key) {
        return discountedLineItemsList == null ? BigDecimal.ZERO :
                discountedLineItemsList.stream()
                        .filter(discountedLineItems -> discountedLineItems.lineItemId().equalsIgnoreCase(key)).findFirst()
                        .map(DiscountedLineItems::discountAmount).orElse(BigDecimal.ZERO);
    }

    private JsonNode calculateTax(TaxRequestDTO taxRequestDTO) {
        return enterpriseTaxServiceClient.calculateTax(getTaxTransactionDTO(taxRequestDTO));
    }

    private TaxTransactionDTO getTaxTransactionDTO(TaxRequestDTO taxRequestDTO) {
        return TaxTransactionDTO.builder()
                .taxDate(LocalDate.now())
                .transactionId(UUID.randomUUID().toString())
                .orderId(taxRequestDTO.getOpportunityId())
                .orderDate(ZonedDateTime.now())
                .ringStore(Integer.valueOf(TaxAttributes.RING_STORE.value()))
                .sourceSystem(TaxAttributes.SOURCE_SYSTEM.value())
                .lineItems(this.getLineItems(taxRequestDTO))
                .build();
    }


    private List<TaxLineItemDTO> getLineItems(TaxRequestDTO taxRequestDTO) {
        List<TaxLineItemDTO> lineItemDTOS = new ArrayList<>();
        for (TaxableFee taxableFee : taxRequestDTO.getTaxableFees()) {
            lineItemDTOS.add(this.buildATaxLineItem(String.valueOf(taxableFee.productType().ordinal()), taxableFee.productType(),
                    taxableFee.unitPrice(), taxableFee.unitPrice().doubleValue(), taxableFee.extendedPrice(),
                    taxableFee.discountAmount(), this.getCustomerLocation(taxRequestDTO)));
        }
        return lineItemDTOS;
    }

    private TaxLineItemDTO buildATaxLineItem(String lineItemId, ProductType productType,
                                             BigDecimal unitPrice, double extendedPrice,
                                             double retailPrice, double discount, LocationDTO customerLocation) {
        return TaxLineItemDTO.builder()
                .lineItemId(lineItemId)
                .sku(productType.type())
                .unitPrice(unitPrice)
                .extendedPrice(extendedPrice)
                .retailPrice(retailPrice)
                .discountAmount(discount)
                .customerLocationDTO(customerLocation)
                .adminOriginTaxAreaId(Integer.parseInt(TaxAttributes.ADMIN_ORIGIN_TAX_AREA_ID.value()))
                .physicalOriginTaxAreaId(Integer.parseInt(TaxAttributes.PHYSICAL_ORIGIN_TAX_AREA_ID.value()))
                .constructionItem(Boolean.TRUE)
                .quantity(BigDecimal.ONE)
                .departmentCode(Integer.parseInt(TaxAttributes.DEPARTMENT_CODE.value()))
                .fulfillmentType(TaxAttributes.DELIVER.value())
                .locationCode(TaxAttributes.LOCATION_CODE.value()).build();
    }

    private LocationDTO getCustomerLocation(TaxRequestDTO taxRequestDTO) {
        return LocationDTO.builder()
                .streetAddress(taxRequestDTO.getStreetAddress1())
                .streetAddress2(taxRequestDTO.getStreetAddress2())
                .city(taxRequestDTO.getCity())
                .county(taxRequestDTO.getCounty())
                .state(taxRequestDTO.getState())
                .country(taxRequestDTO.getCountry())
                .zipCode(taxRequestDTO.getZipCode())
                .build();
    }
}
