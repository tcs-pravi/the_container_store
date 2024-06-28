package com.containerstore.prestonintegrations.proposal.installationfee.service;

import com.containerstore.prestonintegrations.proposal.installationfee.InstallationFeeService;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstants;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsService;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InstallationFeeServiceTest {

	@Mock
	ProposalConstantsService proposalConstantsService;

    @Mock
    private StoreRepository storeRepository;

	@InjectMocks
	InstallationFeeService service;

	@ParameterizedTest
	@MethodSource("installationFeeCalculationInputs")
	public void testInstallationFeeCalculation(Map<String, BigDecimal> spaces, BigDecimal expected, BigDecimal installationRate, boolean isActive, boolean installationFeeEnabled) {
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(installationRate, isActive, installationFeeEnabled));

        loadConstants();

        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setInstallationStoreId("101");
		ProposalFeeResponse actualResponse = service.getInstallationFee(
				ProposalTestHelpers.getProposalRequest(null, spaces), ProposalTestHelpers.getProposalResponse(), mileageEntity);
        BigDecimal actualInstallationFee = actualResponse.getTaxableFees().getHeaderFees().get(0).getFee();
		assertEquals(actualInstallationFee.doubleValue(), expected.doubleValue());
		assertNotNull(actualResponse.getTaxableFees().getHeaderFees().get(0).getName());
		assertNotNull(actualResponse.getTaxableFees().getHeaderFees().get(0).getDescription());
		assertNotNull(actualResponse.getTaxableFees().getLineFees());
        assertNotNull(actualResponse.getTaxableFees().getLineFees().get(0));
		assertNotNull(actualResponse.getTaxableFees().getLineFees().get(0).getSpaceId());
		assertNotNull(actualResponse.getTaxableFees().getLineFees().get(0).getDescription());
		assertNotNull(actualResponse.getTaxableFees().getLineFees().get(0).getName());
		assertNotNull(actualResponse.getTaxableFees().getLineFees().get(0).getFee());
        assertNotNull(ProposalHelpers.getTotalRetailFeeFromProposal(ProposalTestHelpers.getProposalRequest(null, spaces)));
    }

    @ParameterizedTest
    @MethodSource("installationFeeCalculationInputsV2")
    public void testInstallationFeeCalculationV2(Map<String, BigDecimal> spaces, Map<String, BigDecimal> sellingPriceMap, BigDecimal expected, BigDecimal installationRate, boolean isActive, boolean installationFeeEnabled) {
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(installationRate, isActive, installationFeeEnabled));

        loadConstants();

        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setInstallationStoreId("101");
        var response = ProposalTestHelpers.getProposalResponseV2();
        var request= ProposalTestHelpers.getProposalRequestV2(null, spaces);
        if(sellingPriceMap != null && !sellingPriceMap.isEmpty()) {
            request.getSpaces().forEach(space -> {
                space.setSellingPrice(sellingPriceMap.get(space.getSpaceId()));
            });
        }
        var proposalFeeResult = service.getInstallationFeeV2(request, response, mileageEntity);
        BigDecimal actualInstallationFee = response.getFees().getHeaderFees().get(0).getFee();
        assertEquals(actualInstallationFee.doubleValue(), expected.doubleValue());
        assertNotNull(response.getFees().getHeaderFees().get(0).getName());
        assertNotNull(response.getFees().getHeaderFees().get(0).getDescription());
        assertNotNull(response.getFees().getLineFees());
        assertNotNull(response.getFees().getLineFees().get(0));
        assertNotNull(response.getFees().getLineFees().get(0).getSpaceId());
        assertNotNull(response.getFees().getLineFees().get(0).getDescription());
        assertNotNull(response.getFees().getLineFees().get(0).getName());
        assertNotNull(response.getFees().getLineFees().get(0).getFee());
        assertNotNull(ProposalHelpers.getTotalRetailFeeFromProposal(ProposalTestHelpers.getProposalRequest(null, spaces)));

        assertNotNull(proposalFeeResult);
        assertNotNull(proposalFeeResult.getInstallationFeeBasedOnSellingPrice());
        assertNotNull(proposalFeeResult.getDifferenceBetweenRetailAndSellingPrice());
        assertNotNull(proposalFeeResult.getDifferenceBetweenInstallFeeBasedOnRetailAndSellingPrice());
    }

	@ParameterizedTest
	@MethodSource("inputsForInvalidData")
	public void testInvalidData(Map<String, BigDecimal> spaces) {
        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setInstallationStoreId("101");
		assertThrows(Exception.class,
				() -> service.getInstallationFee(ProposalTestHelpers.getProposalRequest(null, spaces),
						ProposalTestHelpers.getProposalResponse(), mileageEntity));
	}

	private static Stream<Arguments> installationFeeCalculationInputs() {
		Map<String, BigDecimal> space1 = new HashMap<>();
		space1.put("1", BigDecimal.valueOf(30000));
		Map<String, BigDecimal> space2 = new HashMap<>();
		space2.put("1", BigDecimal.valueOf(10000));
		space2.put("2", BigDecimal.valueOf(20000));
		return Stream.of(Arguments.of(space1, BigDecimal.valueOf(12450), BigDecimal.valueOf(0.415), true, true),
				Arguments.of(space2, BigDecimal.valueOf(12450), BigDecimal.valueOf(0.415), true, true));
	}

    private static Stream<Arguments> installationFeeCalculationInputsV2() {
        Map<String, BigDecimal> space1 = new HashMap<>();
        space1.put("1", BigDecimal.valueOf(18000));
        Map<String, BigDecimal> space2 = new HashMap<>();
        space2.put("1", BigDecimal.valueOf(9000));
        space2.put("2", BigDecimal.valueOf(9000));
        Map<String, BigDecimal> sellingPrice1 = new HashMap<>();
        sellingPrice1.put("1", BigDecimal.valueOf(15000));
        Map<String, BigDecimal> sellingPrice2 = new HashMap<>();
        sellingPrice2.put("1", BigDecimal.valueOf(7500));
        sellingPrice2.put("2", BigDecimal.valueOf(7500));
        Map<String, BigDecimal> space3 = new HashMap<>();
        space3.put("1", BigDecimal.valueOf(30000));
        return Stream.of(Arguments.of(space1, sellingPrice1, BigDecimal.valueOf(6225), BigDecimal.valueOf(0.415), true, true),
                Arguments.of(space2, sellingPrice2, BigDecimal.valueOf(6225), BigDecimal.valueOf(0.415), true, true),
                Arguments.of(space3, new HashMap<>(), BigDecimal.valueOf(12450), BigDecimal.valueOf(0.415), true, true));
    }

	private static Stream<Arguments> inputsForInvalidData() {
		Map<String, BigDecimal> space1 = new HashMap<>();
		space1.put("1", BigDecimal.valueOf(30000));
		Map<String, BigDecimal> space2 = new HashMap<>();
		space2.put("1", BigDecimal.valueOf(-100));
		Map<String, BigDecimal> space3 = new HashMap<>();
		space3.put("1", BigDecimal.valueOf(0));
		return Stream.of(Arguments.of(space1), Arguments.of(space2), Arguments.of(space3));
	}

    private Optional<StoreEntity> getStore(BigDecimal installationRate, boolean isActive, boolean installationFeeEnabled) {
        StoreEntity store = new StoreEntity();
        store.setSalesforceStoreId("101");
        store.setActive(isActive);
        store.setInstallationRate(installationRate);
        store.setInstallationFeeEnabled(installationFeeEnabled);
        return Optional.of(store);
    }

    @DisplayName("Test for getInstallationRate() for store inactive and disabled fee")
    @ParameterizedTest
    @MethodSource("inActiveAndDisabledStoreData")
    void testForInstallationFeeWithInactiveAndDisabledFeeForStore_V1(BigDecimal installationRate, boolean isActive, boolean installationFeeEnabled,
                                                                     SalesforceMileageEntity mileageEntity, Map<String, BigDecimal> spaces)
    {
        loadConstants();
        when(storeRepository.findStoreBySalesforceStoreId("101"))
                .thenReturn(getStore(installationRate, isActive, installationFeeEnabled));

        ProposalFeeRequest proposalFeeRequest = ProposalTestHelpers.getProposalRequest("state", spaces);
        ProposalFeeResponse proposalFeeResponse = ProposalTestHelpers.getProposalResponse();

        service.getInstallationFee(proposalFeeRequest, proposalFeeResponse, mileageEntity);

        verify(proposalConstantsService, times(1)).getProposalConstants(ProposalConstantKeys.INSTALLATION_RATE);
    }

    @DisplayName("Test for getInstallationRateV2() for store inactive and disabled fee")
    @ParameterizedTest
    @MethodSource("inActiveAndDisabledStoreData")
    void testForInstallationFeeWithInactiveAndDisabledFeeForStore_V2(BigDecimal installationRate, boolean isActive, boolean installationFeeEnabled,
                                                                     SalesforceMileageEntity mileageEntity, Map<String, BigDecimal> spaces)
    {
        loadConstants();
        when(storeRepository.findStoreBySalesforceStoreId("101"))
                .thenReturn(getStore(installationRate, isActive, installationFeeEnabled));

        ProposalFeeRequestV2 proposalFeeRequestV2 = ProposalTestHelpers.getProposalRequestV2("state", spaces);
        ProposalFeeResponseV2 proposalFeeResponseV2 = ProposalTestHelpers.getProposalResponseV2();

        service.getInstallationFeeV2(proposalFeeRequestV2, proposalFeeResponseV2, mileageEntity);

        verify(proposalConstantsService).getProposalConstants(ProposalConstantKeys.INSTALLATION_RATE);
    }
    private void loadConstants() {
        ProposalConstants minInstallationFee = new ProposalConstants();
        minInstallationFee.setValue(BigDecimal.valueOf(1000));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_INSTALLATION_FEE))
                .thenReturn(minInstallationFee);
        ProposalConstants installationRateConstant = new ProposalConstants();
        installationRateConstant.setValue(BigDecimal.valueOf(0.415));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.INSTALLATION_RATE))
                .thenReturn(installationRateConstant);
    }
    private static Stream<Arguments> inActiveAndDisabledStoreData() {
        Map<String, BigDecimal> spaces = new HashMap<>();
        spaces.put("1", BigDecimal.valueOf(20000));
        spaces.put("2", BigDecimal.valueOf(20000));
        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setInstallationStoreId("101");
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(1000), false, false, mileageEntity, spaces),
                Arguments.of(BigDecimal.valueOf(1000), false, true, mileageEntity, spaces),
                Arguments.of(BigDecimal.valueOf(1000), true, false, mileageEntity, spaces)
        );
    }
}
