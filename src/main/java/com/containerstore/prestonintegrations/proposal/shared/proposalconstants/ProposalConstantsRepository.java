package com.containerstore.prestonintegrations.proposal.shared.proposalconstants;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProposalConstantsRepository extends JpaRepository<ProposalConstants, String> {

    @NotNull
    @Override
    @Cacheable(value = "proposalconstants",key = "#proposalKey", unless="#result == null")
    Optional<ProposalConstants> findById(@NotNull String proposalKey);
}
