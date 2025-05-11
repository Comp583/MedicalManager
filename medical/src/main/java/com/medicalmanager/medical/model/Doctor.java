package com.medicalmanager.medical.model;

import java.time.DayOfWeek;
import java.util.*;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workday_start_hour")
    private int workdayStartHour = 9;

    @Column(name = "workday_end_hour")
    private int workdayEndHour = 17;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name",  nullable = false)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String biography;

    //---------------  GENERATED MEDICAL ID  ---------------

    @Column(name = "medical_id", nullable = false, unique = true, updatable = false)
    private String medicalId;

    @PrePersist
    private void generateMedicalId() {
        if (this.medicalId == null) {
            // e.g. "MD-550e8400-e29b-41d4-a716-446655440000"
            this.medicalId = "MD-" + UUID.randomUUID().toString();
        }
    }

    public String getMedicalId() {
        return medicalId;
    }
    // no setter for medicalId!

    //---------------  ROLES  ---------------

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "doctor_roles", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    //---------------  AVAILABILITY (embedded map)  ---------------

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "doctor_availability", joinColumns = @JoinColumn(name = "doctor_id"))
    @MapKeyColumn(name = "day_of_week")
    @MapKeyEnumerated(EnumType.STRING)
    
    private Map<DayOfWeek, LocalTimeRange> availability = new EnumMap<>(DayOfWeek.class);

    //---------------  SLOTS & BREAKS ---------------

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AvailableSlot> availableSlots = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BreakTime> breakTimes = new ArrayList<>();

    //---------------  GETTERS & SETTERS  ---------------

    public Long getId() {
        return id;
    }

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

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBiography() {
        return biography;
    }
    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Map<DayOfWeek, LocalTimeRange> getAvailability() {
        return availability;
    }
    public void setAvailability(Map<DayOfWeek, LocalTimeRange> availability) {
        this.availability = availability;
    }

    public List<AvailableSlot> getAvailableSlots() {
        return availableSlots;
    }
    public void setAvailableSlots(List<AvailableSlot> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public List<BreakTime> getBreakTimes() {
        return breakTimes;
    }
    public void setBreakTimes(List<BreakTime> breakTimes) {
        this.breakTimes = breakTimes;
    }
}
