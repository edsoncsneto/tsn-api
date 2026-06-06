package br.edu.ifpe.dnc.dto.network;

import java.util.List;

public class NetworkDTO {

    private String networkCase;
    private String datasetName;
    private String description;
    private ExecutionConfigDTO executionConfig;
    private List<FlowDTO> flows;
    private List<DeviceDTO> devices;
    private List<LinkDTO> links;

    public String getNetworkCase() {
        return networkCase;
    }

    public void setNetworkCase(String networkCase) {
        this.networkCase = networkCase;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExecutionConfigDTO getExecutionConfig() {
        return executionConfig;
    }

    public void setExecutionConfig(ExecutionConfigDTO executionConfig) {
        this.executionConfig = executionConfig;
    }

    public List<FlowDTO> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowDTO> flows) {
        this.flows = flows;
    }

    public List<DeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDTO> devices) {
        this.devices = devices;
    }

    public List<LinkDTO> getLinks() {
        return links;
    }

    public void setLinks(List<LinkDTO> links) {
        this.links = links;
    }
}
