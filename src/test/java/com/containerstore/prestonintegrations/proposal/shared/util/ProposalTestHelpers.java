package com.containerstore.prestonintegrations.proposal.shared.util;

import com.containerstore.common.base.util.StringUtils;
import com.containerstore.prestonintegrations.proposal.models.CustomerAddress;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequest;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;

import java.math.BigDecimal;
import java.util.*;

public class ProposalTestHelpers {

	public static ProposalFeeRequest getProposalRequest(String stateName, Map<String, BigDecimal> spaces) {
		List<com.containerstore.prestonintegrations.proposal.models.Space> spaceObjects = new ArrayList<>();
		for (Map.Entry<String, BigDecimal> entry : spaces.entrySet()) {
			var space = new com.containerstore.prestonintegrations.proposal.models.Space();
			space.setRetailPrice(entry.getValue());
			space.setSpaceId(entry.getKey());
			spaceObjects.add(space);
		}
		com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestCustomerAddress address = null;
		if (StringUtils.isNotBlank(stateName)) {
			address = new
                    com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestCustomerAddress();
            address.setState(stateName);
		}
		return new ProposalFeeRequest("opp_id", address, spaceObjects);
	}

	public static com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse getProposalResponse() {
		var proposalFeeResponse = new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponse();
		proposalFeeResponse.setTaxableFees(new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFees());
        proposalFeeResponse.setNonTaxableFee(new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseNonTaxableFee());
		return proposalFeeResponse;
	}
    public static ProposalFeeRequestV2 getProposalRequestV2(String stateName, Map<String, BigDecimal> spaces) {
        List<com.containerstore.prestonintegrations.proposal.models.Space> spaceObjects = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : spaces.entrySet()) {
            var space = new com.containerstore.prestonintegrations.proposal.models.Space();
            space.setRetailPrice(entry.getValue());
            space.setSpaceId(entry.getKey());
            spaceObjects.add(space);
        }
        CustomerAddress customerAddress = null;
        if (StringUtils.isNotBlank(stateName)) {
            customerAddress = new CustomerAddress();
            customerAddress.setState(stateName);
        }
        return new ProposalFeeRequestV2("opp_id", customerAddress, spaceObjects);
    }

    public static ProposalFeeResponseV2 getProposalResponseV2() {
        ProposalFeeResponseV2 proposalFeeResponse = new ProposalFeeResponseV2();
        proposalFeeResponse.setFees(new com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseTaxableFees());
        return proposalFeeResponse;
    }
}
