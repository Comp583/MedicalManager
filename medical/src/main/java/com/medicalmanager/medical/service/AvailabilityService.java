//Calculates available slots based on doctors schedules
package com.medicalmanager.medical.service;

import com.medicalmanager.medical.dto.AvailabilityDto;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.model.AvailableSlot;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.repository.AppointmentRepository;
import com.medicalmanager.medical.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public AvailabilityService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<AvailabilityDto> getAvailableSlots(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);
        if (doctorOptional.isEmpty()) {
            throw new RuntimeException("Doctor not found");
        }

        Doctor doctor = doctorOptional.get();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayValue = dayOfWeek.getValue(); // 1 for Monday, 7 for Sunday

        // Get all available slots for the doctor on this day of week
        List<AvailableSlot> availableSlots = doctor.getAvailableSlots().stream()
                .filter(slot -> slot.getDayOfWeek() == dayValue && slot.getIsActive())
                .collect(Collectors.toList());

        // Get all appointments for the doctor on this date
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);

        List<AvailabilityDto> result = new ArrayList<>();

        // For each available slot, generate time slots based on duration
        for (AvailableSlot slot : availableSlots) {
            LocalTime currentTime = slot.getStartTime();
            LocalTime endTime = slot.getEndTime();
            int slotDuration = slot.getSlotDurationMinutes();

            while (currentTime.plusMinutes(slotDuration).compareTo(endTime) <= 0) {
                LocalTime slotEnd = currentTime.plusMinutes(slotDuration);

                // Check if this slot overlaps with any appointment
                boolean isAvailable = appointments.stream()
                        .noneMatch(appointment -> (currentTime.compareTo(appointment.getStartTime()) <= 0
                                && slotEnd.compareTo(appointment.getStartTime()) > 0) ||
                                (currentTime.compareTo(appointment.getEndTime()) < 0
                                        && slotEnd.compareTo(appointment.getEndTime()) >= 0)
                                ||
                                (currentTime.compareTo(appointment.getStartTime()) >= 0
                                        && slotEnd.compareTo(appointment.getEndTime()) <= 0));

                // If slot is available, add to results
                if (isAvailable) {
                    String startTimeStr = currentTime.format(TIME_FORMATTER);
                    String endTimeStr = slotEnd.format(TIME_FORMATTER);
                    result.add(new AvailabilityDto(startTimeStr, endTimeStr));
                }

                // Move to next slot
                currentTime = currentTime.plusMinutes(slotDuration);
            }
        }

        return result;
    }
}