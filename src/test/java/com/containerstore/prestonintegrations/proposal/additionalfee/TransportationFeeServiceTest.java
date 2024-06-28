package com.containerstore.prestonintegrations.proposal.additionalfee;

import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesHeaderFeesInner;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstants;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsService;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType.ADDITIONAL_SERVICES_FEE;
import static com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType.ADDITIONAL_SERVICES_TRANSPORTATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransportationFeeServiceTest {

	@Mock
	ProposalConstantsService proposalConstantsService;

	@InjectMocks
	TransportationFeeService service;

    private  com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2 request;
    private ProposalFeeResponseV2 response;
    private SalesforceMileageEntity mileageEntity;

    private ProposalFee proposalFee;

	@BeforeEach
	public void setup() {
        Map<String, BigDecimal> spaces = new HashMap<>();
        spaces.put("1", BigDecimal.valueOf(20000));
        spaces.put("2", BigDecimal.valueOf(20000));

        request = ProposalTestHelpers.getProposalRequestV2("state", spaces);

        response = ProposalTestHelpers.getProposalResponseV2();
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(4000),
                FeeType.FREIGHT_FEE.name(), "Calculated freight fee"));
        response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(BigDecimal.valueOf(16600),
                FeeType.INSTALLATION_FEE.name(), "Calculated installation fee"));

        proposalFee = new ProposalFee();
        proposalFee.setFreightFeeBasedOnRetailPrice(BigDecimal.valueOf(4000));
        proposalFee.setInstallationFeeBasedOnRetailPrice(BigDecimal.valueOf(16600));

        mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setMiles(BigDecimal.valueOf(50));
        mileageEntity.setDurationValue(BigDecimal.ONE);
        mileageEntity.setChronoUnit(ChronoUnit.HOURS);

        loadConstants();
	}

	@ParameterizedTest
	@MethodSource("additionalFeeCalculationInputs")
    @DisplayName("Test for calculating additional service fees mileage, hotel and per-diem fees")
	public void testAdditionalFeeCalculation(Map<String, BigDecimal> spaces, BigDecimal mileage, BigDecimal duration,
			BigDecimal expectedHotelFee, BigDecimal expectedPerDiemFee, BigDecimal expectedMileageFee, String chronoUnit) {

        //mileage fee
        SalesforceMileageEntity mileageEntity = getMileageEntity(mileage, duration, chronoUnit);
		service.getMileageFee("opp_id", response, mileageEntity);

        if(!expectedMileageFee.equals(BigDecimal.ZERO)) {
            List<ProposalFeeResponseTaxableFeesHeaderFeesInner> additionalServiceTransportationFees = getAppropriateFee(response.getFees().getHeaderFees(), ADDITIONAL_SERVICES_TRANSPORTATION);
            BigDecimal mileageFee = additionalServiceTransportationFees.get(0).getFee();

            assertEquals(expectedMileageFee.doubleValue(), mileageFee.doubleValue());
            assertNotNull(response.getFees());
            assertNotNull(response.getFees().getHeaderFees());
            assertNotNull(response.getFees().getHeaderFees().get(0).getName());
            assertNotNull(response.getFees().getHeaderFees().get(0).getDescription());
        }

        //hotel fee
        ProposalFeeRequestV2 request = ProposalTestHelpers.getProposalRequestV2(null, spaces);

        service.getHotelAndPerDiemFee(request, response, mileageEntity, proposalFee);

        List<ProposalFeeResponseTaxableFeesHeaderFeesInner> additionalServiceHotelFee = getAppropriateFee(response.getFees().getHeaderFees(), ADDITIONAL_SERVICES_FEE);
        BigDecimal additionalServiceFee = additionalServiceHotelFee.get(0).getFee();

		assertEquals(expectedHotelFee.add(expectedPerDiemFee).doubleValue(), additionalServiceFee.doubleValue());

        //freight and installation fee
        assertEquals(FeeType.FREIGHT_FEE.name(), response.getFees().getHeaderFees().get(0).getName());
        assertEquals(BigDecimal.valueOf(4000).doubleValue(), response.getFees().getHeaderFees().get(0).getFee().doubleValue());

        assertEquals(FeeType.INSTALLATION_FEE.name(), response.getFees().getHeaderFees().get(1).getName());
        assertEquals(BigDecimal.valueOf(16600).doubleValue(), response.getFees().getHeaderFees().get(1).getFee().doubleValue());
	}

	@Test
    @DisplayName("Test for calculating additional service fees with invalid input data")
	public void testInvalidData() {
		assertThrows(Exception.class, () -> service.getMileageFee("opp_id", response, null));
	}

    private SalesforceMileageEntity getMileageEntity(BigDecimal mileage, BigDecimal duration,String chronoUnit) {
        SalesforceMileageEntity mileageEntity = new SalesforceMileageEntity();
        mileageEntity.setMiles(mileage);
        mileageEntity.setDurationValue(duration);
        mileageEntity.setChronoUnit(ChronoUnit.valueOf(chronoUnit));
        return mileageEntity;
    }

	private static Stream<Arguments> additionalFeeCalculationInputs() {
		Map<String, BigDecimal> space1 = new HashMap<>();
		space1.put("1", BigDecimal.valueOf(40000));
		Map<String, BigDecimal> space2 = new HashMap<>();
		space2.put("1", BigDecimal.valueOf(20000));
		space2.put("2", BigDecimal.valueOf(20000));
		return Stream.of(
				Arguments.of(space1, BigDecimal.valueOf(30), BigDecimal.valueOf(3), BigDecimal.valueOf(1050.00),
						BigDecimal.valueOf(490.00), BigDecimal.valueOf(17.50), "HOURS"),
				Arguments.of(space2, BigDecimal.valueOf(30), BigDecimal.valueOf(1), BigDecimal.valueOf(1050.00),
						BigDecimal.valueOf(490.00), BigDecimal.valueOf(17.50), "DAYS"),
                Arguments.of(space2, BigDecimal.valueOf(25), BigDecimal.valueOf(120), BigDecimal.valueOf(1050.00),
                        BigDecimal.valueOf(490.00), BigDecimal.ZERO, "MINUTES"),
                Arguments.of(space2, BigDecimal.valueOf(20), BigDecimal.valueOf(7200), BigDecimal.valueOf(1050.00),
                        BigDecimal.valueOf(490.00), BigDecimal.ZERO,"SECONDS"));
	}

	private void loadConstants() {
		ProposalConstants averageHotelFee = new ProposalConstants();
		averageHotelFee.setValue(BigDecimal.valueOf(150));
		when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.AVERAGE_HOTEL_FEE))
                .thenReturn(averageHotelFee);
		ProposalConstants perDiemPerInstaller = new ProposalConstants();
		perDiemPerInstaller.setValue(BigDecimal.valueOf(35));
		when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.AVERAGE_PER_DIEM_FEE))
				.thenReturn(perDiemPerInstaller);
		ProposalConstants noOfInstallers = new ProposalConstants();
		noOfInstallers.setValue(BigDecimal.valueOf(2));
		when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.NO_OF_INSTALLERS))
				.thenReturn(noOfInstallers);
		ProposalConstants threshold = new ProposalConstants();
		threshold.setValue(BigDecimal.valueOf(9000));
		when(proposalConstantsService
				.getProposalConstants(ProposalConstantKeys.THRESHOLD_AMOUNT_TO_CALCULATE_INSTALL_DAYS))
				.thenReturn(threshold);
		ProposalConstants minimumDriveTime = new ProposalConstants();
		minimumDriveTime.setValue(BigDecimal.valueOf(1.5));
		when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_DRIVE_TIME))
				.thenReturn(minimumDriveTime);
		ProposalConstants minimumMileage = new ProposalConstants();
		minimumMileage.setValue(BigDecimal.valueOf(25));
		when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_MILEAGE))
				.thenReturn(minimumMileage);
		ProposalConstants costPerMile = new ProposalConstants();
		costPerMile.setValue(BigDecimal.valueOf(3.5));
		when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.COST_PER_MILE)).thenReturn(costPerMile);
	}

    @Test
    void getHotelAndPerDiemFee(){
        var mileage_1_minute = getMileageEntity(BigDecimal.TEN,BigDecimal.ONE, ChronoUnit.MINUTES.name());
        var mileage_1_hour = getMileageEntity(BigDecimal.TEN,BigDecimal.ONE, ChronoUnit.HOURS.name());
        var mileage_1_day = getMileageEntity(BigDecimal.TEN,BigDecimal.ONE, ChronoUnit.DAYS.name());
        var seconds_60 =service.getDriveTimeInSeconds(mileage_1_minute);
        var seconds_3600 =service.getDriveTimeInSeconds(mileage_1_hour);
        var seconds_86400 =service.getDriveTimeInSeconds(mileage_1_day);
        assertEquals(60, seconds_60.getSeconds());
        assertEquals(3600, seconds_3600.getSeconds());
        assertEquals(86400, seconds_86400.getSeconds());
    }

    @Test
    public void testMileageFeeCalculationWithLessThanAndExactMinimumMileage() {
        ProposalConstants minimumMileage = new ProposalConstants();
        minimumMileage.setValue(BigDecimal.valueOf(100));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_MILEAGE))
                .thenReturn(minimumMileage);

        service.getMileageFee("oppId", response, mileageEntity);

        List<ProposalFeeResponseTaxableFeesHeaderFeesInner> additionalMileageFeesForMinMileage = getAppropriateFee(response.getFees().getHeaderFees(), ADDITIONAL_SERVICES_TRANSPORTATION);
        assertEquals(0, additionalMileageFeesForMinMileage.size());


        ProposalConstants exactMileage = new ProposalConstants();
        exactMileage.setValue(BigDecimal.valueOf(50));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_MILEAGE))
                .thenReturn(exactMileage);

        service.getMileageFee("oppId", response, mileageEntity);

        List<ProposalFeeResponseTaxableFeesHeaderFeesInner> additionalMileageFeesForExactMileage = getAppropriateFee(response.getFees().getHeaderFees(), ADDITIONAL_SERVICES_TRANSPORTATION);
        assertEquals(0, additionalMileageFeesForExactMileage.size());
    }

    @Test
    public void testBuildHotelAndPerDiemFeeWithMoreThanOneAndOneDayInstallationAndMinDriveTime() {
        ProposalConstants thresholdForMoreThanOneDayInstallation = new ProposalConstants();
        thresholdForMoreThanOneDayInstallation.setValue(BigDecimal.valueOf(70000));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.THRESHOLD_AMOUNT_TO_CALCULATE_INSTALL_DAYS))
                .thenReturn(thresholdForMoreThanOneDayInstallation);

        service.getHotelAndPerDiemFee(request, response, mileageEntity, proposalFee);

        List<ProposalFeeResponseTaxableFeesHeaderFeesInner> additionalFeesForMoreThanOneDayInstallation = getAppropriateFee(response.getFees().getHeaderFees(), ADDITIONAL_SERVICES_FEE);
        assertEquals(0, additionalFeesForMoreThanOneDayInstallation.size());


        ProposalConstants thresholdForOneDayInstallation = new ProposalConstants();
        thresholdForOneDayInstallation.setValue(BigDecimal.valueOf(60600));
        when(proposalConstantsService.getProposalConstants(ProposalConstantKeys.THRESHOLD_AMOUNT_TO_CALCULATE_INSTALL_DAYS))
                .thenReturn(thresholdForOneDayInstallation);

        service.getHotelAndPerDiemFee(request, response, mileageEntity, proposalFee);

        List<ProposalFeeResponseTaxableFeesHeaderFeesInner> additionalFeesForOneDayInstallation = getAppropriateFee(response.getFees().getHeaderFees(), ADDITIONAL_SERVICES_FEE);
        assertEquals(0, additionalFeesForOneDayInstallation.size());
    }

    private static List<ProposalFeeResponseTaxableFeesHeaderFeesInner> getAppropriateFee(List<ProposalFeeResponseTaxableFeesHeaderFeesInner> headerFees, FeeType feeType){
        return headerFees.stream()
                .filter(fee -> fee.getName().equalsIgnoreCase(feeType.name()))
                .toList();
    }
}
