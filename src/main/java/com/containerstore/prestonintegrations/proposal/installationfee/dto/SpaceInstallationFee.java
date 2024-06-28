package com.containerstore.prestonintegrations.proposal.installationfee.dto;

import java.math.BigDecimal;

public record SpaceInstallationFee(String spaceId, BigDecimal retailPrice,BigDecimal InstallationFee) {
}
