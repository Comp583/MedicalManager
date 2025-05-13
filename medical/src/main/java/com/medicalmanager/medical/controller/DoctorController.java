

package com.medicalmanager.medical.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.service.DoctorService;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/landing")
    public String doctorLanding() {
        return "doctor-landing";
    }

    @GetMapping("/avail")
    public String doctorAvail(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Doctor doctor = doctorService.findByUsername(username);
        Set<DayOfWeek> availableDays = EnumSet.noneOf(DayOfWeek.class);
        java.util.Map<DayOfWeek, java.util.List<String>> scheduleMap = new java.util.HashMap<>();

        if (doctor != null && doctor.getAvailableSlots() != null) {
            availableDays = doctor.getAvailableSlots().stream()
                .map(slot -> slot.getDayOfWeek())
                .collect(Collectors.toSet());

            for (var slot : doctor.getAvailableSlots()) {
                DayOfWeek day = slot.getDayOfWeek();
                String timeRange = slot.getStartTime().toString() + "-" + slot.getEndTime().toString();
                scheduleMap.computeIfAbsent(day, k -> new java.util.ArrayList<>()).add(timeRange);
            }
        }

        java.util.List<com.medicalmanager.medical.model.DayOffRequest> dayOffRequests = doctorService.getDayOffRequestsByUsername(username);
        model.addAttribute("dayOffRequests", dayOffRequests);

        System.out.println("Doctor: " + doctor);
        System.out.println("Available days: " + availableDays);
        System.out.println("Schedule map: " + scheduleMap);
        System.out.println("Day off requests: " + dayOffRequests);

        model.addAttribute("availableDays", availableDays);
        model.addAttribute("scheduleMap", scheduleMap);
        return "doctor-avail";
    }

    @PostMapping("/avail/dayoff")
    public String requestDayOff(@org.springframework.web.bind.annotation.RequestParam("dayOffDate") String dayOffDate, Model model) {
        System.out.println("Received day off request for date: " + dayOffDate);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        doctorService.requestDayOff(username, dayOffDate);

        model.addAttribute("message", "Day off requested for " + dayOffDate);
        return doctorAvail(model);
    }

    @GetMapping("/manage")
    public String doctorManage() {
        return "doctor-manage";
    }
}
