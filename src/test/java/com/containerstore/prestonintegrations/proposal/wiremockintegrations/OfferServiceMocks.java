package com.containerstore.prestonintegrations.proposal.wiremockintegrations;

import com.containerstore.offer.domain.ImmutablePresentedOfferRequest;
import com.containerstore.prestonintegrations.proposal.offer.feign.OfferServiceClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest
public class OfferServiceMocks {

    @Autowired
    private OfferServiceClient offerServiceClient;

    public static WireMockServer wiremock = new WireMockServer(WireMockSpring
            .options()
            .usingFilesUnderClasspath("wiremock/OfferService")
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

    //todo remove this with an actual meaningful test
    @Test
    void whenGetPresentedOffers_thenSuccess() {
        Assertions.assertEquals("PRPCT20", offerServiceClient
                .getPresentedOffer(ImmutablePresentedOfferRequest
                        .builder()
                        .withOfferCode("PRPCT20")
                        .withRingStore(899)
                        .build())
                .getOffer().getOfferCode());
    }
}
