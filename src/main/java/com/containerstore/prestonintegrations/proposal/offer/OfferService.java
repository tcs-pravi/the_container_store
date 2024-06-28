package com.containerstore.prestonintegrations.proposal.offer;

import com.containerstore.common.base.money.Money;
import com.containerstore.offer.domain.*;
import com.containerstore.offer.exception.InvalidOfferCodeException;
import com.containerstore.prestonintegrations.proposal.offer.dto.DiscountedLineItems;
import com.containerstore.prestonintegrations.proposal.offer.dto.OfferCodeResponseEnvelope;
import com.containerstore.prestonintegrations.proposal.offer.dto.OfferOrderLineItems;
import com.containerstore.prestonintegrations.proposal.offer.feign.OfferServiceClient;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.containerstore.offer.domain.FulfillmentGroupType.DELIVER;

@RequiredArgsConstructor
@Slf4j
@Service
public class OfferService {

    private final OfferServiceClient offerServiceClient;

    public List<DiscountedLineItems> applyOnProposal(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request,
                                                     com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response) {

        List<OfferCodeResponseEnvelope> offerCodeResponseEnvelopes = this.getPresentedOffer(request.getOffers());

        var validOffersFromOfferService = this.getValidOffersFromOfferServiceResponse(offerCodeResponseEnvelopes);

        var invalidOffersFromOfferService = offerCodeResponseEnvelopes.stream()
                .filter(o -> !o.isValid() || o.hasValidationErrors()).toList();

        this.transformInvalidOffersToResponse(request.getOffers(), invalidOffersFromOfferService);

        return this.applyingOffersOnProposal(request, response, validOffersFromOfferService);
    }

    public List<OfferCodeResponseEnvelope> getPresentedOffer(Collection<com.containerstore.prestonintegrations.proposal.models.Offer> offers) {
        List<OfferCodeResponseEnvelope> offerCodeResponseEnvelopes = new ArrayList<>();
        for (com.containerstore.prestonintegrations.proposal.models.Offer offersInner : offers) {
            var offerEnvelope = this.getOfferByCode(offersInner.getOfferCode());
            offerCodeResponseEnvelopes.add(offerEnvelope);
        }
        return offerCodeResponseEnvelopes;
    }

    @NotNull
    private  List<Offer> getValidOffersFromOfferServiceResponse(List<OfferCodeResponseEnvelope> offerCodeResponseEnvelopes) {
        return offerCodeResponseEnvelopes.stream()
                .filter(offerCodeResponseEnvelope -> offerCodeResponseEnvelope.isValid() && !offerCodeResponseEnvelope.hasValidationErrors())
                .map(OfferCodeResponseEnvelope::offer).map(Optional::get).toList();
    }

    private List<DiscountedLineItems> applyingOffersOnProposal(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response, List<Offer> validOffersFromOfferService) {

        if (!validOffersFromOfferService.isEmpty()) {
            List<OfferOrderLineItems> offerOrderLineItems = this.makeOfferOrderLineItems(request, response);
            log.info("Applying offers on opportunity id: {}", request.getOpportunityId());
            var offerorder = OfferOrderBuilder.builder()
                    .withRingStore(899)
                    .withPurchasingChannel(PurchasingChannel.WEB)
                    .withProspectiveOffers(validOffersFromOfferService)
                    .withShippingFeeType("SHIPPING7")
                    .withSourceReferenceId(request.getOpportunityId())
                    .withFulfillmentGroups(this.fulfillmentGroupsForLinesAndFees(offerOrderLineItems))
                    .withPurchasingCustomer(null)
                    .build();

            var appliedOfferOrder = offerServiceClient.apply(offerorder);

            var totalSpacePrice = Boolean.TRUE.equals(ProposalHelpers.hasSellingPrice(request.getSpaces()))
                    ? ProposalHelpers.getTotalSellingPriceFromProposal(request):
                    ProposalHelpers.getTotalRetailFeeFromProposalV2(request);

            var totalProposalFee = ProposalHelpers.getHeaderFees(FeeType.INSTALLATION_FEE.name(), response)
                    .add(ProposalHelpers.getHeaderFees(FeeType.FREIGHT_FEE.name(), response))
                    .add(totalSpacePrice);

            this.buildWinningOfferResponse(totalProposalFee, request.getOffers(), appliedOfferOrder.getWinningOffers());

            this.buildNonWinningOfferResponse(request.getOffers(), appliedOfferOrder.getNonWinningOffers());

            response.setOffers(request.getOffers());

            return this.getDiscountedLineItems(appliedOfferOrder);
        } else {
            log.info("No valid Offers for opportunity id: {}", request.getOpportunityId());
            response.setOffers(request.getOffers());
            return Collections.emptyList();
        }
    }

