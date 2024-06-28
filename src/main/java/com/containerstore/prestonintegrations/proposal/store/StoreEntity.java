package com.containerstore.prestonintegrations.proposal.store;

import com.containerstore.prestonintegrations.proposal.freightfee.entity.State;
import lombok.Data;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "stores")
@Entity
@Data
public class StoreEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 3209816533740532837L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "store_code")
    private String storeCode;

    @Column(name = "salesforce_store_id")
    private String salesforceStoreId;

    @Column(name = "city")
    private String city;

    @ManyToOne
    private State state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "adjustment_value")
    private BigDecimal adjustmentValue;

    @Column(name = "adjustment_type")
    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;

    @Column(name = "is_freight_fee_enabled")
    private boolean isFreightFeeEnabled;

    @Column(name = "installation_rate")
    private BigDecimal installationRate;

    @Column(name = "is_installation_fee_enabled")
    private boolean isInstallationFeeEnabled;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

}
