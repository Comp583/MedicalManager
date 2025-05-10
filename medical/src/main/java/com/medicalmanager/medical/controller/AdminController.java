package com.medicalmanager.medical.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalmanager.medical.dto.DoctorForm;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.LocalTimeRange;
import com.medicalmanager.medical.model.User;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DoctorRepository doctorRepo;
    private final UserRepository   userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(DoctorRepository doctorRepo,
                           UserRepository   userRepo,
                           PasswordEncoder passwordEncoder) {
        this.doctorRepo      = doctorRepo;
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute("doctorForm")
    public DoctorForm doctorForm() {
        return new DoctorForm();
    }

    @GetMapping("/landing")
    public String adminLanding() {
        return "admin-landing";
    }

    @GetMapping("/avail")
    public String adminAvail() {
        return "admin-avail";
    }

    @GetMapping("/managedrs")
    public String showManageDoctors(Model m) {
        m.addAttribute("doctors", doctorRepo.findAll());
        return "admin-managedrs";
    }

    @PostMapping("/managedrs/add")
    public String addDoctor(
        @ModelAttribute("doctorForm") @Valid DoctorForm form,
        BindingResult br,
        Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("doctors", doctorRepo.findAll());
            return "admin-managedrs";
        }

        // 1) create & save Doctor
        Doctor d = form.toDoctor(passwordEncoder);
        d.getRoles().add("ROLE_DOCTOR");
        form.getAvailabilities().stream()
            .filter(av -> Boolean.FALSE.equals(av.getOff()))
            .forEach(av -> d.getAvailability()
                            .put(av.getDay(),
                                LocalTimeRange.of(av.getStart(), av.getEnd())));
        doctorRepo.save(d);

        // 2) create & save User for Spring Security
        User u = new User();
        u.setUsername(d.getUsername());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setRole("DOCTOR");
        userRepo.save(u);

        return "redirect:/admin/managedrs";
    }
}
