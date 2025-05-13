//MVC Controller for page navigation
package com.medicalmanager.medical.controller;

import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.Patient;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.service.PatientService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final DoctorRepository doctorRepository;

    @Autowired
    public PatientController(PatientService patientService, DoctorRepository doctorRepository) {
        this.patientService = patientService;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/landing")
    public String patientLanding() {
        return "patient/landing";
    }

    @GetMapping("/booking")
    public String bookingPage(Model model) {
        // gets all active doctors
        List<Doctor> doctors = doctorRepository.findByIsActive(true);
        model.addAttribute("doctors", doctors);
        return "patient/booking";
    }

    @GetMapping("/notification")
    public String notificationsPage() {
        return "patient/notifications";
    }

    @GetMapping("/manage")
    public ResponseEntity<?> managePatient(@PathVariable Long id) {
        try {
            Patient patient = patientService.getPatientById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
            return ResponseEntity.ok(patient);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
