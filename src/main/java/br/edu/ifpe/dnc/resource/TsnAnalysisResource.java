package br.edu.ifpe.dnc.resource;

import br.edu.ifpe.dnc.service.TsnAnalysisService;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@IfBuildProfile("worker")
@Path("/api/v1/analysis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TsnAnalysisResource {

    @Inject
    TsnAnalysisService service;

    @POST
    @Path("/single")
    public String analyzeSingle(String networkJson) {
        return service.analyze(networkJson);
    }
}
