package com.medicalmanager.medical.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private LocalDateTime dateTime;
    private Integer duration; // in minutes
    private String status;
    private String appointmentType;
    private String reasonForVisit;
    private String notes;

    @Column(name = "previous_appointment_id")
    private Long previousAppointmentId;

    // Getters
    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getStatus() {
        return status;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public String getNotes() {
        return notes;
    }

    public Long getPreviousAppointmentId() {
        return previousAppointmentId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPreviousAppointmentId(Long previousAppointmentId) {
        this.previousAppointmentId = previousAppointmentId;
    }

    // Business logic methods
    public LocalDateTime getEndTime() {
        return dateTime != null && duration != null
                ? dateTime.plusMinutes(duration)
                : null;
    }

    /*
     * @Override
     * public String toString() {
     * return "Appointment{" +
     * "id=" + id +
     * ", doctorId=" + (doctor != null ? doctor.getId() : null) +
     * ", patientId=" + (patient != null ? patient.getId() : null) +
     * ", dateTime=" + dateTime +
     * ", duration=" + duration +
     * ", status='" + status + '\'' +
     * ", type='" + appointmentType + '\'' +
     * '}';
     * }
     */

}