package com.medicalmanager.medical.repository;

import java.util.Optional;

import com.medicalmanager.medical.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUsername(String username);

    Optional<Patient> findByUserId(Long userId);
}
