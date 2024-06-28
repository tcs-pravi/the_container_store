package com.containerstore.prestonintegrations.proposal.offer.feign;

import com.containerstore.common.base.validation.ValidationResult;
import com.containerstore.common.base.validation.ValidationSeverity;
import com.containerstore.offer.domain.ImmutablePresentedOfferRequest;
import com.containerstore.offer.domain.OfferOrder;
import com.containerstore.offer.domain.OfferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class OfferServiceFallback implements OfferServiceClient{
    @Override
    public OfferResult getPresentedOffer(ImmutablePresentedOfferRequest presentedOfferRequest) {
        OfferResult offerResult = new OfferResult();
        ValidationResult validationResult = new ValidationResult();
        validationResult.setSeverity(ValidationSeverity.ERROR);
        validationResult.setCode("500");
        validationResult.setMessage("Unexpected error encountered. Please retry.");

        offerResult.setValidationResults(Collections.singletonList(validationResult));
        return offerResult;
    }

    @Override
    public OfferOrder apply(OfferOrder offerOrder) {
        return new OfferOrder();
    }
}
