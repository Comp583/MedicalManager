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
}
