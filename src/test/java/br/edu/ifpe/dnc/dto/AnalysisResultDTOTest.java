package br.edu.ifpe.dnc.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisResultDTOTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void shouldSerializeToExpectedJsonFormat() throws Exception {
        AnalysisResultDTO dto = new AnalysisResultDTO();

        Map<String, String> key = new LinkedHashMap<>();
        key.put("Network Case", "1000BASE-T1S");
        key.put("Used Multiplexing", "FIFO");
        key.put("Dataset Case", "1-2");
        key.put("Flow Name", "tt1");
        dto.setCompositeKey(key);
        dto.setDeadlineUs(58972.0);

        Map<String, Double> bounds = new LinkedHashMap<>();
        bounds.put("delay bound TFA (us)", 1204.52);
        bounds.put("delay bound SFA (us)", 913.03);
        bounds.put("delay bound PMOO (us)", null);
        bounds.put("delay bound TMA (us)", null);
        dto.setDelayBoundUsMap(bounds);

        String json = mapper.writeValueAsString(dto);

        assertTrue(json.contains("\"compositeKey\""));
        assertTrue(json.contains("\"deadline (us)\""));
        assertTrue(json.contains("\"delayBoundUsMap\""));
        assertTrue(json.contains("\"Network Case\""));
        assertTrue(json.contains("\"1000BASE-T1S\""));
        assertFalse(json.contains("\"error\""));
    }

    @Test
    void shouldDeserializeFromExpectedJsonFormat() throws Exception {
        String json = """
                {
                    "compositeKey": {
                        "Network Case": "1000BASE-T1S",
                        "Used Multiplexing": "FIFO",
                        "Dataset Case": "1-2",
                        "Flow Name": "tt10"
                    },
                    "deadline (us)": 8908.0,
                    "delayBoundUsMap": {
                        "delay bound TFA (us)": 1354.66,
                        "delay bound SFA (us)": 985.43,
                        "delay bound PMOO (us)": null,
                        "delay bound TMA (us)": null
                    }
                }
                """;

        AnalysisResultDTO dto = mapper.readValue(json, AnalysisResultDTO.class);

        assertEquals("1000BASE-T1S", dto.getCompositeKey().get("Network Case"));
        assertEquals("FIFO", dto.getCompositeKey().get("Used Multiplexing"));
        assertEquals("1-2", dto.getCompositeKey().get("Dataset Case"));
        assertEquals("tt10", dto.getCompositeKey().get("Flow Name"));
        assertEquals(8908.0, dto.getDeadlineUs());
        assertEquals(1354.66, dto.getDelayBoundUsMap().get("delay bound TFA (us)"));
        assertEquals(985.43, dto.getDelayBoundUsMap().get("delay bound SFA (us)"));
        assertNull(dto.getDelayBoundUsMap().get("delay bound PMOO (us)"));
        assertNull(dto.getDelayBoundUsMap().get("delay bound TMA (us)"));
        assertNull(dto.getError());
    }

    @Test
    void shouldSerializeErrorResponse() throws Exception {
        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setError("RuntimeException: something went wrong");

        String json = mapper.writeValueAsString(dto);

        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("something went wrong"));
    }

    @Test
    void shouldDeserializeListMatchingSampleFormat() throws Exception {
        String sampleJson = """
                [
                    {
                        "compositeKey": {
                            "Network Case": "1000BASE-T1S",
                            "Used Multiplexing": "FIFO",
                            "Dataset Case": "1-2",
                            "Flow Name": "tt1"
                        },
                        "deadline (us)": 58972.0,
                        "delayBoundUsMap": {
                            "delay bound TFA (us)": 1204.5232799138664,
                            "delay bound SFA (us)": 913.0273884715701,
                            "delay bound PMOO (us)": null,
                            "delay bound TMA (us)": null
                        }
                    },
                    {
                        "compositeKey": {
                            "Network Case": "1000BASE-T1S",
                            "Used Multiplexing": "FIFO",
                            "Dataset Case": "1-2",
                            "Flow Name": "tt10"
                        },
                        "deadline (us)": 8908.0,
                        "delayBoundUsMap": {
                            "delay bound TFA (us)": 1354.6638264382248,
                            "delay bound SFA (us)": 985.432386833053,
                            "delay bound PMOO (us)": null,
                            "delay bound TMA (us)": null
                        }
                    }
                ]
                """;

        List<AnalysisResultDTO> results = mapper.readValue(sampleJson, new TypeReference<>() {});

        assertEquals(2, results.size());
        assertEquals("tt1", results.get(0).getCompositeKey().get("Flow Name"));
        assertEquals("tt10", results.get(1).getCompositeKey().get("Flow Name"));
        assertEquals(58972.0, results.get(0).getDeadlineUs());
        assertEquals(1354.6638264382248, results.get(1).getDelayBoundUsMap().get("delay bound TFA (us)"));
    }

    @Test
    void shouldRoundTripSerializeDeserialize() throws Exception {
        AnalysisResultDTO original = new AnalysisResultDTO();
        original.setCompositeKey(Map.of("Flow Name", "tt5", "Network Case", "Baseline"));
        original.setDeadlineUs(34481.0);
        original.setDelayBoundUsMap(Map.of("delay bound TFA (us)", 395.97));

        String json = mapper.writeValueAsString(original);
        AnalysisResultDTO deserialized = mapper.readValue(json, AnalysisResultDTO.class);

        assertEquals(original.getDeadlineUs(), deserialized.getDeadlineUs());
        assertEquals(original.getCompositeKey().get("Flow Name"), deserialized.getCompositeKey().get("Flow Name"));
        assertEquals(original.getDelayBoundUsMap().get("delay bound TFA (us)"),
                deserialized.getDelayBoundUsMap().get("delay bound TFA (us)"));
    }
}
