package dev.hafnium.facade.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client factory for internal service communication.
 *
 * <p>
 * Creates pre-configured WebClient instances for calling backend microservices.
 */
@Component
public class ServiceClientFactory {

    @Value("${services.identity.url:http://localhost:8081}")
    private String identityServiceUrl;

    @Value("${services.screening.url:http://localhost:8082}")
    private String screeningServiceUrl;

    @Value("${services.monitoring.url:http://localhost:8083}")
    private String monitoringServiceUrl;

    @Value("${services.cases.url:http://localhost:8084}")
    private String caseServiceUrl;

    @Value("${services.vault.url:http://localhost:8085}")
    private String vaultServiceUrl;

    @Value("${services.risk.url:http://localhost:8086}")
    private String riskServiceUrl;

    @Value("${services.signals.url:http://localhost:8087}")
    private String signalsServiceUrl;

    /**
     * Creates a WebClient for the identity service.
     *
     * @return Configured WebClient
     */
    public WebClient identityClient() {
        return WebClient.builder().baseUrl(identityServiceUrl).build();
    }

    /**
     * Creates a WebClient for the screening service.
     *
     * @return Configured WebClient
     */
    public WebClient screeningClient() {
        return WebClient.builder().baseUrl(screeningServiceUrl).build();
    }

    /**
     * Creates a WebClient for the monitoring service.
     *
     * @return Configured WebClient
     */
    public WebClient monitoringClient() {
        return WebClient.builder().baseUrl(monitoringServiceUrl).build();
    }

    /**
     * Creates a WebClient for the case service.
     *
     * @return Configured WebClient
     */
    public WebClient caseClient() {
        return WebClient.builder().baseUrl(caseServiceUrl).build();
    }

    /**
     * Creates a WebClient for the vault service.
     *
     * @return Configured WebClient
     */
    public WebClient vaultClient() {
        return WebClient.builder().baseUrl(vaultServiceUrl).build();
    }

    /**
     * Creates a WebClient for the risk engine service.
     *
     * @return Configured WebClient
     */
    public WebClient riskClient() {
        return WebClient.builder().baseUrl(riskServiceUrl).build();
    }

    /**
     * Creates a WebClient for the signals service.
     *
     * @return Configured WebClient
     */
    public WebClient signalsClient() {
        return WebClient.builder().baseUrl(signalsServiceUrl).build();
    }
}
