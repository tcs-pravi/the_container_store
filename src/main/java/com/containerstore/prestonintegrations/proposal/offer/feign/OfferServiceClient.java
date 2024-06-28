package com.containerstore.prestonintegrations.proposal.offer.feign;

import com.containerstore.offer.domain.ImmutablePresentedOfferRequest;
import com.containerstore.offer.domain.OfferOrder;
import com.containerstore.offer.domain.OfferResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        value = "offerService",
        url = "${feign.client.config.offerService.baseUrl}",
        fallback = OfferServiceFallback.class)
public interface OfferServiceClient {
    @PostMapping(value = "${feign.client.config.offerService.get-presented-offer}",
            headers = {"Content-Type=application/json"})
    OfferResult getPresentedOffer(ImmutablePresentedOfferRequest presentedOfferRequest);

    @PostMapping(value = "${feign.client.config.offerService.apply-offer}",
            headers = {"Content-Type=application/json"})
    OfferOrder apply(OfferOrder offerOrder);
}
