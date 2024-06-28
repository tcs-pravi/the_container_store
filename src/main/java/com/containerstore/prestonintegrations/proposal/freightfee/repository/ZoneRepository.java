package com.containerstore.prestonintegrations.proposal.freightfee.repository;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ZoneRepository extends JpaRepository<Zone, UUID> {
}
