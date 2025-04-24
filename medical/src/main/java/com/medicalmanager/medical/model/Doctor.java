package com.medicalmanager.medical.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workday_start_hour")
    private int workdayStartHour = 9; // Default to 9 AM (24-hour format)

    @Column(name = "workday_end_hour")
    private int workdayEndHour = 17; // Default to 5 PM (24-hour format)

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<AvailableSlot> availableSlots;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<BreakTime> breakTimes;

    // Getters and setters
    public int getWorkdayStartHour() {
        return workdayStartHour;
    }

    public void setWorkdayStartHour(int workdayStartHour) {
        this.workdayStartHour = workdayStartHour;
    }

    public int getWorkdayEndHour() {
        return workdayEndHour;
    }

    public void setWorkdayEndHour(int workdayEndHour) {
        this.workdayEndHour = workdayEndHour;
    }
}