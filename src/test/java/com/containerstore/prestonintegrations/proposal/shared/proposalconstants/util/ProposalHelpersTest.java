package com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util;

import com.containerstore.common.base.exception.BusinessException;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProposalHelpers.class)
class ProposalHelpersTest {
    private ProposalFeeRequest proposalFeeRequest;
    private ProposalFeeRequestV2 proposalFeeRequestV2;
    private ProposalFeeResponseV2 proposalFeeResponseV2;

    @BeforeEach
    void setUp(){
        Map<String, BigDecimal> spaces = new HashMap<>();
        spaces.put("1", BigDecimal.valueOf(40000));
        spaces.put("2", BigDecimal.valueOf(5000));
        proposalFeeRequestV2 = ProposalTestHelpers.getProposalRequestV2("state", spaces);

        proposalFeeResponseV2 = ProposalTestHelpers.getProposalResponseV2();

        proposalFeeResponseV2.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(333),
                FeeType.FREIGHT_FEE.name(), "Calculated freight fee"));
        proposalFeeResponseV2.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(203),
                FeeType.INSTALLATION_FEE.name(), "Calculated installation fee"));

        proposalFeeRequest = ProposalTestHelpers.getProposalRequest("state", spaces);

        ProposalFeeResponseTaxableFeesLineFeesInner innerLineFee = new ProposalFeeResponseTaxableFeesLineFeesInner();
        innerLineFee.setSpaceId("1");
        innerLineFee.setName(FeeType.INSTALLATION_FEE.name());
        innerLineFee.setDescription("Installation fee for space %s ".formatted("1"));
        innerLineFee.setFee(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
        proposalFeeResponseV2.getFees().addLineFeesItem(innerLineFee);
    }

    @Test
    @DisplayName("Test for getHeaderFees()")
    void testGetHeaderFees(){
        BigDecimal headerFee = ProposalHelpers.getHeaderFees(FeeType.FREIGHT_FEE.name(), proposalFeeResponseV2);

        assertNotNull(headerFee);
        assertEquals(BigDecimal.valueOf(333).doubleValue(), headerFee.doubleValue());

        BusinessException exception = assertThrows(BusinessException.class, () -> ProposalHelpers.getHeaderFees("feeName", proposalFeeResponseV2));
        assertEquals("Error while getting Fee : feeName", exception.getMessage());
    }

    @Test
    @DisplayName("Test for getTotalRetailFeeFromProposalV2()")
    void testGetTotalRetailFeeFromProposalV2(){
        BigDecimal headerFee = ProposalHelpers.getTotalRetailFeeFromProposalV2(proposalFeeRequestV2);

        assertNotNull(headerFee);
        assertEquals(BigDecimal.valueOf(45000).doubleValue(), headerFee.doubleValue());

        ProposalFeeRequestV2 invalidRequest = ProposalTestHelpers.getProposalRequestV2("state", new HashMap<>());
        BusinessException exception = assertThrows(BusinessException.class, () -> ProposalHelpers.getTotalRetailFeeFromProposalV2(invalidRequest));
        assertEquals("Error while calculating sum of retail prices", exception.getMessage());
    }

    @Test
    @DisplayName("Test for getTotalRetailFeeFromProposalV2()")
    void testGetTotalRetailFeeFromProposal(){
        BigDecimal headerFee = ProposalHelpers.getTotalRetailFeeFromProposal(proposalFeeRequest);

        assertNotNull(headerFee);
        assertEquals(BigDecimal.valueOf(45000).doubleValue(), headerFee.doubleValue());

        ProposalFeeRequest invalidRequest = ProposalTestHelpers.getProposalRequest("state", new HashMap<>());
        BusinessException exception = assertThrows(BusinessException.class, () -> ProposalHelpers.getTotalRetailFeeFromProposal(invalidRequest));
        assertEquals("Error while calculating sum of retail prices", exception.getMessage());
    }

    @Test
    @DisplayName("Test for getTaxableTotalAdditionalFee()")
    void testGetTaxableTotalAdditionalFee(){
        BigDecimal additionalFee1 = ProposalHelpers.getTaxableTotalAdditionalFee(proposalFeeResponseV2);

        assertNotNull(additionalFee1);
        assertEquals(BigDecimal.valueOf(0).doubleValue(), additionalFee1.doubleValue());

        proposalFeeResponseV2.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(300),
                FeeType.ADDITIONAL_SERVICES_FEE.name(), "Calculated additional fee"));
        proposalFeeResponseV2.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(150),
                FeeType.ADDITIONAL_SERVICES_TRANSPORTATION.name(), "Calculated additional transportation fee"));

        BigDecimal additionalFee2 = ProposalHelpers.getTaxableTotalAdditionalFee(proposalFeeResponseV2);

        assertNotNull(additionalFee2);
        assertEquals(BigDecimal.valueOf(450).doubleValue(), additionalFee2.doubleValue());
    }

    @Test
    @DisplayName("Test for getLineFee()")
    void testGetLineFee(){
        BigDecimal lineFee1 = ProposalHelpers.getLineFee("1", FeeType.INSTALLATION_FEE.name(), proposalFeeResponseV2);

        assertNotNull(lineFee1);
        assertEquals(BigDecimal.valueOf(1000).doubleValue(), lineFee1.doubleValue());

        BigDecimal lineFee2 = ProposalHelpers.getLineFee("invalid", "feeName", proposalFeeResponseV2);

        assertNotNull(lineFee2);
        assertEquals(BigDecimal.valueOf(0).doubleValue(), lineFee2.doubleValue());
    }

    @Test
    void makeLineItemId() {
        var result = ProposalHelpers.makeLineItemId("SPACEID", ProductType.CW_PRODUCT.name());
        Assertions.assertTrue(result.endsWith(ProductType.CW_PRODUCT.name()));
    }

    @Test
    void testHasSellingPrice(){
        var space = new com.containerstore.prestonintegrations.proposal.models.Space();
        space.setRetailPrice(BigDecimal.valueOf(1000));
        space.setSellingPrice(null);

        assertFalse(ProposalHelpers.hasSellingPrice(Collections.singleton(space)));

        space.setSellingPrice(BigDecimal.ZERO);
        assertFalse(ProposalHelpers.hasSellingPrice(Collections.singleton(space)));

        space.setSellingPrice(BigDecimal.valueOf(-10));
        assertFalse(ProposalHelpers.hasSellingPrice(Collections.singleton(space)));

        space.setSellingPrice(BigDecimal.valueOf(1000));
        assertTrue(ProposalHelpers.hasSellingPrice(Collections.singleton(space)));
    }

    @Test
    void testGetMapOfSpaceAndSellingFee(){
        Map<String, BigDecimal> mapOfSpaceAndSellingFee1 = ProposalHelpers.getMapOfSpaceAndSellingFee(proposalFeeRequestV2);
        assertNotNull(mapOfSpaceAndSellingFee1);
        assertEquals(0, mapOfSpaceAndSellingFee1.size());

        proposalFeeRequestV2.getSpaces().forEach(
                space -> space.setSellingPrice(BigDecimal.valueOf(10000))
        );

        Map<String, BigDecimal> mapOfSpaceAndSellingFee2 = ProposalHelpers.getMapOfSpaceAndSellingFee(proposalFeeRequestV2);
        assertNotNull(mapOfSpaceAndSellingFee2);
        assertEquals(2, mapOfSpaceAndSellingFee2.size());
        assertEquals(BigDecimal.valueOf(10000).doubleValue(), mapOfSpaceAndSellingFee2.get("1").doubleValue());
        assertEquals(BigDecimal.valueOf(10000).doubleValue(), mapOfSpaceAndSellingFee2.get("2").doubleValue());
    }

    @Test
    void testGetMapOfSpaceAndRetailFee(){
        Map<String, BigDecimal> spaceAndRetailFee = ProposalHelpers.getMapOfSpaceAndRetailFee(proposalFeeRequest);
        assertNotNull(spaceAndRetailFee);
        assertEquals(2, spaceAndRetailFee.size());
        assertEquals(BigDecimal.valueOf(40000).doubleValue(), spaceAndRetailFee.get("1").doubleValue());
        assertEquals(BigDecimal.valueOf(5000).doubleValue(), spaceAndRetailFee.get("2").doubleValue());

        Map<String, BigDecimal> spaceAndRetailFeeV2 = ProposalHelpers.getMapOfSpaceAndRetailFee(proposalFeeRequestV2);
        assertNotNull(spaceAndRetailFeeV2);
        assertEquals(2, spaceAndRetailFee.size());
        assertEquals(BigDecimal.valueOf(40000).doubleValue(), spaceAndRetailFee.get("1").doubleValue());
        assertEquals(BigDecimal.valueOf(5000).doubleValue(), spaceAndRetailFee.get("2").doubleValue());
    }
}
