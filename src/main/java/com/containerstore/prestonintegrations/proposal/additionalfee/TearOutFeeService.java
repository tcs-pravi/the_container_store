package com.containerstore.prestonintegrations.proposal.additionalfee;

import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFeesLineFeesInner;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.util.ProposalHelpers;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.FeeType.ADDITIONAL_SERVICES_DEMOLITION;

@RequiredArgsConstructor
@Slf4j
@Service
public class TearOutFeeService {

    private final SalesforceTearOutService salesforceTearOutService;

    public void getTearOutFee(ProposalFeeRequestV2 proposalFeeRequest, ProposalFeeResponseV2 proposalFeeResponse) {
        Collection<SalesforceTearOutEntity> tearOutEntities = salesforceTearOutService.getWebhookPersistedDataByKeyAndApp(proposalFeeRequest.getOpportunityId(), WebHookConsumers.SALESFORCE);

        if(tearOutEntities.isEmpty()) {
            log.info("No tear out information received from salesforce for opportunity id: {}", proposalFeeRequest.getOpportunityId());
            return;
        }

        Map<String, BigDecimal> spaceIdAndTearOutFeeMap = tearOutEntities.stream().collect(Collectors.toMap(SalesforceTearOutEntity::getSpaceId, SalesforceTearOutEntity::getTearOutFee));
        List<BigDecimal> fee = new ArrayList<>();

        this.buildTearOutFeePerSpace(proposalFeeResponse, spaceIdAndTearOutFeeMap, fee, proposalFeeRequest.getSpaces());

        var tearOutHeaderFee = ProposalHelpers
                .buildHeaderFees(fee.stream().reduce(BigDecimal.ZERO,BigDecimal::add), ADDITIONAL_SERVICES_DEMOLITION.name(),"Total Tear out fee for the proposal");

        proposalFeeResponse.getFees().addHeaderFeesItem(tearOutHeaderFee);
    }


	private void buildTearOutFeePerSpace(ProposalFeeResponseV2 proposalFeeResponse, Map<String, BigDecimal> spaceIdAndTearOutFeeMap, List<BigDecimal> fee,
                                         List<com.containerstore.prestonintegrations.proposal.models.Space> spaces) {
		spaces.forEach(space -> {
			if (spaceIdAndTearOutFeeMap.containsKey(space.getSpaceId())) {
				var lineFeesInner = new ProposalFeeResponseTaxableFeesLineFeesInner();
				lineFeesInner.setSpaceId(space.getSpaceId());
				lineFeesInner.setName(ADDITIONAL_SERVICES_DEMOLITION.name());
				lineFeesInner.setDescription(
						"Demolition charges provided by salesforce for space %s".formatted(space.getSpaceId()));
				lineFeesInner.setFee(spaceIdAndTearOutFeeMap.get(space.getSpaceId()).setScale(2, RoundingMode.HALF_EVEN));
				proposalFeeResponse.getFees().addLineFeesItem(lineFeesInner);
				fee.add(spaceIdAndTearOutFeeMap.get(space.getSpaceId()));
			}
		});
	}
}
