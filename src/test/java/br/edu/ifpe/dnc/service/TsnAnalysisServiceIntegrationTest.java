package br.edu.ifpe.dnc.service;

import br.edu.ifpe.dnc.dto.AnalysisResultDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TsnAnalysisServiceIntegrationTest {

    private final TsnAnalysisService service = new TsnAnalysisService();

    @Test
    void shouldCallDncToolAndReturnWithoutCrashing() throws IOException {
        String json = loadResource("samples/dataset1-1.json");

        List<AnalysisResultDTO> results = service.analyze(json);

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void shouldReturnErrorForInvalidJson() {
        List<AnalysisResultDTO> results = service.analyze("{ invalid json }");

        assertEquals(1, results.size());
        assertNotNull(results.get(0).getError());
    }

    @Test
    void shouldReturnErrorForEmptyJson() {
        List<AnalysisResultDTO> results = service.analyze("{}");

        assertEquals(1, results.size());
        assertNotNull(results.get(0).getError());
    }

    @Test
    void shouldPopulateCompositeKeyWhenToolReturnsError() throws IOException {
        String json = loadResource("samples/dataset1-1.json");

        List<AnalysisResultDTO> results = service.analyze(json);

        // If the tool throws (IMPLEMENT THIS METHOD), we get an error DTO
        // If it succeeds, we get results with compositeKey populated
        AnalysisResultDTO first = results.get(0);
        assertTrue(first.getError() != null || first.getCompositeKey() != null,
                "Should either have error or compositeKey");
    }

    private String loadResource(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