    private List<DiscountedLineItems> getDiscountedLineItems(OfferOrder offerOrderResponse) {
        List<DiscountedLineItems> discountedLineItemRespons = new ArrayList<>();
        offerOrderResponse.getOfferOrderFulfillmentGroups().stream()
                .map(OfferOrderFulfillmentGroup::getOfferOrderLines)
                .flatMap(List::stream)
                .forEach(offerOrderLine -> {
                    List<OrderLineAdjustment> orderLineAdjustments = offerOrderLine.getLineAdjustments();
                    Money totalOfferAmount = orderLineAdjustments.stream().map(OrderLineAdjustment::getUnitAdjustment).reduce(Money.ZERO, Money::add);
                    DiscountedLineItems discountedLineItems = new DiscountedLineItems(offerOrderLine.getLineId(),
                            totalOfferAmount.getAmount());
                    discountedLineItemRespons.add(discountedLineItems);
                });
        return discountedLineItemRespons;
    }

    private List<OfferOrderLineItems> makeOfferOrderLineItems(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response) {

        var spaces = request.getSpaces().stream().toList();
        var totalFreightFee = ProposalHelpers.getHeaderFees(FeeType.FREIGHT_FEE.name(), response);

        return Boolean.TRUE.equals(ProposalHelpers.hasSellingPrice(spaces)) ?
                this.getOfferOrderLineItemsBasedOnSellingPrice(request, response, spaces, totalFreightFee)
                :this.getOfferOrderLineItemsBasedOnRetailPrice(request, response, spaces, totalFreightFee);
    }

