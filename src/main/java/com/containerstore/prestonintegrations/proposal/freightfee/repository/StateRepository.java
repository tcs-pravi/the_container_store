package com.containerstore.prestonintegrations.proposal.freightfee.repository;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {

    @Cacheable(value = "state",key = "#stateAbbreviation", unless="#result == null")
	Optional<State> findStateByStateAbbreviation(String stateAbbreviation);

}
