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

        // Basic query methods
        List<Appointment> findByDoctorId(Long doctorId);

        List<Appointment> findByPatientId(Long patientId);

        List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

        // Date range queries
        List<Appointment> findByDoctorIdAndAppointmentDateBetween(
                        Long doctorId,
                        LocalDate startDate,
                        LocalDate endDate);

        List<Appointment> findByPatientIdAndAppointmentDateBetween(
                        Long patientId,
                        LocalDate startDate,
                        LocalDate endDate);

        // Overlapping appointments query (improved version)
        @Query("SELECT a FROM Appointment a WHERE " +
                        "a.doctor.id = :doctorId AND " +
                        "a.appointmentDate = :date AND " +
                        "NOT (a.endTime <= :startTime OR a.startTime >= :endTime)")
        List<Appointment> findOverlappingAppointments(
                        @Param("doctorId") Long doctorId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // Additional useful queries
        List<Appointment> findByStatus(String status);

        List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);

        List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

        // For calendar views
        @Query("SELECT a FROM Appointment a WHERE " +
                        "a.doctor.id = :doctorId AND " +
                        "a.appointmentDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY a.appointmentDate, a.startTime")
        List<Appointment> findDoctorAppointmentsInRange(
                        @Param("doctorId") Long doctorId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // For checking specific time slot availability
        @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a WHERE " +
                        "a.doctor.id = :doctorId AND " +
                        "a.appointmentDate = :date AND " +
                        "NOT (a.endTime <= :startTime OR a.startTime >= :endTime)")
        boolean existsOverlappingAppointment(
                        @Param("doctorId") Long doctorId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);
}