package com.containerstore.prestonintegrations.proposal.configuration.health;

import com.containerstore.spring.module.rest.health.HealthAssessment;
import com.containerstore.spring.module.rest.health.HealthAssessor;
import com.containerstore.spring.module.rest.health.HealthStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static com.containerstore.spring.module.rest.health.HealthAssessment.errorHealthAssessment;
import static com.containerstore.spring.module.rest.health.HealthAssessment.normalHealthAssessment;
import static com.google.common.base.Throwables.getRootCause;
import static java.lang.String.format;

@Slf4j
public class ServiceHealthAssessor implements HealthAssessor {
    private final RestOperations restOperations;
    private final URI uri;

    public ServiceHealthAssessor(RestOperations restOperations, URI uri) {
        this.restOperations = restOperations;
        this.uri = uri;
    }

    @Override
    public HealthAssessment assessHealth() {
        try {
            ResponseEntity<JsonNode> response = restOperations.getForEntity(uri, JsonNode.class);
            log.debug("Health Response for {} is : {}", uri, response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                return assessmentOf(HealthStatus.NORMAL);
            } else {
                return assessmentOf(HealthStatus.ERROR);
            }

        } catch (Exception ex) {
            log.error("Exception caught while assessing health of " + getName(), getRootCause(ex));
            return errorHealthAssessment(getName(), format("Received status %s", HealthStatus.ERROR));
        }
    }

    @Override
    public String getName() {
        return this.uri.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    private HealthAssessment assessmentOf(HealthStatus status) {
        return status == HealthStatus.NORMAL ? normalHealthAssessment(getName())
                : errorHealthAssessment(getName(), format("Received status %s", status));
    }
}
