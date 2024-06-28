package com.containerstore.prestonintegrations.proposal.shared.proposalconstants;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProposalConstantsServiceTest {

	@InjectMocks
	ProposalConstantsService service;

	@Mock
	ProposalConstantsRepository repository;

	@Test
	public void getProposalConstantsTest() {
        when(repository.findById(ProposalConstantKeys.INSTALLATION_RATE.name())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
				() -> service.getProposalConstants(ProposalConstantKeys.INSTALLATION_RATE));

        ProposalConstants proposalConstant = new ProposalConstants();
        proposalConstant.setKey(ProposalConstantKeys.NO_OF_INSTALLERS.name());
        proposalConstant.setValue(BigDecimal.TEN);
        when(repository.findById(ProposalConstantKeys.NO_OF_INSTALLERS.name())).thenReturn(Optional.of(proposalConstant));

        ProposalConstants fetchedConstant = service.getProposalConstants(ProposalConstantKeys.NO_OF_INSTALLERS);
        assertNotNull(fetchedConstant);
        assertEquals(proposalConstant.getKey(), fetchedConstant.getKey());
        assertEquals(proposalConstant.getValue(), fetchedConstant.getValue());
    }

}
