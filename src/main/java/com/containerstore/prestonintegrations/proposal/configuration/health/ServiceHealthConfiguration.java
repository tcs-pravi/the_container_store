package com.containerstore.prestonintegrations.proposal.configuration.health;

import com.containerstore.spring.module.rest.health.DataSourceHealthAssessor;
import com.containerstore.spring.module.rest.health.HealthChecker;
import com.containerstore.spring.module.rest.health.HealthCheckerBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.containerstore.spring.module.rest.health.HealthStatus.ERROR;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "health-checks-config")
public class ServiceHealthConfiguration {

    private List<String> services = new ArrayList<>();

    private List<String> dataSources = new ArrayList<>();


    private Duration connectTimeout = Duration.ofMillis(2000);
    private Duration readTimeout = Duration.ofMillis(4000);

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${tcs-useragent:undefined}")
    private String useragent;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean("healthCheckRestTemplate")
    public RestTemplate healthCheckRestTemplate() {
        return new RestTemplateBuilder()
                .defaultHeader("User-Agent",useragent)
                .setConnectTimeout(connectTimeout)
                .setConnectTimeout(readTimeout)
                .build();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "health-checks-config", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public HealthChecker healthChecker() {
        HealthCheckerBuilder builder = HealthChecker.builder();
        this.getServices().forEach(service -> builder.withHealthAccessor(buildServiceHealthAssessor(service)));
        getDataSources().forEach(dataSource -> builder.withHealthAccessor(buildDataSourceHealthAssessor(dataSource)));
        return builder.build();
    }

    private DataSourceHealthAssessor buildDataSourceHealthAssessor(String dataSource) {
        return new DataSourceHealthAssessor(applicationContext.getBean(dataSource, DataSource.class),
                dataSource, ERROR);
    }

    private ServiceHealthAssessor buildServiceHealthAssessor(String service) {
        return new ServiceHealthAssessor(healthCheckRestTemplate(), URI.create(service));
    }
}
