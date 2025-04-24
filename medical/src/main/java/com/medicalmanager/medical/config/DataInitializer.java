package com.medicalmanager.medical.config;

import com.medicalmanager.medical.entity.User;
import com.medicalmanager.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Creates test users if they don't exist
        if (userRepository.findByUsername("tempclient").isEmpty()) {
            User patient = new User();
            patient.setUsername("tempclient");
            patient.setPassword(passwordEncoder.encode("client123"));
            patient.setRole("PATIENT");
            userRepository.save(patient);
        }

        if (userRepository.findByUsername("tempdoctor").isEmpty()) {
            User doctor = new User();
            doctor.setUsername("tempdoctor");
            doctor.setPassword(passwordEncoder.encode("doctor123"));
            doctor.setRole("DOCTOR");
            userRepository.save(doctor);
        }

        if (userRepository.findByUsername("tempadmin").isEmpty()) {
            User admin = new User();
            admin.setUsername("tempadmin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }
}