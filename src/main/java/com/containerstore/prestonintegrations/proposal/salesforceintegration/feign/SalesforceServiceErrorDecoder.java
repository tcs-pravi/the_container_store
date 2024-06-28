package com.containerstore.prestonintegrations.proposal.salesforceintegration.feign;

import com.containerstore.prestonintegrations.proposal.salesforceintegration.exception.OpportunityNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class SalesforceServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new OpportunityNotFoundException("Opportunity id not found");
        }
        return errorDecoder.decode(methodKey,response);
    }
}
