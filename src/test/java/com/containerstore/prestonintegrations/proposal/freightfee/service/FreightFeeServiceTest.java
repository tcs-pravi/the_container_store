package com.containerstore.prestonintegrations.proposal.freightfee.service;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import com.containerstore.prestonintegrations.proposal.freightfee.entity.Zone;
import com.containerstore.prestonintegrations.proposal.freightfee.exception.StateNotFoundException;
import com.containerstore.prestonintegrations.proposal.freightfee.repository.StateRepository;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstants;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsRepository;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.exception.ProposalConstantNotFoundException;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.store.AdjustmentType;
import com.containerstore.prestonintegrations.proposal.store.StoreEntity;
import com.containerstore.prestonintegrations.proposal.store.StoreRepository;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FreightFeeServiceTest {

	@Mock
	ProposalConstantsRepository proposalConstantsRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private StoreRepository storeRepository;

	@InjectMocks
	FreightFeeService service;

	@ParameterizedTest
	@MethodSource("freightFeeCalculationInputs")
	public void testFreightFeeCalculation(String stateName, Map<String, BigDecimal> spaces, BigDecimal costPerCrate,
			BigDecimal expected, AdjustmentType adjustmentType, BigDecimal adjustmentValue, boolean freightEnabled) {
        Optional<State> state = getState(stateName, costPerCrate);
        when(stateRepository.findStateByStateAbbreviation(anyString())).thenReturn(state);
        when(storeRepository.findStoreBySalesforceStoreId(anyString())).thenReturn(getStore(adjustmentType, adjustmentValue, freightEnabled, state.get()));
        ProposalConstants constant = new ProposalConstants();
		constant.setValue(BigDecimal.valueOf(9999.99));
        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setInstallationStoreId("101");
		if (costPerCrate == null || costPerCrate.compareTo(BigDecimal.ZERO) == 0) {
			assertThrows(ProposalConstantNotFoundException.class,
					() -> service.getFreightFeeForProposal(ProposalTestHelpers.getProposalRequest(stateName, spaces),
							ProposalTestHelpers.getProposalResponse(), mileageEntity));
			return;
		}
        when(proposalConstantsRepository.findById(anyString())).thenReturn(Optional.of(constant));
        ProposalFeeResponse response = ProposalTestHelpers.getProposalResponse();
		service.getFreightFeeForProposal(
				ProposalTestHelpers.getProposalRequest(stateName, spaces), response, mileageEntity);
		BigDecimal actualFreightFee = response.getTaxableFees().getHeaderFees().get(0).getFee();
		assertEquals(actualFreightFee, expected);
		assertNotNull(response.getTaxableFees().getHeaderFees().get(0).getName());
		assertNotNull(response.getTaxableFees().getHeaderFees().get(0).getDescription());
	}

    private Optional<StoreEntity> getStore(AdjustmentType adjustmentType, BigDecimal adjustmentValue, boolean freightEnabled, State state) {
        StoreEntity store = new StoreEntity();
        store.setFreightFeeEnabled(freightEnabled);
        store.setAdjustmentType(adjustmentType);
        store.setAdjustmentValue(adjustmentValue);
        store.setSalesforceStoreId("101");
        store.setState(state);
        store.setActive(true);
        return Optional.of(store);
    }

    @ParameterizedTest
	@MethodSource("inputsForInvalidData")
	public void testInvalidData(String stateName, Map<String, BigDecimal> spaces) {
        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setInstallationStoreId("101");
			assertThrows(Exception.class,
					() -> service.getFreightFeeForProposal(ProposalTestHelpers.getProposalRequest(stateName, spaces),
							ProposalTestHelpers.getProposalResponse(), mileageEntity));
	}

	private Optional<State> getState(String stateName, BigDecimal costPerCrate) {
		Zone zone = null;
		if (costPerCrate != null) {
            zone = new Zone();
            zone.setZoneNumber(1);
            zone.setId(UUID.randomUUID());
            zone.setCostPerCrate(costPerCrate);
		}

		State stateObj;
		Optional<State> state = Optional.empty();
		if (stateName != null) {
			stateObj = new State();
            stateObj.setStateName(stateName);
            stateObj.setId(UUID.randomUUID());
            stateObj.setStateAbbreviation(stateName);
            stateObj.setZone(zone);
			state = Optional.of(stateObj);
		}
		return state;
	}

	private static Stream<Arguments> freightFeeCalculationInputs() {
		Map<String, BigDecimal> space1 = new HashMap<>();
		space1.put("1", BigDecimal.valueOf(30000));
		Map<String, BigDecimal> space2 = new HashMap<>();
		space2.put("1", BigDecimal.valueOf(10000));
		space2.put("2", BigDecimal.valueOf(20000));
		return Stream.of(Arguments.of("Mississippi", space1, BigDecimal.valueOf(1000), BigDecimal.valueOf(4800.00).setScale(2, RoundingMode.HALF_EVEN), AdjustmentType.AMOUNT, BigDecimal.valueOf(200), true),
				Arguments.of("Mississippi", space2, BigDecimal.valueOf(1000), BigDecimal.valueOf(3200.00).setScale(2, RoundingMode.HALF_EVEN), AdjustmentType.AMOUNT, BigDecimal.valueOf(-200), true),
                Arguments.of("Mississippi", space2, BigDecimal.valueOf(1000), BigDecimal.valueOf(4000.00).setScale(2, RoundingMode.HALF_EVEN), AdjustmentType.AMOUNT, BigDecimal.ZERO, false),
                Arguments.of("Mississippi", space2, BigDecimal.valueOf(1000), BigDecimal.valueOf(3600.00).setScale(2, RoundingMode.HALF_EVEN), AdjustmentType.PERCENTAGE, BigDecimal.valueOf(-10), true),
                Arguments.of("Mississippi", space2, BigDecimal.valueOf(1000), BigDecimal.valueOf(4400.00).setScale(2, RoundingMode.HALF_EVEN), AdjustmentType.PERCENTAGE, BigDecimal.valueOf(10), true),
                Arguments.of("Mississippi", space2, BigDecimal.ZERO, null, AdjustmentType.AMOUNT, BigDecimal.ZERO, false));
	}

	private static Stream<Arguments> inputsForInvalidData() {
		Map<String, BigDecimal> space1 = new HashMap<>();
		space1.put("1", BigDecimal.valueOf(30000));
		Map<String, BigDecimal> space2 = new HashMap<>();
		space2.put("1", BigDecimal.valueOf(-100));
		Map<String, BigDecimal> space3 = new HashMap<>();
		space3.put("1", BigDecimal.valueOf(0));
		return Stream.of(Arguments.of(null, space1), Arguments.of("  ", space1), Arguments.of("Mississippi", space1),
				Arguments.of("Mississippi", space2), Arguments.of("Mississippi", space3));
	}

    @Test
    @DisplayName("Test for getFreightFeeForProposal() and getFreightFeeForProposalV2() to throw StateNotFoundException")
    void testGetFreightFeeForProposal_ShouldThrowStateNotFoundException(){
        Map<String, BigDecimal> space = new HashMap<>();
        space.put("1", BigDecimal.valueOf(30000));

        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setMiles(BigDecimal.valueOf(50));
        mileageEntity.setDurationValue(BigDecimal.ONE);
        mileageEntity.setChronoUnit(ChronoUnit.HOURS);

        ProposalFee proposalFee = new ProposalFee();
        proposalFee.setInstallationFeeBasedOnRetailPrice(BigDecimal.valueOf(4000));
        proposalFee.setInstallationFeeBasedOnSellingPrice(BigDecimal.ZERO);
        proposalFee.setDifferenceBetweenRetailAndSellingPrice(BigDecimal.ZERO);
        proposalFee.setDifferenceBetweenInstallFeeBasedOnRetailAndSellingPrice(BigDecimal.valueOf(4000));
        proposalFee.setTotalSellingPriceInProposal(BigDecimal.ZERO);
        when(stateRepository.findStateByStateAbbreviation("XY")).thenReturn(Optional.empty());

        StateNotFoundException exception = assertThrows(StateNotFoundException.class, () -> service.getFreightFeeForProposal(
                ProposalTestHelpers.getProposalRequest("XY", space), ProposalTestHelpers.getProposalResponse(), mileageEntity));
        assertEquals("State 'XY' not found in database", exception.getMessage());

        StateNotFoundException exceptionV2 = assertThrows(StateNotFoundException.class, () -> service.getFreightFeeForProposalV2(
                ProposalTestHelpers.getProposalRequestV2("XY", space), ProposalTestHelpers.getProposalResponseV2(), mileageEntity, proposalFee));
        assertEquals("State 'XY' not found in database", exceptionV2.getMessage());
    }
}
