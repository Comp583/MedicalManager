//MVC controller for page navigation
package com.medicalmanager.medical.controller;

import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/patient")
public class PatientControler {

    private final DoctorRepository doctorRepository;

    @Autowired
    public PatientController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/landing")
    public String patientLanding() {
        return "patient/landing";
    }

    @GetMapping("/booking")
    public String bookingPage(Model model) {
        // gets all active doctors
        List<Doctor> doctors = doctorRespository.findByIsActive(true);
        model.addAttribute("doctors", doctors);
        return "patient/booking";
    }

    @GetMapping("/notification")
    public String notificationsPage() {
        return "patient/notifications";
    }

    @GetMapping("/manage")
    public String manageDoctor() {
        return "patient/manage";
    }
}
