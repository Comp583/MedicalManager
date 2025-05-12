package com.medicalmanager.medical.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medicalmanager.medical.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    void deleteByUsername(String username);
}
