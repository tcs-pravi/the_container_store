package com.containerstore.prestonintegrations.proposal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableCaching
@SpringBootApplication
// Spring Configuration requires a publicly available constructor
@SuppressWarnings({ "squid:S1118", "HideUtilityClassConstructor"})
public class Application {

    private static final String ENVIRONMENT = System.getProperty("deployment.environment");
    private static final String STACK = System.getProperty("deployment.stack");

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static String[] profiles() {
        LOGGER.info("Deployment Environment: {}", ENVIRONMENT);
        LOGGER.info("Deployment Stack: {}", STACK);
        return new String[] { ENVIRONMENT };
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .profiles(profiles())
                .run(args);
        LOGGER.info("Service startup is complete");
    }
}
