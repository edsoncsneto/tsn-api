package br.edu.ifpe.dnc.gateway;

import br.edu.ifpe.dnc.model.WorkerInfo;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@IfBuildProfile("gateway")
@ApplicationScoped
public class LoadBalancer {

    @Inject
    PoolManager poolManager;

    private final AtomicInteger counter = new AtomicInteger(0);

    public WorkerInfo selectWorker() {
        List<WorkerInfo> onlineWorkers = poolManager.getOnlineWorkers();

        if (onlineWorkers.isEmpty()) {
            return null;
        }

        int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % onlineWorkers.size();
        return onlineWorkers.get(index);
    }
}
