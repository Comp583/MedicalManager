package com.medicalmanager.medical.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.medicalmanager.medical.dto.AppointmentDto;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.service.AppointmentService;
import com.medicalmanager.medical.service.DoctorService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    public AppointmentRestController(AppointmentService appointmentService, DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getAppointments(Principal principal) {
        String username = principal.getName();
        Doctor doctor = doctorService.findByUsername(username);
        if (doctor == null) {
            return ResponseEntity.badRequest().build();
        }
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(7); // next 7 days
        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctor.getId(), startOfDay, endOfDay);
        List<AppointmentDto> dtos = appointments.stream()
                .map(AppointmentDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> rescheduleAppointment(@PathVariable Long id,
            @RequestBody RescheduleRequest request, Principal principal) {
        try {
            String username = principal.getName();
            System.out.println("Reschedule request by user: " + username + " for appointment id: " + id
                    + " with newDateTime: " + request.getNewDateTime());
            Doctor doctor = doctorService.findByUsername(username);
            if (doctor == null) {
                System.out.println("Doctor not found for username: " + username);
                return ResponseEntity.badRequest().build();
            }
            LocalDateTime newDateTime = LocalDateTime.parse(request.getNewDateTime(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Appointment updated = appointmentService.rescheduleAppointment(id, newDateTime);
            return ResponseEntity.ok(AppointmentDto.fromEntity(updated));
        } catch (Exception e) {
            System.out.println("Exception during reschedule: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public static class RescheduleRequest {
        private String newDateTime;

        public String getNewDateTime() {
            return newDateTime;
        }

        public void setNewDateTime(String newDateTime) {
            this.newDateTime = newDateTime;
        }
    }
}
