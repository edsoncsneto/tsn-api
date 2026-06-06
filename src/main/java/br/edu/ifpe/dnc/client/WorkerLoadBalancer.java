package br.edu.ifpe.dnc.client;

import br.edu.ifpe.dnc.dto.AnalysisResultDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@IfBuildProfile("gateway")
@ApplicationScoped
public class WorkerLoadBalancer {

    private static final Logger log = Logger.getLogger(WorkerLoadBalancer.class);

    @ConfigProperty(name = "tsn.worker.urls", defaultValue = "http://localhost:8080")
    List<String> workerUrls;

    @ConfigProperty(name = "tsn.loadbalancer.health-check-interval-seconds", defaultValue = "10")
    int healthCheckIntervalSeconds;

    @ConfigProperty(name = "tsn.loadbalancer.request-timeout-seconds", defaultValue = "30")
    int requestTimeoutSeconds;

    @ConfigProperty(name = "tsn.loadbalancer.connection-timeout-seconds", defaultValue = "3")
    int connectionTimeoutSeconds;

    @ConfigProperty(name = "tsn.loadbalancer.max-consecutive-failures", defaultValue = "5")
    int maxConsecutiveFailures;

    @Inject
    ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, WorkerInfo> workers = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private HttpClient httpClient;
    private ScheduledExecutorService healthCheckScheduler;

    @PostConstruct
    void init() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .build();

        for (String url : workerUrls) {
            WorkerInfo worker = new WorkerInfo(url);
            workers.put(worker.getId(), worker);
        }

        healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "worker-health-check");
            t.setDaemon(true);
            return t;
        });

        healthCheckScheduler.scheduleWithFixedDelay(
                this::checkAllWorkers,
                0,
                healthCheckIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    @PreDestroy
    void shutdown() {
        if (healthCheckScheduler != null) {
            healthCheckScheduler.shutdownNow();
        }
    }

    public List<AnalysisResultDTO> send(JsonNode networkJson) {
        WorkerInfo worker = nextOnlineWorker();

        if (worker == null) {
            AnalysisResultDTO error = new AnalysisResultDTO();
            error.setError("No online workers available");
            return List.of(error);
        }

        worker.incrementActiveRequests();
        long startTime = System.nanoTime();
        try {
            String body = objectMapper.writeValueAsString(networkJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(worker.getUrl() + "/api/v1/analysis/single"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.infof("Worker %s completed in %d ms", worker.getId(), elapsedMs);
                return objectMapper.readValue(response.body(), new TypeReference<>() {});

            } else {
                log.warnf("Worker %s returned HTTP %d in %d ms", worker.getId(), response.statusCode(), elapsedMs);
                AnalysisResultDTO error = new AnalysisResultDTO();
                error.setError("Worker " + worker.getId() + " returned HTTP " + response.statusCode());
                return List.of(error);
            }

        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            log.warnf("Worker %s failed after %d ms: %s", worker.getId(), elapsedMs, e.getMessage());
            worker.markOffline();
            AnalysisResultDTO error = new AnalysisResultDTO();
            error.setError("Worker " + worker.getId() + ": " + e.getMessage());
            return List.of(error);

        } finally {
            worker.decrementActiveRequests();
        }
    }

    public void addWorker(String url) {
        WorkerInfo worker = new WorkerInfo(url, true);
        workers.put(worker.getId(), worker);
        checkWorkerHealth(worker);
    }

    public boolean removeWorker(String id) {
        WorkerInfo removed = workers.remove(id);

        if (removed != null) {
            return true;
        }
        return false;
    }

    public boolean hasOnlineWorkers() {
        return workers.values().stream().anyMatch(WorkerInfo::isOnline);
    }

    public List<String> getWorkerAllStatuses() {
        return workers.values().stream()
                .map(WorkerInfo::toString)
                .toList();
    }

    public List<String> getWorkerStatus(String id) {
        WorkerInfo worker = workers.get(id);

        if (worker == null) {
            return List.of("Worker not found: " + id);
        }

        return List.of(worker.toString());
    }

    private void checkAllWorkers() {
        for (WorkerInfo worker : workers.values()) {
            checkWorkerHealth(worker);
        }
        deregisterDeadWorkers();
    }

    private void deregisterDeadWorkers() {
        workers.values().removeIf(worker ->
            worker.isDynamic() && worker.getConsecutiveFailures() >= maxConsecutiveFailures
        );
    }

    private void checkWorkerHealth(WorkerInfo worker) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(worker.getUrl() + "/q/health/ready"))
                    .timeout(Duration.ofSeconds(connectionTimeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() == 200) {
                worker.markOnline();
            } else {
                worker.markOffline();
            }
        } catch (Exception e) {
            worker.markOffline();
        }
    }

    private WorkerInfo nextOnlineWorker() {
        List<WorkerInfo> onlineWorkers = workers.values().stream()
                .filter(WorkerInfo::isOnline)
                .toList();

        if (onlineWorkers.isEmpty()) {
            return null;
        }

        int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % onlineWorkers.size();
        return onlineWorkers.get(index);
    }
}
