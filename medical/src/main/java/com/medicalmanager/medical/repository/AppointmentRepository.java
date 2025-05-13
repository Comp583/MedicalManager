package com.medicalmanager.medical.repository;

import com.medicalmanager.medical.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

        @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND " +
                        "((a.startTime <= :startTime AND a.endTime > :startTime) OR " +
                        "(a.startTime < :endTime AND a.endTime >= :endTime) OR " +
                        "(a.startTime >= :startTime AND a.endTime <= :endTime))")
        List<Appointment> findOverlappingAppointments(
                        @Param("doctorId") Long doctorId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        List<Appointment> findByPatientId(Long patientId);
}