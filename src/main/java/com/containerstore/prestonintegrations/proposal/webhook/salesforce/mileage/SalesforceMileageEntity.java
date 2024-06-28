package com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage;

import com.containerstore.prestonintegrations.proposal.webhook.BaseWebHookEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "salesforce_mileage")
@Data
public class SalesforceMileageEntity extends BaseWebHookEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1172542237167153056L;

    private String opportunityId;

    private BigDecimal miles;

    @Column(name = "storeId")
    private  String installationStoreId;

    private BigDecimal durationValue;

    @Enumerated(EnumType.STRING)
    private ChronoUnit chronoUnit;

    public Duration getDuration() throws IllegalArgumentException{
        return Duration.of(this.durationValue.longValue(), chronoUnit);
    }
}
