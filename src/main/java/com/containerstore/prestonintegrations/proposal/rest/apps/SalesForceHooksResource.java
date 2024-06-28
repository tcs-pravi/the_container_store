package com.containerstore.prestonintegrations.proposal.rest.apps;

import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceMileageRequest;
import com.containerstore.prestonintegrations.proposal.salesforce.models.SalesForceTearOutRequest;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.mileage.SalesforceMileageService;
import com.containerstore.prestonintegrations.proposal.webhook.salesforce.tearout.SalesforceTearOutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.containerstore.prestonintegrations.proposal.salesforce.hooks.WebhooksApi;

@RestController
@RequestMapping("/apps/salesforce/api/v1")
public class SalesForceHooksResource implements WebhooksApi {

    private final SalesforceMileageService salesforceMileageService;
    private final SalesforceTearOutService salesforceTearOutService;

    public SalesForceHooksResource(SalesforceMileageService salesforceMileageService, SalesforceTearOutService salesforceTearOutService) {
        this.salesforceMileageService = salesforceMileageService;
        this.salesforceTearOutService = salesforceTearOutService;
    }

    @Override
    public ResponseEntity<Void> subscribeMileageInfoFromSalesForce(SalesForceMileageRequest salesForceMileageRequest) {
        this.salesforceMileageService.handleRequest(salesForceMileageRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> subscribeTearOutInfoFromSalesForce(SalesForceTearOutRequest salesForceTearOutRequest) {
        this.salesforceTearOutService.handleRequest(salesForceTearOutRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
