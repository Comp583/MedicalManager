//REST controller for handling API requests
package com.medicalmanager.medical.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medicalmanager.medical.dto.AppointmentRequest;
import com.medicalmanager.medical.dto.AvailabilityDto;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.repository.AppointmentRepository;
import com.medicalmanager.medical.service.AppointmentService;
import com.medicalmanager.medical.service.AvailabilityService;

@Controller
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;
    private final AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentService appointmentService,
            AvailabilityService availabilityService,
            AppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/availability/{doctorId}")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AvailabilityDto> availableSlots = availabilityService.getAvailableSlots(doctorId, date);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            // First check availability
            List<AvailabilityDto> availableSlots = availabilityService.getAvailableSlots(
                    appointmentRequest.getDoctorId(),
                    appointmentRequest.getAppointmentDate());

            // Calculate end time (assuming default 30 minute duration)
            LocalTime startTime = appointmentRequest.getAppointmentTime();
            LocalTime endTime = startTime.plusMinutes(30);

            // Verify the requested time is available
            boolean slotAvailable = availableSlots.stream()
                    .anyMatch(slot -> {
                        LocalTime slotStart = LocalTime.parse(slot.getStartTime());
                        LocalTime slotEnd = LocalTime.parse(slot.getEndTime());
                        return !startTime.isBefore(slotStart) && !endTime.isAfter(slotEnd);
                    });

            if (!slotAvailable) {
                throw new IllegalStateException("The requested time slot is no longer available");
            }

            Appointment appointment = appointmentService.bookAppointment(appointmentRequest);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "appointmentId", appointment.getId(),
                    "message", "Appointment booked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate != null && endDate != null) {
                return ResponseEntity.ok(appointmentRepository.findByDoctorIdAndAppointmentDateBetween(
                        doctorId, startDate, endDate));
            }
            return ResponseEntity.ok(appointmentRepository.findByDoctorId(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
