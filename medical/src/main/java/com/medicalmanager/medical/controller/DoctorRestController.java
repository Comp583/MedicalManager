package com.medicalmanager.medical.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.service.DoctorService;

@RestController
public class DoctorRestController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorRestController.class);

    private final DoctorService doctorService;

    @Autowired
    public DoctorRestController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/api/doctors")
    public List<Doctor> getDoctorsByDate(
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            // Return all doctors if no date provided
            List<Doctor> allDoctors = doctorService.findDoctorsByDay(null);
            logger.info("No date provided, returning all doctors. Count: {}", allDoctors.size());
            return allDoctors;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Doctor> doctors = doctorService.findDoctorsByDay(dayOfWeek);
        logger.info("Doctors available on {} ({}): {}", date, dayOfWeek, doctors.size());
        return doctors;
    }
}
