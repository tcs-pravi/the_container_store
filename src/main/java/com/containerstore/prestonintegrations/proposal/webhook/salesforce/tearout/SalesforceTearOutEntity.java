package com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout;

import com.containerstore.prestonintegrations.proposal.webhook.BaseWebHookEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "salesforce_tear_out")
@EqualsAndHashCode(callSuper = true)
@Data
public class SalesforceTearOutEntity extends BaseWebHookEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2839747701676551916L;

    private String opportunityId;

    private String spaceId;

    private BigDecimal tearOutFee;
}
