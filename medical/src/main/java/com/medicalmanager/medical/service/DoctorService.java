package com.medicalmanager.medical.service;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.UserRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {
  private final DoctorRepository doctorRepo;
  private final UserRepository   userRepo;

  @Autowired
  public DoctorService(DoctorRepository dr, UserRepository ur) {
    this.doctorRepo = dr;
    this.userRepo   = ur;
  }

  @Transactional
  public void deleteDoctorAndUser(Long id) {
    Doctor d = doctorRepo.findById(id)
                 .orElseThrow(() -> new NoSuchElementException("No doctor "+id));
    doctorRepo.delete(d);
    userRepo.deleteByUsername(d.getUsername());
  }

  public List<Doctor> findDoctorsByDay(DayOfWeek day) {
    List<Doctor> allDoctors = doctorRepo.findAll();
    return allDoctors.stream()
      .filter(d -> d.getAvailableSlots() != null && d.getAvailableSlots().stream()
        .anyMatch(slot -> slot.getDayOfWeek() == day &&
          slot.getStartTime() != null &&
          slot.getEndTime() != null &&
          slot.getStartTime().isBefore(slot.getEndTime())
        )
      )
      .collect(Collectors.toList());
  }

  public List<String> getAvailableHourlySlots(Long doctorId, java.time.LocalDate date) {
    Doctor doctor = doctorRepo.findById(doctorId)
      .orElseThrow(() -> new NoSuchElementException("No doctor found with id " + doctorId));

    java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();

    List<String> hourlySlots = new java.util.ArrayList<>();

    if (doctor.getAvailableSlots() == null) {
      return hourlySlots;
    }

    doctor.getAvailableSlots().stream()
      .filter(slot -> slot.getDayOfWeek() == dayOfWeek)
      .forEach(slot -> {
        java.time.LocalTime start = slot.getStartTime();
        java.time.LocalTime end = slot.getEndTime();

        while (!start.isAfter(end.minusHours(1))) {
          hourlySlots.add(start.toString());
          start = start.plusHours(1);
        }
      });

    return hourlySlots;
  }
}
