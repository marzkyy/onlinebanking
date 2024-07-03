package com.marzkyy.onlinebanking.repository;

import com.marzkyy.onlinebanking.dto.UserDTO;
import com.marzkyy.onlinebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Method to find a user by email and pin
    Optional<User> findByEmailAndPin(String email, String pin);

    // Method to find a user by email
    Optional<User> findUserByEmail(String email);

    // Method to check if a user exists by email
    boolean existsByEmail(String email);

    // Custom query to fetch simplified user data
    @Query("SELECT new com.marzkyy.onlinebanking.dto.UserDTO(u.id, u.email) FROM User u")
    List<UserDTO> findUserDTOs();
}
