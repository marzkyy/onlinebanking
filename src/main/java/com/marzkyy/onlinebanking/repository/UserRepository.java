package com.marzkyy.onlinebanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.marzkyy.onlinebanking.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Add custom query methods if needed
    Optional<User> findByEmailAndPin(String email, String pin);
    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);
}