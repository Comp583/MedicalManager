package com.medicalmanager.medical.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medicalmanager.medical.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}