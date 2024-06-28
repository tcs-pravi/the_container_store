package com.containerstore.prestonintegrations.proposal.tax.feign;

import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxTransactionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        value = "enterpriseTaxService",
        url = "${feign.client.config.enterpriseTaxService.baseUrl}")
@Retry(name = "enterpriseTaxService")
public interface EnterpriseTaxServiceClient {
    @PostMapping(value = "${feign.client.config.enterpriseTaxService.calculate-tax-url}",
            headers = {"Content-Type=application/json"})
    JsonNode calculateTax(TaxTransactionDTO taxTransactionDTO);
}
