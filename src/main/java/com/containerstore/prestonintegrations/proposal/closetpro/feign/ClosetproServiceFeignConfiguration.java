package com.containerstore.prestonintegrations.proposal.closetpro.feign;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ClosetproServiceFeignConfiguration {

    @Value("${feign.client.config.closetproService.basicAuth.username}")
    private String username;

    @Value("${feign.client.config.closetproService.basicAuth.password}")
    private String password;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(username, password);
    }
}
