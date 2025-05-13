package com.medicalmanager.medical.controller;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.medicalmanager.medical.dto.DoctorForm;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.LocalTimeRange;
import com.medicalmanager.medical.model.User;
import com.medicalmanager.medical.repository.DoctorRepository;
import com.medicalmanager.medical.repository.UserRepository;
import com.medicalmanager.medical.service.DoctorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DoctorRepository doctorRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final DoctorService doctorService;

    @Autowired
    public AdminController(DoctorRepository doctorRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder, DoctorService doctorService) {
        this.doctorRepo = doctorRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.doctorService = doctorService;
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
    public String showManageDoctors(Model model) {
        model.addAttribute("doctors", doctorRepo.findAll());
        model.addAttribute("editing", false);
        return "admin-managedrs";
    }

    @GetMapping("/managedrs/edit/{id}")
    public String editDoctorForm(@PathVariable Long id, Model model) {
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        DoctorForm form = DoctorForm.fromDoctor(d);
        model.addAttribute("doctorForm", form);
        model.addAttribute("editing", true);
        model.addAttribute("doctors", doctorRepo.findAll());
        return "admin-managedrs";
    }

    @GetMapping("/managedrs/{id}/json")
    @ResponseBody
    public DoctorForm doctorJson(@PathVariable Long id) {
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return DoctorForm.fromDoctor(d);
    }

    @PutMapping("/managedrs/edit/{id}")
    public String updateDoctor(
            @PathVariable Long id,
            @ModelAttribute("doctorForm") @Valid DoctorForm form,
            BindingResult br,
            Model model) {
        if (br.hasErrors()) {
            model.addAttribute("doctors", doctorRepo.findAll());
            model.addAttribute("editing", true);
            return "admin-managedrs";
        }
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        form.updateDoctor(d, passwordEncoder);
        doctorRepo.save(d);
        // if the password was changed, also update userRepo here
        return "redirect:/admin/managedrs";
    }

    @DeleteMapping("/managedrs/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctorAndUser(id);
        return "redirect:/admin/managedrs";
    }

    @PostMapping("/managedrs/add")
    public String addDoctor(
            @ModelAttribute("doctorForm") @Valid DoctorForm form,
            BindingResult br,
            Model model) {
        if (br.hasErrors()) {
            model.addAttribute("doctors", doctorRepo.findAll());
            model.addAttribute("editing", false);
            return "admin-managedrs";
        }

        Doctor d = form.toDoctor(passwordEncoder);
        d.getRoles().add("ROLE_DOCTOR");
        form.getAvailabilities().stream()
                .filter(av -> Boolean.FALSE.equals(av.getOff()))
                .forEach(av -> d.getAvailability().put(av.getDay(),
                        LocalTimeRange.of(
                                LocalTime.parse(av.getStartTime()), // Convert String to LocalTime
                                LocalTime.parse(av.getEndTime()) // Convert String to LocalTime
                        )));
        doctorRepo.save(d);

        User u = new User();
        u.setUsername(d.getUsername());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setRole("DOCTOR");
        userRepo.save(u);

        return "redirect:/admin/managedrs";
    }
}