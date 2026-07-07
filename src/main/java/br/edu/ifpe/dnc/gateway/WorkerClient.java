package br.edu.ifpe.dnc.gateway;

import br.edu.ifpe.dnc.dto.AnalysisResultDTO;
import br.edu.ifpe.dnc.model.WorkerInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@IfBuildProfile("gateway")
@ApplicationScoped
public class WorkerClient {

    @ConfigProperty(name = "tsn.loadbalancer.request-timeout-seconds", defaultValue = "30")
    int requestTimeoutSeconds;

    @ConfigProperty(name = "tsn.loadbalancer.connection-timeout-seconds", defaultValue = "3")
    int connectionTimeoutSeconds;

    @Inject
    ObjectMapper objectMapper;

    private HttpClient httpClient;

    @PostConstruct
    void init() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .build();
    } 

    public List<AnalysisResultDTO> sendAnalysis(WorkerInfo worker, JsonNode networkJson) {
        worker.incrementActiveRequests();
        try {
            String body = objectMapper.writeValueAsString(networkJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(worker.getUrl() + "/api/v1/analysis/single"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {});
            } else {
                AnalysisResultDTO error = new AnalysisResultDTO();
                error.setError("Worker " + worker.getId() + " returned HTTP " + response.statusCode());
                return List.of(error);
            }

        } catch (Exception e) {
            worker.markOffline();
            AnalysisResultDTO error = new AnalysisResultDTO();
            error.setError("Worker " + worker.getId() + ": " + e.getMessage());
            return List.of(error);
        } finally {
            worker.decrementActiveRequests();
        }
    }

    public boolean checkHealth(WorkerInfo worker) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(worker.getUrl() + "/q/health/ready"))
                    .timeout(Duration.ofSeconds(connectionTimeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}
