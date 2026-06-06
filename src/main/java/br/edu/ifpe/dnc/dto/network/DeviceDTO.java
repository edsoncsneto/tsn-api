package br.edu.ifpe.dnc.dto.network;

import java.util.List;

public class DeviceDTO {

    private String deviceName;
    private int processingDelay;
    private List<InterfaceDTO> interfaces;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getProcessingDelay() {
        return processingDelay;
    }

    public void setProcessingDelay(int processingDelay) {
        this.processingDelay = processingDelay;
    }

    public List<InterfaceDTO> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<InterfaceDTO> interfaces) {
        this.interfaces = interfaces;
    }
}
