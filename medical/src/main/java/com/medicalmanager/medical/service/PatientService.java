package com.medicalmanager.medical.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.medicalmanager.medical.model.Patient;
import com.medicalmanager.medical.repository.PatientRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient createPatient(Patient patientData) {
        patientData.setCreatedAt(LocalDateTime.now());
        return patientRepository.save(patientData);
    }

    public Optional<Patient> getPatientByUsername(String username) {
        return patientRepository.findByUsername(username);
    }

    public Optional<Patient> getPatientById(Long patientId) {
        return patientRepository.findById(patientId);
    }

    public Patient updatePatientInfo(Long patientId, Patient updateData) {
        return patientRepository.findById(patientId)
                .map(patient -> {
                    // Update patient fields here
                    if (updateData.getFirstName() != null) {
                        patient.setFirstName(updateData.getFirstName());
                    }
                    if (updateData.getLastName() != null) {
                        patient.setLastName(updateData.getLastName());
                    }
                    // Update other fields as needed
                    return patientRepository.save(patient);
                })
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
    }
}