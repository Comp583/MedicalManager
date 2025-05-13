package com.medicalmanager.medical.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.UserRepository;

@Service
public class DoctorService {
  private final DoctorRepository doctorRepo;
  private final UserRepository userRepo;

  public DoctorService(DoctorRepository dr, UserRepository ur) {
    this.doctorRepo = dr;
    this.userRepo = ur;
  }

  @Transactional
  public void deleteDoctorAndUser(Long id) {
    Doctor d = doctorRepo.findById(id)
        .orElseThrow(() -> new NoSuchElementException("No doctor " + id));
    doctorRepo.delete(d);
    userRepo.deleteByUsername(d.getUsername());
  }
}