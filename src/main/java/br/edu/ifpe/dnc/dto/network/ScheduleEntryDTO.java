package br.edu.ifpe.dnc.dto.network;

public class ScheduleEntryDTO {

    private long openTime;
    private long closeTime;
    private long periodLength;
    private int priority;

    public long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public long getPeriodLength() {
        return periodLength;
    }

    public void setPeriodLength(long periodLength) {
        this.periodLength = periodLength;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
