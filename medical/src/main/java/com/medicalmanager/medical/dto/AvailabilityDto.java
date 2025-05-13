//Transfers available time slots data from backend to frontend
package com.medicalmanager.medical.dto;

import java.time.DayOfWeek;

public class AvailabilityDto {
    private DayOfWeek day;
    private String startTime;
    private String endTime;
    private Boolean off = true;

    public AvailabilityDto() {

    }

    public AvailabilityDto(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek d) {
        this.day = d;
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

    public Boolean getOff() {
        return off;
    }

    public void setOff(Boolean o) {
        this.off = o;
    }
}
