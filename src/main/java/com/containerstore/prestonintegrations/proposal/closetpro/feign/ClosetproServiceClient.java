package com.containerstore.prestonintegrations.proposal.closetpro.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        value = "closetproService",
        url = "${feign.client.config.closetproService.baseUrl}",configuration = ClosetproServiceFeignConfiguration.class)
public interface ClosetproServiceClient {

    @PostMapping(value = "${feign.client.config.closetproService.retrieve-pdf-by-propasal}",produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> downloadPdf(@RequestParam(value = "ID") String proposalId, @RequestBody String empty);
}
