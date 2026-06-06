package br.edu.ifpe.dnc.dto.network;

import java.util.List;

public class FlowDTO {

    private String flowName;
    private double sizeBytes;
    private double deadlineUs;
    private String type;
    private int priority;
    private double periodUs;
    private double offsetUs;
    private String sourceDevice;
    private String destinationDevice;
    private List<PathHopDTO> path;

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public double getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(double sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public double getDeadlineUs() {
        return deadlineUs;
    }

    public void setDeadlineUs(double deadlineUs) {
        this.deadlineUs = deadlineUs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getPeriodUs() {
        return periodUs;
    }

    public void setPeriodUs(double periodUs) {
        this.periodUs = periodUs;
    }

    public double getOffsetUs() {
        return offsetUs;
    }

    public void setOffsetUs(double offsetUs) {
        this.offsetUs = offsetUs;
    }

    public String getSourceDevice() {
        return sourceDevice;
    }

    public void setSourceDevice(String sourceDevice) {
        this.sourceDevice = sourceDevice;
    }

    public String getDestinationDevice() {
        return destinationDevice;
    }

    public void setDestinationDevice(String destinationDevice) {
        this.destinationDevice = destinationDevice;
    }

    public List<PathHopDTO> getPath() {
        return path;
    }

    public void setPath(List<PathHopDTO> path) {
        this.path = path;
    }
}
