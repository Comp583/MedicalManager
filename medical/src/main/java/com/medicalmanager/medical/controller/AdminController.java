package com.medicalmanager.medical.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/landing")
    public String adminLanding() {
        return "admin-landing";
    }

    @GetMapping("/avail")
    public String adminAvail() {
        return "admin-avail";
    }

    @GetMapping("/manage")
    public String adminManage() {
        return "admin-manage";
    }
}