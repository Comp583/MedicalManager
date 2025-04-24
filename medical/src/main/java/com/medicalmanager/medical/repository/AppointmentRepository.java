package com.medicalmanager.medical.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medicalmanager.medical.model.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
        List<Appointment> findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                        Long doctorId, LocalDateTime startDate, LocalDateTime endDate);

        List<Appointment> findByPatientIdOrderByDateTimeAsc(Long patientId);

        List<Appointment> findByStatusOrderByDateTimeAsc(String status);

        List<Appointment> findByDoctorIdAndDateTimeBetweenAndStatusInOrderByDateTimeAsc(
                        Long doctorId, LocalDateTime startDate, LocalDateTime endDate, List<String> statuses);

        @Query("SELECT a FROM Appointment a WHERE " +
                        "a.doctor.id = :doctorId AND " +
                        "a.status IN :statuses AND " +
                        "(" +
                        "  (a.dateTime < :endTime AND a.dateTime >= :startTime) OR " +
                        "  (FUNCTION('TIMESTAMPADD', MINUTE, a.duration, a.dateTime) > :startTime AND " +
                        "   FUNCTION('TIMESTAMPADD', MINUTE, a.duration, a.dateTime) <= :endTime) OR " +
                        "  (a.dateTime <= :startTime AND " +
                        "   FUNCTION('TIMESTAMPADD', MINUTE, a.duration, a.dateTime) >= :endTime)" +
                        ") ORDER BY a.dateTime ASC")
        List<Appointment> findConflictingAppointments(
                        @Param("doctorId") Long doctorId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("statuses") List<String> statuses);
}