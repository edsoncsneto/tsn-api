package br.edu.ifpe.dnc.dto.network;

import java.util.List;

public class ExecutionConfigDTO {

    private String generateTASWindowsExcelSheet;
    private String generateTASWindowsCharts;
    private String generateNCCurvesCharts;
    private String generatePortGuaranteedWinFiles;
    private String prioritizingOrder;
    private String multiplexing;
    private String validateSchedulingForFrameSize;
    private String saveServerGraph;
    private String plcaModeling;
    private List<String> analyses;

    public String getGenerateTASWindowsExcelSheet() {
        return generateTASWindowsExcelSheet;
    }

    public void setGenerateTASWindowsExcelSheet(String generateTASWindowsExcelSheet) {
        this.generateTASWindowsExcelSheet = generateTASWindowsExcelSheet;
    }

    public String getGenerateTASWindowsCharts() {
        return generateTASWindowsCharts;
    }

    public void setGenerateTASWindowsCharts(String generateTASWindowsCharts) {
        this.generateTASWindowsCharts = generateTASWindowsCharts;
    }

    public String getGenerateNCCurvesCharts() {
        return generateNCCurvesCharts;
    }

    public void setGenerateNCCurvesCharts(String generateNCCurvesCharts) {
        this.generateNCCurvesCharts = generateNCCurvesCharts;
    }

    public String getGeneratePortGuaranteedWinFiles() {
        return generatePortGuaranteedWinFiles;
    }

    public void setGeneratePortGuaranteedWinFiles(String generatePortGuaranteedWinFiles) {
        this.generatePortGuaranteedWinFiles = generatePortGuaranteedWinFiles;
    }

    public String getPrioritizingOrder() {
        return prioritizingOrder;
    }

    public void setPrioritizingOrder(String prioritizingOrder) {
        this.prioritizingOrder = prioritizingOrder;
    }

    public String getMultiplexing() {
        return multiplexing;
    }

    public void setMultiplexing(String multiplexing) {
        this.multiplexing = multiplexing;
    }

    public String getValidateSchedulingForFrameSize() {
        return validateSchedulingForFrameSize;
    }

    public void setValidateSchedulingForFrameSize(String validateSchedulingForFrameSize) {
        this.validateSchedulingForFrameSize = validateSchedulingForFrameSize;
    }

    public String getSaveServerGraph() {
        return saveServerGraph;
    }

    public void setSaveServerGraph(String saveServerGraph) {
        this.saveServerGraph = saveServerGraph;
    }

    public String getPlcaModeling() {
        return plcaModeling;
    }

    public void setPlcaModeling(String plcaModeling) {
        this.plcaModeling = plcaModeling;
    }

    public List<String> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<String> analyses) {
        this.analyses = analyses;
    }
}
