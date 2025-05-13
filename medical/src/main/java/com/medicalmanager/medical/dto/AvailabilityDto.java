//Transfers available time slots data from backend to frontend
package com.medicalmanager.medical.dto;

import java.time.LocalTime;

public class AvailabilityDto {
    private String startTime;
    private String endTime;

    public AvailabilityDto() {
    }

    public AvailabilityDto(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
