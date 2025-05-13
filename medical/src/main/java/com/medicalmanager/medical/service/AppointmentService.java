//Handles booking logic and appointment management
package com.medicalmanager.medical.service;

import com.medicalmanager.medical.dto.AppointmentRequest;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.Patient;
import com.medicalmanager.medical.model.User;
import com.medicalmanager.medical.repository.AppointmentRepository;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.PatientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserDetailsServiceImpl userService;

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            UserDetailsServiceImpl userService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.userService = userService;
    }

    @Transactional
    public Appointment bookAppointment(AppointmentRequest appointmentRequest) {
        // Get current authenticated user
        User currentUser = userService.getCurrentUser();

        // Get patient by user id
        Optional<Patient> patientOptional = patientRepository.findByUserId(currentUser.getId());
        if (patientOptional.isEmpty()) {
            throw new RuntimeException("Patient not found for current user");
        }

        // Get doctor
        Optional<Doctor> doctorOptional = doctorRepository.findById(appointmentRequest.getDoctorId());
        if (doctorOptional.isEmpty()) {
            throw new RuntimeException("Doctor not found");
        }

        // Calculate end time (assuming 30-minute appointments)
        LocalTime startTime = appointmentRequest.getAppointmentTime();
        LocalTime endTime = startTime.plusMinutes(30);

        // Check for overlapping appointments
        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingAppointments(
                appointmentRequest.getDoctorId(),
                appointmentRequest.getAppointmentDate(),
                startTime,
                endTime);

        if (!overlappingAppointments.isEmpty()) {
            throw new RuntimeException("Selected time slot is no longer available");
        }

        // Create new appointment
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctorOptional.get());
        appointment.setPatient(patientOptional.get());
        appointment.setAppointmentDate(appointmentRequest.getAppointmentDate());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setAppointmentType(appointmentRequest.getAppointmentType());
        appointment.setReasonForVisit(appointmentRequest.getReasonForVisit());
        appointment.setStatus("SCHEDULED");

        // Save appointment
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getPatientAppointments() {
        User currentUser = userService.getCurrentUser();
        Optional<Patient> patientOptional = patientRepository.findByUserId(currentUser.getId());

        if (patientOptional.isEmpty()) {
            throw new RuntimeException("Patient not found for current user");
        }

        return appointmentRepository.findByPatientId(patientOptional.get().getId());
    }
}