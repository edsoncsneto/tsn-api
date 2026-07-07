package br.edu.ifpe.dnc.gateway;

import br.edu.ifpe.dnc.model.WorkerInfo;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Startup
@IfBuildProfile("gateway")
@ApplicationScoped
public class HealthCheckScheduler {

    @ConfigProperty(name = "tsn.loadbalancer.health-check-interval-seconds", defaultValue = "10")
    int healthCheckIntervalSeconds;

    @Inject
    PoolManager poolManager;

    @Inject
    WorkerClient workerClient;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "worker-health-check");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(
                this::checkAll,
                0,
                healthCheckIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    @PreDestroy
    void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void checkAll() {
        for (WorkerInfo worker : poolManager.getAllWorkers()) {
            boolean healthy = workerClient.checkHealth(worker);
            if (healthy) {
                worker.markOnline();
            } else {
                worker.markOffline();
            }
        }
        poolManager.deregisterDeadWorkers();
    }
}
