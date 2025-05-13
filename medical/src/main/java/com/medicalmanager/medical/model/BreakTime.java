package com.medicalmanager.medical.model;

import jakarta.persistence.*;

@Entity
@Table(name = "break_times")
public class BreakTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private String dayOfWeek;
    private String startTime;
    private String endTime;

    // Getters
    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /*
     * @Override
     * public String toString() {
     * return "BreakTime{" +
     * "id=" + id +
     * ", doctor=" + (doctor != null ? doctor.getId() : null) +
     * ", dayOfWeek='" + dayOfWeek + '\'' +
     * ", startTime='" + startTime + '\'' +
     * ", endTime='" + endTime + '\'' +
     * '}';
     * }
     */

}