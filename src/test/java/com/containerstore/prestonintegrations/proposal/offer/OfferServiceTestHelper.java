package com.containerstore.prestonintegrations.proposal.offer;

import com.containerstore.common.base.money.Money;
import com.containerstore.offer.domain.*;
import com.containerstore.prestonintegrations.proposal.offer.dto.OfferOrderLineItems;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.containerstore.offer.domain.FulfillmentGroupType.DELIVER;

public class OfferServiceTestHelper {

    public static void setLineFeeV2(com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 feeResponse, String spaceId, BigDecimal installationLineFee) {
        var lineFee = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner();
        lineFee.setSpaceId(spaceId);
        lineFee.setName(FeeType.INSTALLATION_FEE.name());
        lineFee.setDescription("Installation fee for space %s ".formatted(spaceId));
        lineFee.setFee(installationLineFee.setScale(2, RoundingMode.HALF_EVEN));
        feeResponse.getFees().addLineFeesItem(lineFee);
    }

    public static List<AdjustmentBenefit> getAdjustmentBenefitsForAmountOff() {
        return List.of(
                ImmutableAdjustmentBenefit.builder()
                        .withId(2L)
                        .withAdjustmentType(AdjustmentType.AMOUNT_OFF)
                        .withAdjustmentValue(BigDecimal.valueOf(500))
                        .withAppliesTo(AppliesTo.LINE)
                        .build()
        );
    }

    public static List<AdjustmentBenefit> getAdjustmentBenefitsForPercentOff() {
        return List.of(
                ImmutableAdjustmentBenefit.builder()
                        .withId(2L)
                        .withAdjustmentType(AdjustmentType.PERCENT_OFF)
                        .withAdjustmentValue(BigDecimal.valueOf(10))
                        .withAppliesTo(AppliesTo.LINE)
                        .build()
        );
    }

    public static com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 getResponseWithFees(){
        com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response = ProposalTestHelpers.getProposalResponseV2();
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(4000),
                FeeType.FREIGHT_FEE.name(), "Calculated freight fee"));
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(16600),
                FeeType.INSTALLATION_FEE.name(), "Calculated installation fee"));
        setLineFeeV2(response, "1", BigDecimal.valueOf(1000));
        return response;
    }

    public static void addFulfillmentGroups(com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response, com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request, OfferOrder offerOrder) {
        List<OfferOrderFulfillmentGroup> fulfillmentGroups = fulfillmentGroupsForLinesAndFees(
                makeOfferOrderLineItems(request, response)
        );
        for(OfferOrderFulfillmentGroup group : fulfillmentGroups) {
            offerOrder.getOfferOrderFulfillmentGroups().add(group);
        }
    }

    private static List<OfferOrderLineItems> makeOfferOrderLineItems(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response) {
        List<OfferOrderLineItems> offerOrderLineItems = new ArrayList<>();

        var spaces = request.getSpaces().stream().toList();
        var totalRetailFee = ProposalHelpers.getTotalRetailFeeFromProposalV2(request);
        var totalFreightFee = ProposalHelpers.getHeaderFees(FeeType.FREIGHT_FEE.name(), response);

        spaces.forEach(space -> {
            var installationFee = ProposalHelpers.getLineFee(space.getSpaceId(), FeeType.INSTALLATION_FEE.name(), response);
            var freightFee = ProposalHelpers.getFeePerSpace(totalFreightFee, totalRetailFee, space.getRetailPrice());
            var cwInstall = freightFee.add(installationFee);

            OfferOrderLineItems productItem = new OfferOrderLineItems(space.getSpaceId(), ProductType.CW_PRODUCT,
                    space.getRetailPrice());
            offerOrderLineItems.add(productItem);
            OfferOrderLineItems installItem = new OfferOrderLineItems(space.getSpaceId(), ProductType.CW_INSTALL,
                    cwInstall);
            offerOrderLineItems.add(installItem);
        });

        return offerOrderLineItems;
    }
    private static List<OfferOrderFulfillmentGroup> fulfillmentGroupsForLinesAndFees(List<OfferOrderLineItems> offerOrderLineItems) {
        List<OfferOrderLine> offerOrderLines = new ArrayList<>();
        for (OfferOrderLineItems ob : offerOrderLineItems) {
            offerOrderLines.add(offerOrderLineFromItem(ob.linePrice(), ob.lineItemId().concat(ob.productType().name())));
        }
        com.containerstore.offer.domain.OfferOrderFulfillmentGroupBuilder fgBuilder =
                com.containerstore.offer.domain.OfferOrderFulfillmentGroup.builder()
                        .withFulfillmentGroupType(DELIVER)
                        .withFulfillmentGroupId("TCSCLOSETS")
                        .withOfferOrderLines(offerOrderLines)
                        .withShippingMethod(null);
        return Collections.singletonList(fgBuilder.build());
    }

    private static com.containerstore.offer.domain.OfferOrderLine offerOrderLineFromItem(BigDecimal retailPrice, String lineId) {
        var sku = com.containerstore.sku.domain.Sku.builder()
                .withSkuNumber(String.valueOf(999925))
                .withDepartmentId(25)
                .withRegisterPrice(new Money(retailPrice))
                .withRetailPrice(new Money(retailPrice))
                .withDumpSkuId(999925)
                .withShortDescription("Dump sku for department 25")
                .withLongDescription("Dump sku for department 25")
                .build();
        OfferOrderLineBuilder builder = com.containerstore.offer.domain.OfferOrderLine.builder()
                .withLineId(lineId)
                .withSkuNumber(999925)
                .withQuantity(1)
                .withPreOfferPrice(new Money(retailPrice))
                .withSpaceUseId(null)
                .withSpaceSource(null)
                .withSpaceUseId("NONE")
                .withSku(sku);
        return builder.build();
    }
}
