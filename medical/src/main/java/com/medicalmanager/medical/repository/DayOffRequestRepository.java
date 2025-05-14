package com.medicalmanager.medical.repository;

import com.medicalmanager.medical.model.DayOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayOffRequestRepository extends JpaRepository<DayOffRequest, Long> {
}
