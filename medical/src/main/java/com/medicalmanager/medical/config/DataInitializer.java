package com.medicalmanager.medical.config;

import com.medicalmanager.medical.model.User;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.AvailableSlot;
import com.medicalmanager.medical.repository.UserRepository;
import com.medicalmanager.medical.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

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

        /*if (userRepository.findByUsername("tempdoctor").isEmpty()) {
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
        }*/
        ////Only ever will be us admins
        if (userRepository.findByUsername("nic_admin").isEmpty()) {
            User nicAdmin = new User();
            nicAdmin.setUsername("nic_admin");
            nicAdmin.setPassword(passwordEncoder.encode("Nic123!"));
            nicAdmin.setRole("ADMIN");
            userRepository.save(nicAdmin);
        }

        if (userRepository.findByUsername("jess_admin").isEmpty()) {
            User jessAdmin = new User();
            jessAdmin.setUsername("jess_admin");
            jessAdmin.setPassword(passwordEncoder.encode("Jess123!"));
            jessAdmin.setRole("ADMIN");
            userRepository.save(jessAdmin);
        }

        if (userRepository.findByUsername("angel_admin").isEmpty()) {
            User angelAdmin = new User();
            angelAdmin.setUsername("angel_admin");
            angelAdmin.setPassword(passwordEncoder.encode("Angel123!"));
            angelAdmin.setRole("ADMIN");
            userRepository.save(angelAdmin);
        }
        ////Potential to add more doctors later

        // Insert sample doctors from DB_schema.js if not present
        insertDoctorIfNotExists("dr.smith", "John", "Smith",
        "Dr. Smith is a comprehensive ophthalmologist with over 15 years of experience in cataract surgery and general eye care.",
        new String[][]{
                {"MONDAY", "09:00", "17:00"},
                {"WEDNESDAY", "09:00", "17:00"},
                {"FRIDAY", "09:00", "15:00"}
        }, "doctor123");

        // Create User entity for dr.smith
        if (userRepository.findByUsername("dr.smith").isEmpty()) {
            User drSmithUser = new User();
            drSmithUser.setUsername("dr.smith");
            drSmithUser.setPassword(passwordEncoder.encode("doctor123"));
            drSmithUser.setRole("DOCTOR");
            userRepository.save(drSmithUser);
        }

        insertDoctorIfNotExists("dr.patel", "Riya", "Patel",
        "Dr. Patel is a pediatric optometrist focusing on children's vision, including lazy eye treatment and vision therapy.",
        new String[][]{
                {"TUESDAY", "08:00", "16:00"},
                {"THURSDAY", "08:00", "16:00"},
                {"SATURDAY", "10:00", "14:00"}
        }, "doctor123");        

        // Create User entity for dr.patel
        if (userRepository.findByUsername("dr.patel").isEmpty()) {
            User drPatelUser = new User();
            drPatelUser.setUsername("dr.patel");
            drPatelUser.setPassword(passwordEncoder.encode("doctor123"));
            drPatelUser.setRole("DOCTOR");
            userRepository.save(drPatelUser);
        }

        insertDoctorIfNotExists("dr.johnson", "Michael", "Johnson",
         "Dr. Johnson is a retinal specialist treating complex retinal diseases such as macular degeneration and diabetic retinopathy.",
         new String[][]{
                {"MONDAY", "10:00", "18:00"},
                {"THURSDAY", "10:00", "18:00"},
                {"FRIDAY", "10:00", "16:00"}
        }, "doctor123");

        // Create User entity for dr.johnson
        if (userRepository.findByUsername("dr.johnson").isEmpty()) {
            User drJohnsonUser = new User();
            drJohnsonUser.setUsername("dr.johnson");
            drJohnsonUser.setPassword(passwordEncoder.encode("doctor123"));
            drJohnsonUser.setRole("DOCTOR");
            userRepository.save(drJohnsonUser);
        }

        insertDoctorIfNotExists("dr.garcia", "Elena", "Garcia",
        "Dr. Garcia is a glaucoma specialist with advanced training in laser and surgical treatments for glaucoma management.",
        new String[][]{
                {"TUESDAY", "09:00", "17:00"},
                {"WEDNESDAY", "09:00", "17:00"},
                {"FRIDAY", "09:00", "15:00"}
        }, "doctor123");

        // Create User entity for dr.garcia
        if (userRepository.findByUsername("dr.garcia").isEmpty()) {
            User drGarciaUser = new User();
            drGarciaUser.setUsername("dr.garcia");
            drGarciaUser.setPassword(passwordEncoder.encode("doctor123"));
            drGarciaUser.setRole("DOCTOR");
            userRepository.save(drGarciaUser);
        }

    }

    private void insertDoctorIfNotExists(String username, String firstName, String lastName, String biography, String[][] slots, String rawPassword) {
        Optional<Doctor> existingDoctor = doctorRepository.findByUsername(username);
        if (existingDoctor.isEmpty()) {
            Doctor doctor = new Doctor();
            doctor.setUsername(username);
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setBiography(biography);
            Set<String> roles = new HashSet<>();
            roles.add("DOCTOR");
            doctor.setRoles(roles);
            doctor.setPasswordHash(passwordEncoder.encode(rawPassword));

            for (String[] slot : slots) {
                AvailableSlot availableSlot = new AvailableSlot();
                availableSlot.setDoctor(doctor);
                availableSlot.setDayOfWeek(DayOfWeek.valueOf(slot[0]));
                availableSlot.setStartTime(LocalTime.parse(slot[1]));
                availableSlot.setEndTime(LocalTime.parse(slot[2]));
                doctor.getAvailableSlots().add(availableSlot);
            }

            doctorRepository.save(doctor);
        }
    }
}
