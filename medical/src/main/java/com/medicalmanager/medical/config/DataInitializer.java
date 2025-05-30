package com.medicalmanager.medical.config;

import com.medicalmanager.medical.model.User;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.AvailableSlot;
import com.medicalmanager.medical.model.Patient;
import com.medicalmanager.medical.model.Appointment;
import com.medicalmanager.medical.repository.UserRepository;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.PatientRepository;
import com.medicalmanager.medical.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Creates test users if they don't exist
        if (userRepository.findByUsername("Emma Chavez").isEmpty()) {
            User patient = new User();
            patient.setUsername("Emma Chavez");
            patient.setPassword(passwordEncoder.encode("client123"));
            patient.setRole("PATIENT");
            userRepository.save(patient);
        }

        // Create Patient entity for tempclient if not exists
        if (patientRepository.findByUsername("Emma Chavez").isEmpty()) {
            Patient patientEntity = new Patient();
            patientEntity.setUsername("Emma Chavez");
            patientEntity.setFirstName("Emma");
            patientEntity.setLastName("Chavez");
            patientEntity.setCreatedAt(LocalDateTime.now());
            patientRepository.save(patientEntity);
        }

        //// Only ever will be us admins
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
        //// Potential to add more doctors later

        // Insert sample doctors from DB_schema.js if not present
        insertDoctorIfNotExists("dr.smith", "John", "Smith",
                "Dr. Smith is a comprehensive ophthalmologist with over 15 years of experience in cataract surgery and general eye care.",
                new String[][] {
                        { "MONDAY", "09:00", "17:00" },
                        { "WEDNESDAY", "09:00", "17:00" },
                        { "FRIDAY", "09:00", "15:00" }
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
                new String[][] {
                        { "TUESDAY", "08:00", "16:00" },
                        { "THURSDAY", "08:00", "16:00" },
                        { "SATURDAY", "10:00", "14:00" }
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
                new String[][] {
                        { "MONDAY", "10:00", "18:00" },
                        { "THURSDAY", "10:00", "18:00" },
                        { "FRIDAY", "10:00", "16:00" }
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
                new String[][] {
                        { "TUESDAY", "09:00", "17:00" },
                        { "WEDNESDAY", "09:00", "17:00" },
                        { "FRIDAY", "09:00", "15:00" }
                }, "doctor123");

        // Create User entity for dr.garcia
        if (userRepository.findByUsername("dr.garcia").isEmpty()) {
            User drGarciaUser = new User();
            drGarciaUser.setUsername("dr.garcia");
            drGarciaUser.setPassword(passwordEncoder.encode("doctor123"));
            drGarciaUser.setRole("DOCTOR");
            userRepository.save(drGarciaUser);
        }

        // Create dummy patients if not exist
        createDummyPatient("John Doe");
        createDummyPatient("Jane Smith");
        createDummyPatient("Robert Johnson");

        // Create dummy appointments if not exist
        createDummyAppointment("dr.smith", "John Doe", LocalDateTime.now().plusDays(1).withHour(14).withMinute(30));
        createDummyAppointment("dr.smith", "Jane Smith", LocalDateTime.now().plusDays(2).withHour(15).withMinute(0));
        createDummyAppointment("dr.smith", "Robert Johnson",
                LocalDateTime.now().plusDays(3).withHour(15).withMinute(15));
    }

    private void createDummyPatient(String fullName) {
        if (patientRepository.findByUsername(fullName).isEmpty()) {
            Patient patient = new Patient();
            patient.setUsername(fullName);
            String[] names = fullName.split(" ");
            if (names.length >= 2) {
                patient.setFirstName(names[0]);
                patient.setLastName(names[1]);
            } else {
                patient.setFirstName(fullName);
                patient.setLastName("");
            }
            patient.setCreatedAt(LocalDateTime.now());
            patientRepository.save(patient);
        }
    }

    private void createDummyAppointment(String doctorUsername, String patientUsername, LocalDateTime dateTime) {
        var doctorOpt = doctorRepository.findByUsername(doctorUsername);
        var patientOpt = patientRepository.findByUsername(patientUsername);
        if (doctorOpt.isPresent() && patientOpt.isPresent()) {
            var doctor = doctorOpt.get();
            var patient = patientOpt.get();
            // Fetch all appointments for doctor and patient and check if any match the
            // dateTime
            List<Appointment> existingAppointments = appointmentRepository
                    .findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                            doctor.getId(), dateTime.minusMinutes(1), dateTime.plusMinutes(1));
            boolean exists = existingAppointments.stream()
                    .anyMatch(a -> a.getPatient().getId().equals(patient.getId()) && a.getDateTime().equals(dateTime));
            if (!exists) {
                Appointment appointment = new Appointment();
                appointment.setDoctor(doctor);
                appointment.setPatient(patient);
                appointment.setDateTime(dateTime);
                appointment.setDuration(30);
                appointment.setStatus("scheduled");
                appointment.setAppointmentType("General");
                appointment.setReasonForVisit("Routine checkup");
                appointment.setNotes("");
                appointmentRepository.save(appointment);
            }
        }
    }

    private void insertDoctorIfNotExists(String username, String firstName, String lastName, String biography,
            String[][] slots, String rawPassword) {
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
