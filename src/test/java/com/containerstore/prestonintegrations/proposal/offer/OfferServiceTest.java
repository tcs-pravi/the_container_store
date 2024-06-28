package com.containerstore.prestonintegrations.proposal.offer;

import com.containerstore.common.base.money.Money;
import com.containerstore.common.base.validation.ValidationResult;
import com.containerstore.common.base.validation.ValidationSeverity;
import com.containerstore.offer.domain.ImmutablePresentedOfferRequest;
import com.containerstore.offer.domain.OfferOrder;
import com.containerstore.offer.domain.OfferOrderLine;
import com.containerstore.offer.domain.OfferResult;
import com.containerstore.offer.exception.InvalidOfferCodeException;
import com.containerstore.prestonintegrations.proposal.models.Offer;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.offer.dto.DiscountedLineItems;
import com.containerstore.prestonintegrations.proposal.offer.feign.OfferServiceClient;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.sku.domain.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.*;

import static com.containerstore.offer.domain.FulfillmentGroupType.DELIVER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OfferServiceTest {

	@InjectMocks
	OfferService offerService;

	@Mock
	OfferServiceClient offerServiceClient;

    private ProposalFeeRequestV2 request;
    private ProposalFeeResponseV2 response;

    @BeforeEach
    void setup(){
        Map<String, BigDecimal> spaces = new HashMap<>();
        spaces.put("1", BigDecimal.valueOf(30000));
        spaces.put("2", BigDecimal.valueOf(10000));
        request = ProposalTestHelpers.getProposalRequestV2(null, spaces);
        request.getSpaces().forEach(
                space -> space.setSellingPrice(BigDecimal.ZERO)
        );

        response = OfferServiceTestHelper.getResponseWithFees();
    }

	@Test
	public void testApplyOfferWithWinningAndNonWinningOffer() {
        Set<Offer> offers = new HashSet<>();

        Offer offerCode = new Offer();
		offerCode.setOfferCode("PRSTNAMTOFF");
		offers.add(offerCode);

        Offer offerCode2 = new Offer();
        offerCode2.setOfferCode("PRESTOC");
        offers.add(offerCode2);

		request.setOffers(offers);

        OfferOrder offerOrder = new OfferOrder();

		com.containerstore.offer.domain.Offer offer = com.containerstore.offer.domain.Offer.builder()
                .withAdjustmentBenefits(OfferServiceTestHelper.getAdjustmentBenefitsForAmountOff())
				.withOfferName("$500 Off $2.5k Preston Test Offer").withOfferCode("PRSTNAMTOFF").build();

		offerOrder.getWinningOffers().add(offer);

        OfferServiceTestHelper.addFulfillmentGroups(response, request, offerOrder);

        var request1 = ImmutablePresentedOfferRequest.builder().withOfferCode("PRSTNAMTOFF").withRingStore(899).build();

        OfferResult offerResult1 = new OfferResult(offer, Collections.EMPTY_LIST);

        when(offerServiceClient.getPresentedOffer(request1)).thenReturn(offerResult1);
		when(offerServiceClient.apply(any())).thenReturn(offerOrder);

        List<DiscountedLineItems> discountedLineItems = offerService.applyOnProposal(request, response);

        assertNotNull(discountedLineItems);

        var applied = response.getOffers().stream().filter(Offer::getHasApplied).toList();

		applied.forEach(resultOffer -> {
			assertNotNull(resultOffer.getOfferCode());
			assertEquals("PRSTNAMTOFF",resultOffer.getOfferCode());
			assertNotNull(resultOffer.getAdjustmentValue());
			assertNotNull(resultOffer.getAmountOff());
			assertEquals(BigDecimal.valueOf(500), resultOffer.getAmountOff());
			assertNotNull(resultOffer.getAdjustmentType());
			assertNotNull(resultOffer.getHasApplied());
			assertTrue(resultOffer.getHasApplied());
			assertNotNull(resultOffer.getHasError());
			assertNull(resultOffer.getErrorMessage());
		});

        var unApplied = response.getOffers().stream().filter(Offer::getHasError).toList();

        unApplied.forEach(resultOffer -> {
            assertNotNull(resultOffer.getOfferCode());
            assertEquals("PRESTOC", resultOffer.getOfferCode());
            assertFalse(resultOffer.getHasApplied());
            assertNotNull(resultOffer.getHasError());
            assertTrue(resultOffer.getHasError());
            assertNotNull(resultOffer.getErrorMessage());
            assertEquals("Unexpected error encountered. Please retry.", resultOffer.getErrorMessage());
        });
    }

    @Test
    public void testApplyOfferWithTwoValidOffer() {
        Set<Offer> offers = new HashSet<>();

        Offer offerCode1 = new Offer();
        offerCode1.setOfferCode("PRSTPERCOFF");
        offers.add(offerCode1);

        Offer offerCode2 = new Offer();
        offerCode2.setOfferCode("PRSTNAMTOFF");
        offers.add(offerCode2);

        request.setOffers(offers);

        OfferOrder offerOrder = new OfferOrder();

        com.containerstore.offer.domain.Offer offer1 = com.containerstore.offer.domain.Offer.builder()
                .withAdjustmentBenefits(OfferServiceTestHelper.getAdjustmentBenefitsForPercentOff())
                .withOfferName("10% Off $2.5k Preston Test Offer").withOfferCode("PRSTPERCOFF").build();

        com.containerstore.offer.domain.Offer offer2 = com.containerstore.offer.domain.Offer.builder()
                .withAdjustmentBenefits(OfferServiceTestHelper.getAdjustmentBenefitsForAmountOff())
                .withOfferName("$500 Off $2.5k Preston Test Offer").withOfferCode("PRSTNAMTOFF").build();

        offerOrder.getWinningOffers().add(offer1);
        offerOrder.getWinningOffers().add(offer2);

        OfferServiceTestHelper.addFulfillmentGroups(response, request, offerOrder);

        var request1 = ImmutablePresentedOfferRequest.builder().withOfferCode("PRSTPERCOFF").withRingStore(899).build();
        var request2 = ImmutablePresentedOfferRequest.builder().withOfferCode("PRSTNAMTOFF").withRingStore(899).build();

        OfferResult offerResult1 = new OfferResult(offer1, Collections.EMPTY_LIST);
        OfferResult offerResult2 = new OfferResult(offer2, Collections.EMPTY_LIST);

        when(offerServiceClient.getPresentedOffer(request1)).thenReturn(offerResult1);
        when(offerServiceClient.getPresentedOffer(request2)).thenReturn(offerResult2);
        when(offerServiceClient.apply(any())).thenReturn(offerOrder);

        List<DiscountedLineItems> discountedLineItems = offerService.applyOnProposal(request, response);

        assertNotNull(discountedLineItems);
        assertEquals(4, discountedLineItems.size());

        var applied = response.getOffers().stream().filter(Offer::getHasApplied).toList();

        applied.forEach(resultOffer -> {
            assertNotNull(resultOffer.getOfferCode());
            assertNotNull(resultOffer.getAdjustmentValue());
            assertNotNull(resultOffer.getAmountOff());
            assertNotNull(resultOffer.getAdjustmentType());
            assertNotNull(resultOffer.getHasApplied());
            assertTrue(resultOffer.getHasApplied());
            assertNotNull(resultOffer.getHasError());
            assertNull(resultOffer.getErrorMessage());
        });

        assertEquals(2, applied.size());
        assertEquals("PRSTPERCOFF", applied.get(0).getOfferCode());
        assertEquals(BigDecimal.valueOf(10), applied.get(0).getAdjustmentValue());
        assertEquals("PERCENT_OFF", applied.get(0).getAdjustmentType().name());
        assertEquals(BigDecimal.valueOf(6060).doubleValue(), applied.get(0).getAmountOff().doubleValue());

        assertEquals("PRSTNAMTOFF", applied.get(1).getOfferCode());
        assertEquals(BigDecimal.valueOf(500), applied.get(1).getAdjustmentValue());
        assertEquals("AMOUNT_OFF", applied.get(1).getAdjustmentType().name());
        assertEquals(BigDecimal.valueOf(500).doubleValue(), applied.get(1).getAmountOff().doubleValue());

        ArgumentCaptor<OfferOrder> captor = ArgumentCaptor.forClass(OfferOrder.class);
        verify(offerServiceClient).apply(captor.capture());

        OfferOrder offerResponse = captor.getValue();
        assertNotNull(offerResponse);
        assertNotNull(offerResponse.getOfferOrderFulfillmentGroups());

        offerResponse.getOfferOrderFulfillmentGroups().forEach(offerOrderFulfillmentGroup -> {
                assertEquals(DELIVER, offerOrderFulfillmentGroup.getFulfillmentGroupType());
                assertEquals("TCSCLOSETS", offerOrderFulfillmentGroup.getFulfillmentGroupId());
                assertNull(offerOrderFulfillmentGroup.getShippingMethod());
            }
        );
        List<OfferOrderLine> offerOrderLineList = offerResponse.getOfferOrderFulfillmentGroups().get(0).getOfferOrderLines();
        offerOrderLineList.forEach(offerOrderLine -> {
                            assertNotNull(offerOrderLine.getLineId());
                            assertEquals(999925, offerOrderLine.getSkuNumber());
                            assertEquals(1, offerOrderLine.getQuantity());
                            assertNotNull(offerOrderLine.getPreOfferPrice());
                            assertEquals("NONE", offerOrderLine.getSpaceUseId());
                            assertNull(offerOrderLine.getSpaceSource());

                            Sku sku = offerOrderLine.getSku();
                            assertNotNull(sku);

                            assertEquals(String.valueOf(999925), sku.getSkuNumber());
                            assertEquals(25, sku.getDepartmentId());
                            assertNotNull(sku.getRegisterPrice());
                            assertNotNull(sku.getRetailPrice());
                            assertEquals(999925, sku.getDumpSkuId());
                            assertEquals("Dump sku for department 25", sku.getShortDescription());
                            assertEquals("Dump sku for department 25", sku.getLongDescription());
                        }
                );

        OfferOrderLine space1Install = offerOrderLineList.stream().filter(offerOrderLine -> offerOrderLine.getLineId().equalsIgnoreCase("1CW_INSTALL")).findFirst().get();
        OfferOrderLine space2Install = offerOrderLineList.stream().filter(offerOrderLine -> offerOrderLine.getLineId().equalsIgnoreCase("2CW_INSTALL")).findFirst().get();
        OfferOrderLine space1Product = offerOrderLineList.stream().filter(offerOrderLine -> offerOrderLine.getLineId().equalsIgnoreCase("1CW_PRODUCT")).findFirst().get();
        OfferOrderLine space2Product = offerOrderLineList.stream().filter(offerOrderLine -> offerOrderLine.getLineId().equalsIgnoreCase("2CW_PRODUCT")).findFirst().get();

        assertEquals(new Money(4000), space1Install.getPreOfferPrice());
        assertEquals(new Money(1000), space2Install.getPreOfferPrice());
        assertEquals(new Money(30000), space1Product.getPreOfferPrice());
        assertEquals(new Money(10000), space2Product.getPreOfferPrice());
    }

	@Test
	public void testApplyOfferWithExpiredOffer() {
        request.getSpaces().forEach(
                space -> space.setSellingPrice(BigDecimal.valueOf(10000))
        );

        Set<Offer> offers = new HashSet<>();

        Offer offer1 = new Offer();
		offer1.setOfferCode("PRESTOC");
		offers.add(offer1);
		request.setOffers(offers);

        List<ValidationResult> validationResults = new ArrayList<>();
		ValidationResult result = new ValidationResult();
		result.setCode("OFFER-EXPIRED");
		result.setSeverity(ValidationSeverity.ERROR);
		result.setMessage("Offer PRESTOC is no longer valid; it expired on Nov 20, 2023");
		validationResults.add(result);

        com.containerstore.offer.domain.Offer nonWinningOffer = com.containerstore.offer.domain.Offer.builder().withOfferName("20% Off Preston")
				.withOfferCode("PRESTOC")
                .withAdjustmentBenefits(OfferServiceTestHelper.getAdjustmentBenefitsForAmountOff())
                .build();

        OfferResult offerResult = new OfferResult(nonWinningOffer, validationResults);
        OfferOrder offerOrder = new OfferOrder();
		offerOrder.getNonWinningOffers().add(nonWinningOffer);

        when(offerServiceClient.getPresentedOffer(any())).thenReturn(offerResult);
		when(offerServiceClient.apply(any())).thenReturn(offerOrder);

        List<DiscountedLineItems> discountedLineItems = offerService.applyOnProposal(request, response);

        assertNotNull(discountedLineItems);

		response.getOffers().forEach(resultOffer -> {
			assertNotNull(resultOffer.getOfferCode());
			assertNull(resultOffer.getOfferName());
			assertNull(resultOffer.getAdjustmentValue());
			assertNull(resultOffer.getAdjustmentType());
			assertFalse(resultOffer.getHasApplied());
			assertTrue(resultOffer.getHasError());
			assertNotNull(resultOffer.getErrorMessage());
		});

        var nonApplied = response.getOffers().stream().filter(Offer::getHasError).toList();


        nonApplied.forEach(resultOffer -> {
            assertNotNull(resultOffer.getOfferCode());
            assertEquals("PRESTOC",resultOffer.getOfferCode());
            assertNull(resultOffer.getAdjustmentValue());
            assertNull(resultOffer.getAmountOff());
            assertNull(resultOffer.getAdjustmentType());
            assertFalse(resultOffer.getHasApplied());
            assertTrue(resultOffer.getHasError());
            assertNotNull(resultOffer.getErrorMessage());
            assertEquals(validationResults.get(0).getMessage(), resultOffer.getErrorMessage());
        });

	}

    @Test
    void testApplyOnProposal_ShouldThrowInvalidOfferCodeException(){
        Set<Offer> offers = new HashSet<>();

        Offer offer1 = new Offer();
        offer1.setOfferCode("OFFER-CODE");
        offers.add(offer1);
        request.setOffers(offers);

        when(offerServiceClient.getPresentedOffer(any())).thenThrow(new InvalidOfferCodeException("Invalid offer code OFFER-CODE"));

        List<DiscountedLineItems> result = offerService.applyOnProposal(request, response);

        assertNotNull(result);
    }
}
