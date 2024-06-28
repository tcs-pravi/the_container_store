package com.containerstore.prestonintegrations.proposal.offer.feign;

import com.containerstore.offer.exception.InvalidOfferCodeException;
import com.containerstore.spring.module.rest.RestError;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("all")
@Component
public class OfferServiceErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        RestError message = null;
        try (InputStream bodyIs = response.body()
                .asInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.readValue(bodyIs, RestError.class);
        } catch (IOException e) {
            return new Exception(e.getMessage());
        }
        if (response.status() == 400) {
            return new InvalidOfferCodeException(message.getMessage());
        }
        return new Exception("Exception while getting product details");
    }
}
