package br.edu.ifpe.dnc.gateway;

import br.edu.ifpe.dnc.model.WorkerInfo;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@IfBuildProfile("gateway")
@ApplicationScoped
public class PoolManager {

    @ConfigProperty(name = "tsn.worker.urls", defaultValue = "http://localhost:8080")
    List<String> workerUrls;

    @ConfigProperty(name = "tsn.loadbalancer.max-consecutive-failures", defaultValue = "5")
    int maxConsecutiveFailures;

    private final ConcurrentHashMap<String, WorkerInfo> workers = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        for (String url : workerUrls) {
            WorkerInfo worker = new WorkerInfo(url);
            workers.put(worker.getId(), worker);
        }
    }

    public void register(String url) {
        WorkerInfo worker = new WorkerInfo(url, true);
        workers.put(worker.getId(), worker);
    }

    public boolean unregister(String url) {
        String id = extractId(url);
        return workers.remove(id) != null;
    }

    public WorkerInfo getWorker(String id) {
        return workers.get(id);
    }

    public List<WorkerInfo> getAllWorkers() {
        return List.copyOf(workers.values());
    }

    public List<WorkerInfo> getOnlineWorkers() {
        return workers.values().stream()
                .filter(WorkerInfo::isOnline)
                .toList();
    }

    public boolean hasOnlineWorkers() {
        return workers.values().stream().anyMatch(WorkerInfo::isOnline);
    }

    public void deregisterDeadWorkers() {
        workers.values().removeIf(worker ->
            worker.isDynamic() && worker.getConsecutiveFailures() >= maxConsecutiveFailures
        );
    }

    public List<String> getAllStatuses() {
        return workers.values().stream()
                .map(WorkerInfo::toString)
                .toList();
    }

    public List<String> getStatus(String id) {
        WorkerInfo worker = workers.get(id);
        if (worker == null) {
            return List.of("Worker not found: " + id);
        }
        return List.of(worker.toString());
    }

    private String extractId(String url) {
        if (url == null) return "";
        String normalized = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return normalized.replaceFirst("^https?://", "").replaceFirst("/$", "");
    }
}
