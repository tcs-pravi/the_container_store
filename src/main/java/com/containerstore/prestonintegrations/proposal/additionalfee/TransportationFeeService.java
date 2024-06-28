package com.containerstore.prestonintegrations.proposal.additionalfee;

import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.dto.ProposalFee;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.ProposalConstantsService;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType.ADDITIONAL_SERVICES_TRANSPORTATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportationFeeService {

    private final ProposalConstantsService proposalConstantsService;

	public void getMileageFee(String opportunityId, ProposalFeeResponseV2 proposalFeeResponse,
                              SalesforceMileageEntity mileageEntity) {

        BigDecimal mileage = mileageEntity.getMiles();

        BigDecimal minimumMileage = proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_MILEAGE)
                .getValue();

        if (mileage.compareTo(minimumMileage) > 0) {
            BigDecimal mileageFee = mileage.subtract(minimumMileage).multiply(
                    proposalConstantsService.getProposalConstants(ProposalConstantKeys.COST_PER_MILE).getValue());

            proposalFeeResponse.getFees().addHeaderFeesItem(
                    ProposalHelpers.buildHeaderFees(mileageFee, ADDITIONAL_SERVICES_TRANSPORTATION.name(),
                            "Calculated Mileage fee for opportunity id: %s".formatted(opportunityId)));
        }
    }

	public void getHotelAndPerDiemFee(ProposalFeeRequestV2 request, ProposalFeeResponseV2 response,
                                      SalesforceMileageEntity mileageEntity, ProposalFee proposalFee) {
        BigDecimal noOfDaysOfInstallation = this.getNoOfDaysOfInstallation(request, response, proposalFee);
        Duration driveTime = this.getDriveTimeInSeconds(mileageEntity);
        this.buildHotelAndPerDiemFee(request, response, noOfDaysOfInstallation, driveTime);
    }

	private void buildHotelAndPerDiemFee(ProposalFeeRequestV2 request, ProposalFeeResponseV2 response,
                                         BigDecimal noOfDaysOfInstallation, Duration driveTime) {
		if (noOfDaysOfInstallation.compareTo(BigDecimal.ONE) > 0 && driveTime.compareTo(
				Duration.of(proposalConstantsService.getProposalConstants(ProposalConstantKeys.MINIMUM_DRIVE_TIME)
						.getValue().longValue(), ChronoUnit.SECONDS)) > 0) {
			response.getFees().addHeaderFeesItem(ProposalHelpers.buildHeaderFees(
					this.calculateHotelFee(noOfDaysOfInstallation).add(this.calculatePerDiem(noOfDaysOfInstallation)),
					FeeType.ADDITIONAL_SERVICES_FEE.name(),
					"Calculated Additional services fee for opportunity id: %s".formatted(request.getOpportunityId())));
        }
    }

    @NotNull
    private BigDecimal getNoOfDaysOfInstallation(ProposalFeeRequestV2 request, ProposalFeeResponseV2 response, ProposalFee proposalFee) {
        BigDecimal retailPrice = ProposalHelpers.getTotalRetailFeeFromProposalV2(request).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal installationFee = proposalFee.getInstallationFeeBasedOnRetailPrice();
        BigDecimal freightFee = proposalFee.getFreightFeeBasedOnRetailPrice();

        return this.calculateNumberDaysForInstallation(retailPrice, installationFee,freightFee);
    }

    private BigDecimal calculateHotelFee(BigDecimal noOfDaysOfInstallation) {
        return noOfDaysOfInstallation.multiply(proposalConstantsService.getProposalConstants(ProposalConstantKeys.AVERAGE_HOTEL_FEE).getValue());
    }

    private BigDecimal calculatePerDiem(BigDecimal noOfDaysOfInstallation) {
        return noOfDaysOfInstallation
                .multiply(proposalConstantsService.getProposalConstants(ProposalConstantKeys.AVERAGE_PER_DIEM_FEE).getValue())
                .multiply(proposalConstantsService.getProposalConstants(ProposalConstantKeys.NO_OF_INSTALLERS).getValue());
    }

    private BigDecimal calculateNumberDaysForInstallation(BigDecimal totalRetailPrice, BigDecimal installationFee, BigDecimal freightFee) {
        return (totalRetailPrice.add(installationFee).add(freightFee))
                .divide(proposalConstantsService.getProposalConstants(ProposalConstantKeys.THRESHOLD_AMOUNT_TO_CALCULATE_INSTALL_DAYS).getValue(), MathContext.DECIMAL32)
                .setScale(0, RoundingMode.CEILING);
    }


	public Duration getDriveTimeInSeconds(SalesforceMileageEntity entity) {
		BigDecimal duration = entity.getDurationValue();
        return switch (entity.getChronoUnit()) {
            case DAYS -> Duration.of((long) duration.doubleValue() * 24 * 60 * 60, ChronoUnit.SECONDS);
            case HOURS -> Duration.of((long) duration.doubleValue() * 60 * 60, ChronoUnit.SECONDS);
            case MINUTES -> Duration.of((long) duration.doubleValue() * 60, ChronoUnit.SECONDS);
            default -> entity.getDuration();
        };
	}

}
