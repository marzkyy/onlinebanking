package com.marzkyy.onlinebanking.service;

import java.util.Optional;

import com.marzkyy.onlinebanking.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.marzkyy.onlinebanking.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRespository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRespository = userRepository;
    }

    @Transactional
    public Optional<User> findUserByEmail(String email) {
        return userRespository.findUserByEmail(email);
    }

    public boolean userExists(String email) {
        return userRespository.existsByEmail(email);
    }

    @Transactional
    public User save(User user){
        return userRespository.save(user);
    }


}