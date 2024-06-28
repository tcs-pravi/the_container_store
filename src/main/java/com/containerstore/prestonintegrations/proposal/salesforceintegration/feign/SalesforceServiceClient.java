package com.containerstore.prestonintegrations.proposal.salesforceintegration.feign;

import com.containerstore.prestonintegrations.proposal.models.SalesforceSaveProposalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        value = "salesforceService",
        url = "${feign.client.config.salesforceService.baseUrl}")
public interface SalesforceServiceClient {

    @PostMapping(value = "${feign.client.config.salesforceService.save-proposal-fee}",
            headers = {"Content-Type=application/json"})
    void saveProposal(SalesforceSaveProposalRequest saveProposalRequest);

    @GetMapping(value = "${feign.client.config.salesforceService.cases}",
            headers = {"Content-Type=application/json"})
    List<ValidateSpaceResponse> validateSpaces(@RequestParam("oppId") String oppId);


    @PostMapping(value = "${feign.client.config.salesforceService.upload-pdf}"
            , consumes = MediaType.APPLICATION_PDF_VALUE
            ,produces = MediaType.APPLICATION_JSON_VALUE)
    void uploadPdf(@RequestBody byte[] pdfBytes, @PathVariable String id,@PathVariable String fileName);
}
