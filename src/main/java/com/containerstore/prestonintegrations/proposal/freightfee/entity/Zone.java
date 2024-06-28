package com.containerstore.prestonintegrations.proposal.freightfee.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "zone")
@Entity
@Data
public class Zone implements Serializable {

    @Serial
    private static final long serialVersionUID = -470280729810140302L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "zone_number")
    private Integer zoneNumber;

    @Column(name = "cost_per_crate")
    private BigDecimal costPerCrate;
}
