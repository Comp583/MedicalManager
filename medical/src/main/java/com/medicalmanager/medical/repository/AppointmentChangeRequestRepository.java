package com.medicalmanager.medical.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicalmanager.medical.model.AppointmentChangeRequest;

@Repository
public interface AppointmentChangeRequestRepository extends JpaRepository<AppointmentChangeRequest, Long> {
    List<AppointmentChangeRequest> findByAppointmentPatientIdAndStatus(Long patientId, String status);
}
