package com.containerstore.prestonintegrations.proposal.tax;

import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.models.Space;
import com.containerstore.prestonintegrations.proposal.offer.dto.DiscountedLineItems;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.tax.dto.LocationDTO;
import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxLineItemDTO;
import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxTransactionDTO;
import com.containerstore.prestonintegrations.proposal.tax.enums.ProductType;
import com.containerstore.prestonintegrations.proposal.tax.enums.TaxAttributes;
import com.containerstore.prestonintegrations.proposal.tax.feign.EnterpriseTaxServiceClient;
import com.containerstore.prestonintegrations.proposal.tax.service.TaxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaxServiceTest {

    @Mock
    private EnterpriseTaxServiceClient enterpriseTaxServiceClient;

    @InjectMocks
    private TaxService taxService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taxService = new TaxService(enterpriseTaxServiceClient);
    }

    @Test
    public void testGetTax() throws Exception {

        var space1 = new Space();
        space1.setSpaceId("9c96c0e9-d7aa-4541-a473-8acd201ed365");
        space1.setRetailPrice(new BigDecimal("8000.12"));

        Map<String, BigDecimal> map1 = new HashMap<>();
        map1.put(space1.getSpaceId(), space1.getRetailPrice());

        com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request = ProposalTestHelpers.getProposalRequestV2("NY", map1);
        request.getSpaces().forEach(space -> space.setSellingPrice(null));
        request.getCustomerAddress().setAddress2(null);
        request.getCustomerAddress().setAddress1(null);

        ProposalFeeResponseV2 response = ProposalTestHelpers.getProposalResponseV2();

        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(4000),
                FeeType.FREIGHT_FEE.name(), "Calculated freight fee"));
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(16600),
                FeeType.INSTALLATION_FEE.name(), "Calculated installation fee"));

        var lineFees = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner();
        lineFees.setName(FeeType.INSTALLATION_FEE.name());
        lineFees.setDescription("Calculated installation fee");
        lineFees.setFee((BigDecimal.valueOf(2000).setScale(2, RoundingMode.HALF_EVEN)));
        lineFees.setSpaceId(space1.getSpaceId());
        response.getFees().addLineFeesItem(lineFees);

        List<DiscountedLineItems> discountedLineItemsList = new ArrayList<>();

        when(enterpriseTaxServiceClient.calculateTax(any(TaxTransactionDTO.class))).thenReturn(
                new ObjectMapper().readTree("{\n" +
                        "  \"totalTax\": 1460.24\n" +
                        "}")
        );

        taxService.getTax(request, response, discountedLineItemsList);

        ArgumentCaptor<TaxTransactionDTO> captor = ArgumentCaptor.forClass(TaxTransactionDTO.class);
        verify(enterpriseTaxServiceClient, times(1)).calculateTax(captor.capture());

        TaxTransactionDTO taxTransactionDTO = captor.getValue();
        LocationDTO locationDTO = taxTransactionDTO.getLineItems().get(0).getCustomerLocationDTO();
        assertNotNull(locationDTO);
        assertEquals(EMPTY, locationDTO.getStreetAddress());
        assertEquals(EMPTY, locationDTO.getStreetAddress2());
        assertEquals("", locationDTO.getCounty());
        assertEquals("USA", locationDTO.getCountry());
        Assertions.assertEquals(1460.24,response.getTax().get(0).getAmount().doubleValue());
    }

    @Test
    @DisplayName("Test for getTax() with additional services fee and discounts items with different selling price for spaces")
    void testGetTaxWithAdditionalServicesFee() throws JsonProcessingException {
        Map<String, BigDecimal> spaceMap = new HashMap<>();
        spaceMap.put("spaceId1", BigDecimal.valueOf(10000));
        spaceMap.put("spaceId2", BigDecimal.valueOf(10000));

        ProposalFeeRequestV2 request = ProposalTestHelpers.getProposalRequestV2("NY", spaceMap);
        request.getSpaces().forEach(space1 -> space1.setSellingPrice(BigDecimal.valueOf(9000)));
        request.getCustomerAddress().setAddress2("address 2");
        request.getCustomerAddress().setAddress1("address 1");
        request.getCustomerAddress().setCity("city");
        request.getCustomerAddress().setZipCode("11110");

        ProposalFeeResponseV2 response = ProposalTestHelpers.getProposalResponseV2();

        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(4000),
                FeeType.FREIGHT_FEE.name(), "Calculated freight fee"));
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(16600),
                FeeType.INSTALLATION_FEE.name(), "Calculated installation fee"));
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(2000),
                FeeType.ADDITIONAL_SERVICES_FEE.name(), "Calculated additional fee"));

        var lineFees = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner();
        lineFees.setName(FeeType.INSTALLATION_FEE.name());
        lineFees.setDescription("Calculated installation fee");
        lineFees.setFee((BigDecimal.valueOf(2000).setScale(2, RoundingMode.HALF_EVEN)));
        lineFees.setSpaceId("spaceId1");
        response.getFees().addLineFeesItem(lineFees);

        ProposalFeeResponseTaxableFeesLineFeesInner demolitionLineFees = new ProposalFeeResponseTaxableFeesLineFeesInner();
        demolitionLineFees.setName(FeeType.ADDITIONAL_SERVICES_DEMOLITION.name());
        demolitionLineFees.setDescription("Calculated installation fee");
        demolitionLineFees.setFee((BigDecimal.valueOf(2000).setScale(2, RoundingMode.HALF_EVEN)));
        demolitionLineFees.setSpaceId("spaceId1");

        response.getFees().addLineFeesItem(demolitionLineFees);

        when(enterpriseTaxServiceClient.calculateTax(any(TaxTransactionDTO.class))).thenReturn(
                new ObjectMapper().readTree("""
                        {
                          "totalTax": 1460.24
                        }""")
        );

        taxService.getTax(request, response, null);

        assertEquals(1460.24,response.getTax().get(0).getAmount().doubleValue());
        assertEquals(3, response.getFees().getHeaderFees().size());
        assertEquals(2, response.getFees().getLineFees().size());

        assertNotNull(response.getTax());
        assertEquals("Total Tax", response.getTax().get(0).getTaxName());
        assertEquals(BigDecimal.valueOf(1460.24), response.getTax().get(0).getAmount());
        assertFalse(response.getTax().get(0).getHasError());

        ArgumentCaptor<TaxTransactionDTO> captor = ArgumentCaptor.forClass(TaxTransactionDTO.class);
        verify(enterpriseTaxServiceClient, times(1)).calculateTax(captor.capture());

        TaxTransactionDTO taxTransactionDTO = captor.getValue();
        assertNotNull(taxTransactionDTO);
        assertNotNull(taxTransactionDTO.getTaxDate());
        assertNotNull(taxTransactionDTO.getTransactionId());
        assertEquals("opp_id", taxTransactionDTO.getOrderId());
        assertNotNull(taxTransactionDTO.getOrderDate());
        assertEquals(Integer.valueOf(TaxAttributes.RING_STORE.value()), taxTransactionDTO.getRingStore());
        assertEquals(TaxAttributes.SOURCE_SYSTEM.value(), taxTransactionDTO.getSourceSystem());

        List<TaxLineItemDTO> lineItems = taxTransactionDTO.getLineItems();
        assertNotNull(lineItems);
        assertEquals(6, lineItems.size());

        List<TaxLineItemDTO> installTaxLineItem = lineItems.stream().filter(o -> o.getSku().equalsIgnoreCase(ProductType.CW_INSTALL.type())).toList();
        List<TaxLineItemDTO> productTaxLineItem = lineItems.stream().filter(o -> o.getSku().equalsIgnoreCase(ProductType.CW_PRODUCT.type())).toList();
        List<TaxLineItemDTO> demoTaxLineItem = lineItems.stream().filter(o -> o.getSku().equalsIgnoreCase(ProductType.CW_DEMO.type())).toList();

        assertEquals(2, installTaxLineItem.size());
        assertEquals(2, productTaxLineItem.size());
        assertEquals(2, demoTaxLineItem.size());

        lineItems.forEach(taxLineItemDTO -> {
                    assertNotNull(taxLineItemDTO.getLineItemId());
                    assertNotNull(taxLineItemDTO.getSku());
                    assertNotNull(taxLineItemDTO.getUnitPrice());
                    assertEquals(Integer.parseInt(TaxAttributes.ADMIN_ORIGIN_TAX_AREA_ID.value()), taxLineItemDTO.getAdminOriginTaxAreaId());
                    assertEquals(Integer.parseInt(TaxAttributes.PHYSICAL_ORIGIN_TAX_AREA_ID.value()), taxLineItemDTO.getPhysicalOriginTaxAreaId());
                    assertTrue(taxLineItemDTO.getConstructionItem());
                    assertEquals(BigDecimal.ONE, taxLineItemDTO.getQuantity());
                    assertEquals(Integer.parseInt(TaxAttributes.DEPARTMENT_CODE.value()), taxLineItemDTO.getDepartmentCode());
                    assertEquals(TaxAttributes.DELIVER.value(), taxLineItemDTO.getFulfillmentType());
                    assertEquals(TaxAttributes.LOCATION_CODE.value(), taxLineItemDTO.getLocationCode());

                    LocationDTO locationDTO = taxLineItemDTO.getCustomerLocationDTO();
                    assertNotNull(locationDTO);
                    assertEquals(request.getCustomerAddress().getAddress1(), locationDTO.getStreetAddress());
                    assertEquals(request.getCustomerAddress().getAddress2(), locationDTO.getStreetAddress2());
                    assertEquals(request.getCustomerAddress().getCity(), locationDTO.getCity());
                    assertEquals("", locationDTO.getCounty());
                    assertEquals(request.getCustomerAddress().getState(), locationDTO.getState());
                    assertEquals("USA", locationDTO.getCountry());
                    assertEquals(request.getCustomerAddress().getZipCode(), locationDTO.getZipCode());
                }
        );
    }
}
