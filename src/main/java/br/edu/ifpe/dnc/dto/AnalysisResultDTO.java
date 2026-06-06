package br.edu.ifpe.dnc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class AnalysisResultDTO {

    private Map<String, String> compositeKey;

    @JsonProperty("deadline (us)")
    private Double deadlineUs;

    private Map<String, Double> delayBoundUsMap;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public Map<String, String> getCompositeKey() {
        return compositeKey;
    }

    public void setCompositeKey(Map<String, String> compositeKey) {
        this.compositeKey = compositeKey;
    }

    public Double getDeadlineUs() {
        return deadlineUs;
    }

    public void setDeadlineUs(Double deadlineUs) {
        this.deadlineUs = deadlineUs;
    }

    public Map<String, Double> getDelayBoundUsMap() {
        return delayBoundUsMap;
    }

    public void setDelayBoundUsMap(Map<String, Double> delayBoundUsMap) {
        this.delayBoundUsMap = delayBoundUsMap;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
