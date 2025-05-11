package com.medicalmanager.medical.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.Patient;
import com.medicalmanager.medical.repository.AppointmentRepository;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.PatientRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public Appointment bookAppointment(Long doctorId, Long patientId, LocalDateTime dateTime,
            Integer duration, String appointmentType, String reasonForVisit) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid patient ID"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid doctor ID"));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDateTime(dateTime);
        appointment.setDuration(duration);
        appointment.setStatus("scheduled");
        appointment.setAppointmentType(appointmentType);
        appointment.setReasonForVisit(reasonForVisit);
        appointment.setNotes("");

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getDoctorAppointments(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                doctorId, startDate, endDate);
    }

    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByDateTimeAsc(patientId);
    }

    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatusOrderByDateTimeAsc(status);
    }

    public Appointment rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        Appointment originalAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        // Update original appointment status
        originalAppointment.setStatus("rescheduled");
        appointmentRepository.save(originalAppointment);

        // Create new appointment
        Appointment newAppointment = new Appointment();
        newAppointment.setDoctor(originalAppointment.getDoctor());
        newAppointment.setPatient(originalAppointment.getPatient());
        newAppointment.setDateTime(newDateTime);
        newAppointment.setDuration(originalAppointment.getDuration());
        newAppointment.setStatus("scheduled");
        newAppointment.setAppointmentType(originalAppointment.getAppointmentType());
        newAppointment.setReasonForVisit(originalAppointment.getReasonForVisit());
        newAppointment.setNotes(originalAppointment.getNotes());
        newAppointment.setPreviousAppointmentId(originalAppointment.getId());

        return appointmentRepository.save(newAppointment);
    }

    public Appointment cancelAppointment(Long appointmentId, String cancellationReason) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> {
                    appointment.setStatus("cancelled");
                    appointment.setNotes(cancellationReason);
                    return appointmentRepository.save(appointment);
                })
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
    }

    public Appointment completeAppointment(Long appointmentId, String notes) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> {
                    appointment.setStatus("completed");
                    appointment.setNotes(notes);
                    return appointmentRepository.save(appointment);
                })
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
    }

    public Map<String, Object> checkDoctorAvailability(Long doctorId, LocalDateTime proposedDateTime, int duration) {
        Map<String, Object> result = new HashMap<>();

        // Check if doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        // Check if proposed time is during doctor's working hours
        // (Assuming doctor entity has working hours fields)
        int proposedHour = proposedDateTime.getHour();
        if (proposedHour < doctor.getWorkdayStartHour() || proposedHour >= doctor.getWorkdayEndHour()) {
            result.put("available", false);
            result.put("reason", "Outside working hours");
            return result;
        }

        // Check for existing appointments that conflict with proposed time
        LocalDateTime endTime = proposedDateTime.plusMinutes(duration);
        List<Appointment> conflictingAppointments = appointmentRepository
                .findConflictingAppointments(doctorId, proposedDateTime, endTime,
                        Arrays.asList("scheduled", "rescheduled"));

        if (!conflictingAppointments.isEmpty()) {
            result.put("available", false);
            result.put("reason", "Time slot already booked");
            result.put("conflictingAppointments", conflictingAppointments);
            return result;
        }

        // Check for lunch breaks or other blocked times
        // (This would depend on your business logic)
        if (isDuringLunchBreak(proposedDateTime, duration, doctor)) {
            result.put("available", false);
            result.put("reason", "During lunch break");
            return result;
        }

        // If all checks pass
        result.put("available", true);
        return result;
    }

    private boolean isDuringLunchBreak(LocalDateTime proposedDateTime, int duration, Doctor doctor) {
        // Example implementation - adjust based on your requirements
        int lunchStart = 12; // 12 PM
        int lunchEnd = 13; // 1 PM
        int proposedHour = proposedDateTime.getHour();
        int proposedEndHour = proposedDateTime.plusMinutes(duration).getHour();

        return (proposedHour >= lunchStart && proposedHour < lunchEnd) ||
                (proposedEndHour > lunchStart && proposedEndHour <= lunchEnd) ||
                (proposedHour < lunchStart && proposedEndHour > lunchEnd);
    }

    public List<Appointment> getTodaysAppointments(Long doctorId) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);

        return appointmentRepository.findByDoctorIdAndDateTimeBetweenAndStatusInOrderByDateTimeAsc(
                doctorId, today, tomorrow, Arrays.asList("scheduled", "rescheduled"));
    }

    public Map<String, Object> findAvailableSlots(Long doctorId, LocalDate date, Integer appointmentDuration) {
        Map<String, Object> result = new HashMap<>();

        // Get doctor's working hours
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        int startHour = doctor.getWorkdayStartHour(); // e.g., 9 for 9 AM
        int endHour = doctor.getWorkdayEndHour(); // e.g., 17 for 5 PM

        // Get existing appointments for the day
        LocalDateTime dayStart = date.atTime(startHour, 0);
        LocalDateTime dayEnd = date.atTime(endHour, 0);
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndDateTimeBetweenAndStatusInOrderByDateTimeAsc(
                        doctorId, dayStart, dayEnd, Arrays.asList("scheduled", "rescheduled"));

        // Generate all possible slots (simplified - adjust based on your needs)
        List<String> allPossibleSlots = generateTimeSlots(startHour, endHour, appointmentDuration);
        List<String> availableSlots = new ArrayList<>(allPossibleSlots);

        // 4. Remove slots that are already booked
        for (Appointment appointment : existingAppointments) {
            LocalDateTime appointmentTime = appointment.getDateTime();
            String slotTime = appointmentTime.toLocalTime().toString();
            availableSlots.remove(slotTime);
        }

        // 5. Remove lunch break slots (if applicable)
        availableSlots.removeIf(slot -> {
            LocalTime slotTime = LocalTime.parse(slot);
            return slotTime.getHour() >= 12 && slotTime.getHour() < 13; // 12 PM - 1 PM
        });

        result.put("availableSlots", availableSlots);
        result.put("doctor", doctor);
        result.put("date", date);
        return result;
    }

    private List<String> generateTimeSlots(int startHour, int endHour, int durationMinutes) {
        List<String> slots = new ArrayList<>();
        LocalTime time = LocalTime.of(startHour, 0);
        LocalTime endTime = LocalTime.of(endHour, 0);

        while (time.plusMinutes(durationMinutes).isBefore(endTime) ||
                time.plusMinutes(durationMinutes).equals(endTime)) {
            slots.add(time.toString());
            time = time.plusMinutes(durationMinutes);
        }

        return slots;
    }
}