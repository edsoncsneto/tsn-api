package br.edu.ifpe.dnc.dto.network;

import java.util.List;

public class InterfaceDTO {

    private int interfaceId;
    private String phyStandard;
    private int plcaWeightWRR;
    private int maxGclEntries;
    private List<ScheduleEntryDTO> schedule;

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getPhyStandard() {
        return phyStandard;
    }

    public void setPhyStandard(String phyStandard) {
        this.phyStandard = phyStandard;
    }

    public int getPlcaWeightWRR() {
        return plcaWeightWRR;
    }

    public void setPlcaWeightWRR(int plcaWeightWRR) {
        this.plcaWeightWRR = plcaWeightWRR;
    }

    public int getMaxGclEntries() {
        return maxGclEntries;
    }

    public void setMaxGclEntries(int maxGclEntries) {
        this.maxGclEntries = maxGclEntries;
    }

    public List<ScheduleEntryDTO> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<ScheduleEntryDTO> schedule) {
        this.schedule = schedule;
    }
}
