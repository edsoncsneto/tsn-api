package br.edu.ifpe.dnc.service;

import br.edu.ifpe.dnc.dto.AnalysisResultDTO;
import br.edu.ifpe.dnc.parser.NetworkJsonParser;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import org.networkcalculus.dnc.ethernet.tsn.data.DatasetReader;
import org.networkcalculus.dnc.ethernet.tsn.dto.EthernetNetworkDTO;
import org.networkcalculus.dnc.ethernet.tsn.model.EthernetNetwork;
import org.networkcalculus.dnc.ethernet.tsn.results.database.DatabaseResults2;
import org.networkcalculus.dnc.ethernet.tsn.results.database.DatabaseRow;
import org.networkcalculus.dnc.ethernet.tsn.results.database.paper_access_2018.DatabaseColumnIDAccessPaper2018;
import org.networkcalculus.dnc.ethernet.service.networkcalculus.AnalyzerService2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@IfBuildProfile("worker")
@ApplicationScoped
public class TsnAnalysisService {

    public List<AnalysisResultDTO> analyze(String jsonContent) {
        try {
            EthernetNetworkDTO networkDTO = NetworkJsonParser.parse(jsonContent);

            EthernetNetwork network = EthernetNetwork.build(networkDTO);
            DatabaseResults2 dbResults = AnalyzerService2.performAnalysis3(network);

            Object networkCase = networkDTO.metadata().get(DatasetReader.DATASET_METADATA_NETWORK_CASE);
            Object datasetCase = networkDTO.metadata().get(DatasetReader.DATASET_METADATA_DATASET_CASE);
            String networkCaseStr = networkCase != null ? networkCase.toString() : null;
            String datasetCaseStr = datasetCase != null ? datasetCase.toString() : null;

            return toResultList(dbResults, networkCaseStr, datasetCaseStr);
            
        } catch (Exception e) {
            AnalysisResultDTO error = new AnalysisResultDTO();
            error.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
            return List.of(error);
        }
    }

    private List<AnalysisResultDTO> toResultList(DatabaseResults2 dbResults, String networkCase, String datasetName) {
        return dbResults.selectByMapping(Map.of()).parallelStream()
                .map(row -> mapRowToDto(row, networkCase, datasetName))
                .toList();
    }

    private AnalysisResultDTO mapRowToDto(DatabaseRow row, String networkCase, String datasetName) {
        AnalysisResultDTO dto = new AnalysisResultDTO();

        Map<String, String> key = new LinkedHashMap<>();
        key.put("Network Case", networkCase);
        key.put("Used Multiplexing", toString(row.getValue(DatabaseColumnIDAccessPaper2018.MULTIPLEXING)));
        key.put("Dataset Case", datasetName);
        key.put("Flow Name", toString(row.getValue(DatabaseColumnIDAccessPaper2018.FLOW_NAME)));
        dto.setCompositeKey(key);

        dto.setDeadlineUs(toDouble(row.getValue(DatabaseColumnIDAccessPaper2018.DEADLINE)));

        Map<String, Double> bounds = new LinkedHashMap<>();
        bounds.put("delay bound TFA (us)", toDouble(row.getValue(DatabaseColumnIDAccessPaper2018.TFA_DELAY_BOUND)));
        bounds.put("delay bound SFA (us)", toDouble(row.getValue(DatabaseColumnIDAccessPaper2018.SFA_DELAY_BOUND)));
        bounds.put("delay bound PMOO (us)", toDouble(row.getValue(DatabaseColumnIDAccessPaper2018.PMOO_DELAY_BOUND)));
        bounds.put("delay bound TMA (us)", toDouble(row.getValue(DatabaseColumnIDAccessPaper2018.TMA_DELAY_BOUND)));
        dto.setDelayBoundUsMap(bounds);

        return dto;
    }

    private Double toDouble(Object val) {
        return val instanceof Double d ? d : null;
    }

    private String toString(Object val) {
        return val != null ? val.toString() : null;
    }
}
