package com.medicalmanager.medical.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalmanager.medical.model.Patient;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.service.PatientService;
import com.medicalmanager.medical.service.AppointmentService;
import com.medicalmanager.medical.model.Appointment;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final DoctorRepository doctorRepository;
    private final AppointmentService appointmentService;

    public PatientController(PatientService patientService, DoctorRepository doctorRepository, AppointmentService appointmentService) {
        this.patientService = patientService;
        this.doctorRepository = doctorRepository;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/landing")
    public String patientLanding(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<Patient> patientOpt = patientService.getPatientByUsername(username);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            List<Appointment> appointments = appointmentService.getPatientAppointments(patient.getId());
            model.addAttribute("appointments", appointments);
        } else {
            model.addAttribute("appointments", List.of());
        }
        return "patient-landing";
    }

@GetMapping("/booking")
public String patientBooking(Model model) {
    List<Doctor> doctors = doctorRepository.findAll();
    model.addAttribute("doctors", doctors);
    return "patient-booking";
}

    @GetMapping("/notifications")
    public String patientNotifications() {
        return "patient-notifications";
    }

    @GetMapping("/manage")
    public String patientDrView(Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);
        return "patient-doctorview";
    }

    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        try {
            Patient newPatient = patientService.createPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPatient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Patient> getPatientByUsername(@PathVariable String username) {
        return patientService.getPatientByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            Patient updatedPatient = patientService.updatePatientInfo(id, patient);
            return ResponseEntity.ok(updatedPatient);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

    }
}