    private List<OfferOrderLineItems> getOfferOrderLineItemsBasedOnRetailPrice(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response, List<com.containerstore.prestonintegrations.proposal.models.Space> spaces, BigDecimal totalFreightFee) {
        log.info("Applying offers on opportunity id: {} based on retail price", request.getOpportunityId());
        var totalRetailFee = ProposalHelpers.getTotalRetailFeeFromProposalV2(request);

        List<OfferOrderLineItems> offerOrderLineItems = new ArrayList<>();

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

    private List<OfferOrderLineItems> getOfferOrderLineItemsBasedOnSellingPrice(com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request, com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2 response, List<com.containerstore.prestonintegrations.proposal.models.Space> spaces, BigDecimal totalFreightFee) {
        log.info("Applying offers on opportunity id: {} based on selling price", request.getOpportunityId());

        var totalSellingPrice = ProposalHelpers.getTotalSellingPriceFromProposal(request);

        List<OfferOrderLineItems> offerOrderLineItems = new ArrayList<>();

        spaces.forEach(space -> {
            var installationFee = ProposalHelpers.getLineFee(space.getSpaceId(), FeeType.INSTALLATION_FEE.name(), response);
            var freightFee = ProposalHelpers.getFeePerSpace(totalFreightFee, totalSellingPrice, space.getSellingPrice());
            var cwInstall = freightFee.add(installationFee);

            OfferOrderLineItems productItem = new OfferOrderLineItems(space.getSpaceId(), ProductType.CW_PRODUCT,
                    space.getSellingPrice());
            offerOrderLineItems.add(productItem);
            OfferOrderLineItems installItem = new OfferOrderLineItems(space.getSpaceId(), ProductType.CW_INSTALL,
                    cwInstall);
            offerOrderLineItems.add(installItem);
        });
        return offerOrderLineItems;
    }

    private void transformInvalidOffersToResponse(Set<com.containerstore.prestonintegrations.proposal.models.Offer> offers, List<OfferCodeResponseEnvelope> invalidOffersFromOfferService) {
        for (OfferCodeResponseEnvelope nonWinning : invalidOffersFromOfferService) {
            offers.forEach(requestOffer -> {
                if (requestOffer.getOfferCode().equals(nonWinning.offerCode())) {
                    log.info("Invalid offers: {}", requestOffer.getOfferCode());
                    requestOffer.setHasApplied(false);
                    requestOffer.setHasError(true);
                    requestOffer.setErrorMessage(nonWinning.message());
                    if (nonWinning.hasValidationErrors()) {
                        requestOffer.setErrorMessage(nonWinning.validationResults().get(0).getMessage());
                    }
                }
            });
        }
    }

    private void buildNonWinningOfferResponse(Set<com.containerstore.prestonintegrations.proposal.models.Offer> offers, Set<Offer> nonWinningOffers) {
        for (Offer nonWinning : nonWinningOffers) {
            offers.forEach(requestOffer -> {
                if (requestOffer.getOfferCode().equals(nonWinning.getOfferCode())) {
                    log.info("Non-Winning offers: {}", requestOffer.getOfferCode());
                    requestOffer.setOfferName(nonWinning.getOfferName());
                    if (nonWinning.getAdjustmentBenefits() != null && !nonWinning.getAdjustmentBenefits().isEmpty()) {
                        requestOffer.setAdjustmentValue(nonWinning.getAdjustmentBenefits().get(0).getAdjustmentValue());
                        requestOffer.setAdjustmentType(com.containerstore.prestonintegrations.proposal.models.Offer.AdjustmentTypeEnum.valueOf(nonWinning.getAdjustmentBenefits().get(0).getAdjustmentType().name()));
                    }
                        requestOffer.setHasApplied(false);
                        requestOffer.setHasError(true);
                        requestOffer.setErrorMessage("Offer code cannot be applied");
                }
            });
        }
    }

    private void buildWinningOfferResponse(BigDecimal totalRetailFee, Set<com.containerstore.prestonintegrations.proposal.models.Offer> offerProposalRequest, Set<Offer> winningOffers) {
        for (Offer winningOffer : winningOffers) {
            for (com.containerstore.prestonintegrations.proposal.models.Offer requestOffer : offerProposalRequest) {
                if (requestOffer.getOfferCode().equals(winningOffer.getOfferCode())) {
                    log.info("Winning offers: {}", requestOffer.getOfferCode());
                    requestOffer.setOfferName(winningOffer.getOfferName());
                    requestOffer.setAdjustmentValue(winningOffer.getAdjustmentBenefits().get(0).getAdjustmentValue());
                    requestOffer.setAmountOff(winningOffer.getAdjustmentBenefits().get(0).getAdjustmentValue());
                    requestOffer.setAdjustmentType(com.containerstore.prestonintegrations.proposal.models.Offer.AdjustmentTypeEnum.valueOf(winningOffer.getAdjustmentBenefits().get(0).getAdjustmentType().name()));
                    requestOffer.setHasApplied(true);
                    requestOffer.setHasError(false);
                    requestOffer.setErrorMessage(null);
                    if (Boolean.TRUE.equals(requestOffer.getHasApplied()) && requestOffer.getAdjustmentType()
                            .compareTo(com.containerstore.prestonintegrations.proposal.models.Offer.AdjustmentTypeEnum.PERCENT_OFF) == 0) {
                        requestOffer.setAmountOff(totalRetailFee.multiply((winningOffer.getAdjustmentBenefits()
                                .get(0).getAdjustmentValue().divide(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_EVEN))).setScale(2, RoundingMode.HALF_EVEN));
                    }
                }
            }
        }
    }

    private OfferCodeResponseEnvelope getOfferByCode(String code) {
        try {
            var result = offerServiceClient
                    .getPresentedOffer(ImmutablePresentedOfferRequest
                            .builder()
                            .withOfferCode(code)
                            .withRingStore(899)
                            .build());
            return new OfferCodeResponseEnvelope(Optional.ofNullable(result.getOffer()), code, "OK", result.getValidationResults());
        } catch (InvalidOfferCodeException e) {
            log.info("Invalid offer code {}", e.getMessage());
            return new OfferCodeResponseEnvelope(Optional.empty(), code, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Error while fetching offer code from offer service", e);
            return new OfferCodeResponseEnvelope(Optional.empty(), code, "Unexpected error encountered. Please retry.", null);
        }
    }

    private List<OfferOrderFulfillmentGroup> fulfillmentGroupsForLinesAndFees(List<OfferOrderLineItems> offerOrderLineItems) {
        List<OfferOrderLine> offerOrderLines = new ArrayList<>();
        for (OfferOrderLineItems ob : offerOrderLineItems) {
            offerOrderLines.add(this.offerOrderLineFromItem(ob.linePrice(),
                    ProposalHelpers.makeLineItemId(ob.lineItemId(), ob.productType().name())));
        }
        com.containerstore.offer.domain.OfferOrderFulfillmentGroupBuilder fgBuilder =
                com.containerstore.offer.domain.OfferOrderFulfillmentGroup.builder()
                        .withFulfillmentGroupType(DELIVER)
                        .withFulfillmentGroupId("TCSCLOSETS")
                        .withOfferOrderLines(offerOrderLines)
                        .withShippingMethod(null);
        return Collections.singletonList(fgBuilder.build());
    }

    private com.containerstore.offer.domain.OfferOrderLine offerOrderLineFromItem(BigDecimal linePrice, String lineId) {
        var sku = com.containerstore.sku.domain.Sku.builder()
                .withSkuNumber(String.valueOf(999925))
                .withDepartmentId(25)
                .withRegisterPrice(new Money(linePrice))
                .withRetailPrice(new Money(linePrice))
                .withDumpSkuId(999925)
                .withShortDescription("Dump sku for department 25")
                .withLongDescription("Dump sku for department 25")
                .build();
        OfferOrderLineBuilder builder = com.containerstore.offer.domain.OfferOrderLine.builder()
                .withLineId(lineId)
                .withSkuNumber(999925)
                .withQuantity(1)
                .withPreOfferPrice(new Money(linePrice))
                .withSpaceUseId(null)
                .withSpaceSource(null)
                .withSpaceUseId("NONE")
                .withSku(sku);
        return builder.build();
    }
}
