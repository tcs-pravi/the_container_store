package com.containerstore.prestonintegrations.proposal.wiremockintegrations;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

//todo add with retry and test fault tolerance
public class ClosetProMocks {


    public static WireMockServer wiremock = new WireMockServer(WireMockSpring
            .options()
            .usingFilesUnderClasspath("wiremock/closetpro")
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

}
