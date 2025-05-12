package com.medicalmanager.medical.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medicalmanager.medical.dto.AppointmentRequest;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.service.AppointmentService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(@RequestBody AppointmentRequest request) {
        try {
            Appointment appointment = appointmentService.bookAppointment(
                    request.getDoctorId(),
                    request.getPatientId(),
                    request.getDateTime(),
                    request.getDuration(),
                    request.getAppointmentType(),
                    request.getReasonForVisit());
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/log")
    public ResponseEntity<String> logAppointment(@RequestBody AppointmentRequest request) {
        System.out.println("Received appointment booking data:");
        System.out.println("Doctor ID: " + request.getDoctorId());
        System.out.println("Patient ID: " + request.getPatientId());
        System.out.println("DateTime: " + request.getDateTime());
        System.out.println("Duration: " + request.getDuration());
        System.out.println("Appointment Type: " + request.getAppointmentType());
        System.out.println("Reason for Visit: " + request.getReasonForVisit());
        return ResponseEntity.ok("Logged appointment data successfully");
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId, startDate, endDate);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getPatientAppointments(@PathVariable Long patientId) {
        List<Appointment> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable String status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{appointmentId}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime) {

        try {
            Appointment appointment = appointmentService.rescheduleAppointment(appointmentId, newDateTime);
            return ResponseEntity.ok(appointment);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {

        try {
            Appointment appointment = appointmentService.cancelAppointment(
                    appointmentId, request.get("cancellationReason"));
            return ResponseEntity.ok(appointment);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{appointmentId}/complete")
    public ResponseEntity<Appointment> completeAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {

        try {
            Appointment appointment = appointmentService.completeAppointment(
                    appointmentId, request.get("notes"));
            return ResponseEntity.ok(appointment);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctor/{doctorId}/today")
    public ResponseEntity<List<Appointment>> getTodaysAppointments(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getTodaysAppointments(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}/availability")
    public ResponseEntity<Map<String, Object>> checkDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam(defaultValue = "30") Integer duration) {

        Map<String, Object> availability = appointmentService.checkDoctorAvailability(doctorId, dateTime, duration);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/doctor/{doctorId}/available-slots")
    public ResponseEntity<Map<String, Object>> findAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer duration) {

        Map<String, Object> slots = appointmentService.findAvailableSlots(doctorId, date, duration);
        return ResponseEntity.ok(slots);
    }
}
