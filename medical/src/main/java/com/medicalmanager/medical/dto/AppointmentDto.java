package com.medicalmanager.medical.dto;

import java.time.format.DateTimeFormatter;

import com.medicalmanager.medical.model.Appointment;

public class AppointmentDto {
    private Long id;
    private String patientName;
    private String dateTime;
    private Integer duration;
    private String status;
    private String appointmentType;
    private String reasonForVisit;
    private String notes;

    public static AppointmentDto fromEntity(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setPatientName(appointment.getPatient() != null ? appointment.getPatient().getFullName() : null);
        dto.setDateTime(appointment.getDateTime() != null
                ? appointment.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null);
        dto.setDuration(appointment.getDuration());
        dto.setStatus(appointment.getStatus());
        dto.setAppointmentType(appointment.getAppointmentType());
        dto.setReasonForVisit(appointment.getReasonForVisit());
        dto.setNotes(appointment.getNotes());
        return dto;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
