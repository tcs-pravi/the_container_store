package com.containerstore.prestonintegrations.proposal.rest.internal;

import com.containerstore.prestonintegrations.proposal.rest.internal.dto.ResponseDTO;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.OpportunityNotFoundException;
import com.containerstore.prestonintegrations.proposal.salesforceintegration.feign.SalesforceServiceClient;
import com.containerstore.prestonintegrations.proposal.store.dto.PaginatedAPIResponse;
import com.containerstore.prestonintegrations.proposal.webhook.WebHookConsumers;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutEntity;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import com.containerstore.spring.module.rest.RestError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("java:S1452")
@Slf4j
@RestController
@RequestMapping("/api/v1/salesforce")
@RequiredArgsConstructor
public class SalesforceResource {

    private final SalesforceMileageService salesforceMileageService;

    private final SalesforceTearOutService salesforceTearOutService;

    private final SalesforceServiceClient salesforceServiceClient;

    @DeleteMapping("/mileage/delete/{opportunityId}")
    public ResponseEntity<ResponseDTO> deleteMileageFee(@PathVariable String opportunityId) {
        int numberOfRowsDeleted = salesforceMileageService.deleteEntry(opportunityId, WebHookConsumers.SALESFORCE);
        String response = "Deleted mileage entry for opportunityId: %s , Number of rows deleted %d".formatted(opportunityId, numberOfRowsDeleted);
        return ResponseEntity.ok().body(new ResponseDTO<>(HttpStatus.OK, 200, response, numberOfRowsDeleted));
    }

    @DeleteMapping("/tear-out/delete/{opportunityId}")
    public ResponseEntity<ResponseDTO> deleteTearOutFee(@PathVariable String opportunityId) {
        int numberOfRowsDeleted = salesforceTearOutService.deleteEntry(opportunityId, WebHookConsumers.SALESFORCE);
        String response = "Deleted tear-out entry for opportunityId: %s , Number of rows deleted %d".formatted(opportunityId, numberOfRowsDeleted);
        return ResponseEntity.ok().body(new ResponseDTO<>(HttpStatus.OK, 200, response, numberOfRowsDeleted));
    }

    @GetMapping("/list-mileage-fee")
    public ResponseEntity<PaginatedAPIResponse<SalesforceMileageEntity>> getAllMileageRecords(@RequestParam(value = "opportunity-id",required = false) String opportunityId,
                                                                                              @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                                                                              @RequestParam(value = "limit", defaultValue = "20", required = false) Integer limit) {
        Page<SalesforceMileageEntity> paginatedResponse = salesforceMileageService.getAllMileageFee(page, limit, opportunityId);
        return ResponseEntity.ok().body(new PaginatedAPIResponse<>(
                paginatedResponse.getPageable().getPageNumber(),
                paginatedResponse.getPageable().getPageSize(),
                paginatedResponse.getTotalPages(),
                paginatedResponse.getTotalElements(),
                paginatedResponse.getContent()
        ));
    }

    @GetMapping("/list-tearOut-fee")
    public ResponseEntity<PaginatedAPIResponse<SalesforceTearOutEntity>> getAllTearOutRecords(@RequestParam(value = "opportunity-id", required = false) String opportunityId,
                                                                                              @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                                                                              @RequestParam(value = "limit", defaultValue = "20", required = false) Integer limit) {
        Page<SalesforceTearOutEntity> paginatedResponse = salesforceTearOutService.getAllTearOutFee(page, limit, opportunityId);
        return ResponseEntity.ok().body(new PaginatedAPIResponse<>(
                paginatedResponse.getPageable().getPageNumber(),
                paginatedResponse.getPageable().getPageSize(),
                paginatedResponse.getTotalPages(),
                paginatedResponse.getTotalElements(),
                paginatedResponse.getContent()
        ));
    }

    @PostMapping(value = "/upload/{id}")
    public ResponseEntity<?> uploadPdf(@PathVariable String id,
                                       @RequestPart MultipartFile file,
                                       @RequestParam String filename) {
        try {
            salesforceServiceClient.uploadPdf(file.getBytes(), id, filename);
        } catch (OpportunityNotFoundException e) {
            log.error("Opportunity {} not found in SF backend", id);
            RestError restError = new RestError(HttpStatus.NOT_FOUND, e);
            return ResponseEntity.status(404).body(restError);
        } catch (Exception e) {
            log.error("Failed to upload pdf to salesforce backend", e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

}
