package org.lab.dev.health;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Liveness
@ApplicationScoped
public class KeycloakConnectionHealthCheck implements HealthCheck {

    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "false")
    Provider<String> keycloakUrl;

    @Override
    public HealthCheckResponse call() {

        HealthCheckResponseBuilder responseBuilder =
                HealthCheckResponse.named("Keycloak connection health check");

        try {
            keycloakConnectionVerification();
            responseBuilder.up();
        } catch (IllegalStateException e) {
            // cannot access keycloak
            responseBuilder.down().withData("error", e.getMessage());
        }

        return responseBuilder.build();
    }

    private void keycloakConnectionVerification() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(keycloakUrl.get()))
                .build();

        HttpResponse<String> response = null;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IOException", e);
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
            Thread.currentThread().interrupt();
        }

        if (response == null || response.statusCode() != 200) {
            throw new IllegalStateException("Cannot contact Keycloak");
        }
    }
}
