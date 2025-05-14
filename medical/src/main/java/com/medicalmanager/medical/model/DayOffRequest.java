package com.medicalmanager.medical.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "day_off_requests")
public class DayOffRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "day_off_date", nullable = false)
    private LocalDate dayOffDate;

    public DayOffRequest() {}

    public DayOffRequest(Doctor doctor, LocalDate dayOffDate) {
        this.doctor = doctor;
        this.dayOffDate = dayOffDate;
    }

    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getDayOffDate() {
        return dayOffDate;
    }

    public void setDayOffDate(LocalDate dayOffDate) {
        this.dayOffDate = dayOffDate;
    }
}
