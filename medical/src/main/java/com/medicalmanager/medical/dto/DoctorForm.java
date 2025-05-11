package com.medicalmanager.medical.dto;

import com.medicalmanager.medical.model.Doctor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DoctorForm {
  @NotBlank private String firstName;
  @NotBlank private String lastName;
  @NotBlank private String username;
  @NotBlank @Size(min = 8) private String password;
  @NotBlank private String biography;

  private List<AvailabilityDto> availabilities = new ArrayList<>();

  // ← Initialize all seven days here
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

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getBiography() { return biography; }
  public void setBiography(String biography) { this.biography = biography; }

  public List<AvailabilityDto> getAvailabilities() { return availabilities; }
  public void setAvailabilities(List<AvailabilityDto> availabilities) {
    this.availabilities = availabilities;
  }
}