package com.medicalmanager.medical.repository;

import java.util.Optional;

import com.medicalmanager.medical.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    Optional<User> findByEmail(String email);
}
