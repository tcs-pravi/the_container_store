package com.containerstore.prestonintegrations.proposal.wiremockintegrations;

import com.containerstore.prestonintegrations.proposal.tax.dto.feign.TaxTransactionDTO;
import com.containerstore.prestonintegrations.proposal.tax.feign.EnterpriseTaxServiceClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext
@SpringBootTest
public class EnterpriseTaxServiceClientMocks {

    @Autowired
    private EnterpriseTaxServiceClient taxServiceClient;

    public static WireMockServer wiremock = new WireMockServer(WireMockSpring.options()
            .usingFilesUnderClasspath("wiremock/EnterpriseTaxService")
            .port(8080)
            .bindAddress("127.0.0.1"));

    @BeforeAll
    static void setupClass() {
        wiremock.start();
    }

    @AfterEach
    void after() {
        wiremock.resetAll();
    }

    @AfterAll
    static void clean() {
        wiremock.shutdown();
    }

    @Test
    void testCalculateTax_retry_on_timeout(){
        assertThrows(RetryableException.class, () ->
                taxServiceClient.calculateTax(TaxTransactionDTO.builder().build()));

        verify(exactly(3), postRequestedFor(urlEqualTo("/calculateTax")));
    }

    @Test
    void testCalculateTax_no_retry_on_500() {
        stubFor(post(urlEqualTo("/calculateTax"))
                .withRequestBody(equalToJson("{\n    \"transactionId\": \"54bf6bb4-341e-4348-82fb-122994950e1a\",\n    \"sourceSystem\": null,\n    \"orderId\": \"testOppId\",\n    \"orderDate\": null,\n    \"ringStore\": null,\n    \"taxDate\": null,\n    \"lineItems\": []\n}"))
                .willReturn(serverError()));

        assertThrows(FeignException.InternalServerError.class, () ->
                taxServiceClient.calculateTax(TaxTransactionDTO.builder()
                        .orderId("testOppId")
                        .transactionId("54bf6bb4-341e-4348-82fb-122994950e1a")
                        .lineItems(new ArrayList<>())
                        .build())
        );

        verify(exactly(1), postRequestedFor(urlEqualTo("/calculateTax")));
    }
}
