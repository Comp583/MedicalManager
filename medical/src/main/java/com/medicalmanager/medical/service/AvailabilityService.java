//Calculates available slots based on doctors schedules
package com.medicalmanager.medical.service;

import com.medicalmanager.medical.dto.AvailabilityDto;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.model.AvailableSlot;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.repository.AppointmentRepository;
import com.medicalmanager.medical.repository.DoctorRepository;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_SLOT_DURATION = 15; // Minimum appointment duration in minutes

    public AvailabilityService(DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<AvailabilityDto> getAvailableSlots(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);
        if (doctorOptional.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found with ID: " + doctorId);
        }

        Doctor doctor = doctorOptional.get();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        List<AvailableSlot> availableSlots = doctor.getAvailableSlots().stream()
                .filter(slot -> slot.getDayOfWeek() == dayOfWeek && slot.getIsActive())
                .collect(Collectors.toList());

        if (availableSlots.isEmpty()) {
            throw new IllegalStateException("No availability configured for doctor on " + dayOfWeek);
        }

        // Get all appointments for the day in one query
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);

        List<AvailabilityDto> result = new ArrayList<>();

        for (AvailableSlot slot : availableSlots) {
            validateSlotDuration(slot.getSlotDurationMinutes());

            LocalTime currentTime = slot.getStartTime();
            LocalTime endTime = slot.getEndTime();
            int slotDuration = slot.getSlotDurationMinutes();

            while (currentTime.plusMinutes(slotDuration).compareTo(endTime) <= 0) {
                LocalTime slotEnd = currentTime.plusMinutes(slotDuration);

                if (isSlotAvailable(currentTime, slotEnd, appointments)) {
                    result.add(createAvailabilityDto(currentTime, slotEnd));
                }
                currentTime = currentTime.plusMinutes(slotDuration);
            }
        }

        return result;
    }

    public boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingAppointments(
                doctorId,
                date,
                startTime,
                endTime);

        return overlappingAppointments.isEmpty();
    }

    private boolean isSlotAvailable(LocalTime startTime, LocalTime endTime, List<Appointment> appointments) {
        return appointments.stream()
                .noneMatch(appt -> startTime.isBefore(appt.getEndTime()) && endTime.isAfter(appt.getStartTime()));
    }

    private AvailabilityDto createAvailabilityDto(LocalTime startTime, LocalTime endTime) {
        return new AvailabilityDto(
                startTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER));
    }

    private void validateSlotDuration(int duration) {
        if (duration < MIN_SLOT_DURATION) {
            throw new IllegalStateException("Slot duration must be at least " + MIN_SLOT_DURATION + " minutes");
        }
    }
}