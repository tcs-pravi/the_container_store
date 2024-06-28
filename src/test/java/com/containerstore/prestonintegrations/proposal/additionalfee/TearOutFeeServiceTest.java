package com.containerstore.prestonintegrations.proposal.additionalfee;

import com.containerstore.prestonintegrations.proposal.models.ProposalFeeRequestV2;
import com.containerstore.prestonintegrations.proposal.models.ProposalFeeResponseV2;
import com.containerstore.prestonintegrations.proposal.shared.util.ProposalTestHelpers;
import com.containerstore.prestonintegrations.proposal.webhook.BaseWebHookEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TearOutFeeServiceTest {

	@InjectMocks
	TearOutFeeService service;

	@Mock
	SalesforceTearOutService salesforceTearOutService;

	@ParameterizedTest
	@MethodSource("tearOutFeeCalculationInputs")
	public void testTearOutFeeCalculation(Map<String, BigDecimal> spaces,
                                          Map<String, BigDecimal> spaceIdAndTearOutFeeMap,
                                          BigDecimal expectedTearOutFee) {

		Collection<BaseWebHookEntity> tearOutEntities = getTearOutEntity(spaceIdAndTearOutFeeMap);
		when(salesforceTearOutService.getWebhookPersistedDataByKeyAndApp(any(), any())).thenReturn(tearOutEntities);

        ProposalFeeRequestV2 request = ProposalTestHelpers.getProposalRequestV2(null, spaces);
        ProposalFeeResponseV2 response = ProposalTestHelpers.getProposalResponseV2();

        service.getTearOutFee(request, response);

		BigDecimal tearOutFee = response.getFees().getHeaderFees().get(0).getFee();
		assertEquals(tearOutFee.doubleValue(), expectedTearOutFee.doubleValue());
		assertNotNull(response.getFees().getHeaderFees().get(0).getName());
		assertNotNull(response.getFees().getHeaderFees().get(0).getDescription());
        assertNotNull(response.getFees().getLineFees());
        assertNotNull(response.getFees().getLineFees().get(0).getSpaceId());
        assertNotNull(response.getFees().getLineFees().get(0).getDescription());
        assertNotNull(response.getFees().getLineFees().get(0).getName());
        assertNotNull(response.getFees().getLineFees().get(0).getFee());
	}

    @Test
    public void testInvalidInput(){
        when(salesforceTearOutService.getWebhookPersistedDataByKeyAndApp(any(), any())).thenReturn(Collections.emptyList());

        Map<String, BigDecimal> spaces = new HashMap<>();
        spaces.put("1", BigDecimal.valueOf(4000));
        spaces.put("2", BigDecimal.valueOf(2000));
        ProposalFeeRequestV2 request = ProposalTestHelpers.getProposalRequestV2(null, spaces);
        ProposalFeeResponseV2 response = ProposalTestHelpers.getProposalResponseV2();

        service.getTearOutFee(request, response);

        assertNull(response.getFees().getHeaderFees());
    }

	private Collection<BaseWebHookEntity> getTearOutEntity(Map<String, BigDecimal> spaceIdAndTearOutFeeMap) {
		Collection<BaseWebHookEntity> list = new ArrayList<>();
		spaceIdAndTearOutFeeMap.forEach((key, value) -> {
            SalesforceTearOutEntity entity = new SalesforceTearOutEntity();
            entity.setSpaceId(key);
            entity.setTearOutFee(value);
            list.add(entity);
        });

		return list;
	}

	public static Stream<Arguments> tearOutFeeCalculationInputs() {
		Map<String, BigDecimal> tearOutMap1 = new HashMap<>();
		tearOutMap1.put("1", BigDecimal.valueOf(100));
        Map<String, BigDecimal> space1 = new HashMap<>();
        space1.put("1", BigDecimal.valueOf(4000));
        Map<String, BigDecimal> tearOutMap2 = new HashMap<>();
        tearOutMap2.put("1", BigDecimal.valueOf(100));
        Map<String, BigDecimal> space2 = new HashMap<>();
        space2.put("1", BigDecimal.valueOf(4000));
        space2.put("2", BigDecimal.valueOf(2000));
		return Stream.of(Arguments.of(space1, tearOutMap1, BigDecimal.valueOf(100.00)),
                Arguments.of(space2, tearOutMap2, BigDecimal.valueOf(100.00)));
	}
}
