package com.medicalmanager.medical.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @GetMapping("/landing")
    public String doctorLanding() {
        return "doctor-landing";
    }

    @GetMapping("/avail")
    public String doctorAvail() {
        return "doctor-avail";
    }

    @GetMapping("/manage")
    public String doctorManage() {
        return "doctor-manage";
    }
}