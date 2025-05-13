//Transfers available time slots data from backend to frontend
package com.medicalmanager.medical.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class AvailabilityDto {
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    private Boolean off = true;

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek d) {
        this.day = d;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime s) {
        this.start = s;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime e) {
        this.end = e;
    }

    public Boolean getOff() {
        return off;
    }

    public void setOff(Boolean o) {
        this.off = o;
    }
}
