package com.containerstore.prestonintegrations.proposal.freightfee.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;


@Table(name = "state")
@Entity
@Data
public class State implements Serializable {

    @Serial
    private static final long serialVersionUID = 1577578091150969162L;

    @Id
	@GeneratedValue
	@Column(name = "id")
	private UUID id;

	@Column(name = "state_name")
	private String stateName;

	@Column(name = "abbreviation")
	private String stateAbbreviation;

	@ManyToOne
	private Zone zone;
}
