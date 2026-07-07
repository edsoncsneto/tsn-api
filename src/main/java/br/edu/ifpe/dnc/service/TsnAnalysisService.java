package br.edu.ifpe.dnc.service;

import br.edu.ifpe.dnc.parser.NetworkJsonParser;
import com.google.gson.Gson;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import org.networkcalculus.dnc.ethernet.service.networkcalculus.AnalyzerService3;
import org.networkcalculus.dnc.ethernet.tsn.TSNNetworkBuilder;
import org.networkcalculus.dnc.ethernet.tsn.dto.EthernetNetworkDTO;
import org.networkcalculus.dnc.ethernet.tsn.model.EthernetNetwork;
import org.networkcalculus.dnc.ethernet.tsn.results.database.v3.DatabaseResults3;

@IfBuildProfile("worker")
@ApplicationScoped
public class TsnAnalysisService {

    private static final Gson gson = new Gson();

    public String analyze(String jsonContent) {
        try {
            EthernetNetworkDTO networkDTO = NetworkJsonParser.parse(jsonContent);
            EthernetNetwork network = TSNNetworkBuilder.fromDTO(networkDTO);
            DatabaseResults3 results = AnalyzerService3.performAnalysis(network, null);
            return results.toJSON(gson);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getClass().getSimpleName() + ": " + e.getMessage() + "\"}";
        }
    }
}
