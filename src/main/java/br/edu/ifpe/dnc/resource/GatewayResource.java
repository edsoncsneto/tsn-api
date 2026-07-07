package br.edu.ifpe.dnc.resource;

import br.edu.ifpe.dnc.dto.ApiResponseDTO;
import br.edu.ifpe.dnc.dto.WorkerUrlDTO;
import br.edu.ifpe.dnc.gateway.LoadBalancer;
import br.edu.ifpe.dnc.gateway.PoolManager;
import br.edu.ifpe.dnc.gateway.WorkerClient;
import br.edu.ifpe.dnc.model.WorkerInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@IfBuildProfile("gateway")
@Path("/api/v1/gateway")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatewayResource {

    private static final Logger log = Logger.getLogger(GatewayResource.class);

    @Inject
    LoadBalancer loadBalancer;

    @Inject
    WorkerClient workerClient;

    @Inject
    PoolManager poolManager;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/analyze")
    public Response analyze(List<JsonNode> networkJsonList) {
        log.infof("Gateway received %d network(s) for analysis", networkJsonList.size());

        if (!poolManager.hasOnlineWorkers()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(ApiResponseDTO.error("No online workers available"))
                    .build();
        }

        long startTotal = System.nanoTime();

        List<String> results = networkJsonList.parallelStream()
                .map(json -> {
                    WorkerInfo worker = loadBalancer.selectWorker();
                    if (worker == null) {
                        return "{\"error\": \"No online workers available\"}";
                    }
                    try {
                        String jsonString = objectMapper.writeValueAsString(json);
                        return workerClient.sendAnalysis(worker, jsonString);
                    } catch (Exception e) {
                        return "{\"error\": \"" + e.getMessage() + "\"}";
                    }
                })
                .toList();

        long elapsedMs = (System.nanoTime() - startTotal) / 1_000_000;
        log.infof("Gateway analysis completed: %d network(s), total time: %d ms",
                networkJsonList.size(), elapsedMs);

        return Response.ok("[" + String.join(",", results) + "]").build();
    }

    @PUT
    @Path("/manage-worker/add")
    public Response addWorker(WorkerUrlDTO body) {
        poolManager.register(body.getUrl());
        return Response.ok(ApiResponseDTO.success("Worker added: " + body.getUrl())).build();
    }

    @GET
    @Path("/manage-worker/status")
    public List<String> getWorkerStatuses() {
        return poolManager.getAllStatuses();
    }

    @GET
    @Path("/manage-worker/status/{id}")
    public List<String> getWorkerStatus(@PathParam("id") String id) {
        return poolManager.getStatus(id);
    }

    @DELETE
    @Path("/manage-worker/remove")
    public Response removeWorker(WorkerUrlDTO body) {
        boolean removed = poolManager.unregister(body.getUrl());

        if (removed) {
            return Response.ok(ApiResponseDTO.success("Worker removed: " + body.getUrl())).build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponseDTO.error("Worker not found: " + body.getUrl()))
                .build();
    }
}
