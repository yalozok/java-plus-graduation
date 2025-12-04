package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitCreate;
import ru.practicum.stats.dto.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StatClientImpl implements StatClient {
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;
    private final RestClient client;
    private final String app;

    public StatClientImpl(DiscoveryClient discoveryClient,
                          @Value("${discovery.services.stats-server-id}") String statsServiceId,
                          @Value("${spring.application.name}") String app) {
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.app = app;
        this.client = RestClient.builder().build();

        this.retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    public void createHit(HttpServletRequest request) {
        log.trace("STAT CLIENT: createHit() call with request: {}", request);
        EndpointHitCreate hitCreate = EndpointHitCreate.builder()
                .app(app)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        ResponseEntity<Void> result = client
                .post()
                .uri(makeStatsServerUrl() + "/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitCreate)
                .retrieve()
                .toEntity(Void.class);

        if (result.getStatusCode().is2xxSuccessful()) {
            log.info("STAT CLIENT: createHit() success with status: {}",
                    result.getStatusCode());
        } else {
            log.warn("STAT CLIENT: createHit() failure with status: {}",
                    result.getStatusCode());
        }
    }

    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start,
                                                    LocalDateTime end,
                                                    List<String> uris,
                                                    boolean unique) {
        log.info("STAT CLIENT: getStats() call with params: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(makeStatsServerUrl() + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);
        uris.forEach(uri -> builder.queryParam("uris", uri));
        String path = builder.toUriString();

        log.info("STAT CLIENT: final Uri : {}", path);

        ResponseEntity<List<ViewStats>> result = client
                .get()
                .uri(path)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<ViewStats>>() {
                });

        if (result.getStatusCode().is2xxSuccessful()) {
            log.info("STAT CLIENT: getStats() success with status: {}, body: {}",
                    result.getStatusCode(), result.getBody());
        } else {
            log.info("STAT CLIENT: getStats() failure with status: {}, body: {}",
                    result.getStatusCode(), result.getBody());
        }

        return result;
    }

    private ServiceInstance getInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);

            log.info("Discovered {} instances for service '{}': {}",
                    instances.size(), statsServiceId, instances);

            if (instances.isEmpty()) {
                throw new StatsServerUnavailable(
                        "No instances found in Eureka for serviceId=" + statsServiceId);
            }

            return instances.getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Error while discovering stats server with id: " + statsServiceId
            );
        }
    }

    private URI makeStatsServerUrl() {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort());
    }
}