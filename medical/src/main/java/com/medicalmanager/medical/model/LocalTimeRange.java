package com.medicalmanager.medical.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LocalTimeRange {
  @Column(name="start_time", columnDefinition="TIME")
  private LocalTime start; 

  @Column(name="end_time",   columnDefinition="TIME")
  private LocalTime end;

  public LocalTime getStart() { return start; }
  public void setStart(LocalTime s) { this.start = s; }

  public LocalTime getEnd() { return end; }
  public void setEnd(LocalTime e) { this.end = e; }

  public static LocalTimeRange of(LocalTime s, LocalTime e) {
    LocalTimeRange r = new LocalTimeRange();
    r.setStart(s);
    r.setEnd(e);
    return r;
  }
}