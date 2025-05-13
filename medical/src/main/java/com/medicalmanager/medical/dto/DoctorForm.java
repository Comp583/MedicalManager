package com.medicalmanager.medical.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.medicalmanager.medical.model.Doctor;
import com.medicalmanager.medical.model.LocalTimeRange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DoctorForm {
  private Long id;
  @NotBlank
  private String firstName;
  @NotBlank
  private String lastName;
  @NotBlank
  private String username;
  @NotBlank
  @Size(min = 8)
  private String password;
  @NotBlank
  private String biography;

  private List<AvailabilityDto> availabilities = new ArrayList<>();

  public DoctorForm() {
    for (DayOfWeek d : DayOfWeek.values()) {
      AvailabilityDto a = new AvailabilityDto();
      a.setDay(d);
      availabilities.add(a);
    }
  }

  public Doctor toDoctor(PasswordEncoder encoder) {
    Doctor d = new Doctor();
    d.setFirstName(firstName);
    d.setLastName(lastName);
    d.setUsername(username);
    d.setPasswordHash(encoder.encode(password));
    d.setBiography(biography);
    return d;
  }

  public static DoctorForm fromDoctor(Doctor d) {
    DoctorForm f = new DoctorForm();
    f.setId(d.getId());
    f.setFirstName(d.getFirstName());
    f.setLastName(d.getLastName());
    f.setUsername(d.getUsername());
    f.setBiography(d.getBiography());

    d.getAvailability().forEach((day, range) -> {
      AvailabilityDto dto = f.getAvailabilities()
          .stream()
          .filter(av -> av.getDay() == day)
          .findFirst()
          .orElseThrow();
      dto.setOff(false);
      dto.setStartTime(range.getStart().toString()); // Convert LocalTime to String
      dto.setEndTime(range.getEnd().toString()); // Convert LocalTime to String
    });
    return f;
  }

  public void updateDoctor(Doctor d, PasswordEncoder encoder) {
    d.setFirstName(firstName);
    d.setLastName(lastName);
    d.setBiography(biography);

    if (password != null && !password.isBlank()) {
      d.setPasswordHash(encoder.encode(password));
    }

    d.getAvailability().clear();
    for (AvailabilityDto av : availabilities) {
      if (!Boolean.TRUE.equals(av.getOff())) {
        d.getAvailability()
            .put(av.getDay(),
                LocalTimeRange.of(
                    LocalTime.parse(av.getStartTime()),
                    LocalTime.parse(av.getEndTime())));
      }
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getBiography() {
    return biography;
  }

  public void setBiography(String biography) {
    this.biography = biography;
  }

  public List<AvailabilityDto> getAvailabilities() {
    return availabilities;
  }

  public void setAvailabilities(List<AvailabilityDto> availabilities) {
    this.availabilities = availabilities;
  }
}