package com.containerstore.prestonintegrations.proposal.shared.proposalconstants;

import com.containerstore.prestonintegrations.proposal.shared.proposalconstants.enums.ProposalConstantKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProposalConstantsService {

    private final ProposalConstantsRepository proposalConstantsRepository;

    public ProposalConstantsService(ProposalConstantsRepository proposalConstantsRepository) {
        this.proposalConstantsRepository = proposalConstantsRepository;
    }

    public ProposalConstants getProposalConstants(ProposalConstantKeys proposalConstant) {
        log.debug("Getting proposal constant '{}' from database", proposalConstant.name());
        return proposalConstantsRepository
                .findById(proposalConstant.name())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve %s from the database"
                        .formatted(proposalConstant.name())));
    }
}
