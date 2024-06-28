package com.containerstore.prestonintegrations.proposal.shared.proposalconstants;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "proposal_constants")
@Entity
@Data
public class ProposalConstants implements Serializable {

    @Serial
    private static final long serialVersionUID = 5030462862228214540L;

    @Id
	@Column(name = "proposal_key")
	private String key;

	@Column(name = "proposal_value")
	private BigDecimal value;
}
