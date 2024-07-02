package com.marzkyy.onlinebanking.service;

import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.model.Balance;
import com.marzkyy.onlinebanking.repository.UserRepository;
import com.marzkyy.onlinebanking.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;

    @Autowired
    public UserService(UserRepository userRepository, BalanceRepository balanceRepository) {
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
    }

    @Transactional
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User save(User user) {
        // Save the user
        User savedUser = userRepository.save(user);

        // Set default balance
        createOrUpdateBalance(savedUser.getId(), new BigDecimal("500.00"));

        return savedUser;
    }

    @Transactional
    public void createOrUpdateBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Balance> existingBalance = balanceRepository.findByUserId(userId);
        if (existingBalance.isPresent()) {
            Balance balance = existingBalance.get();
            balance.setAmount(amount);
            balanceRepository.save(balance);
        } else {
            Balance newBalance = new Balance(amount, user);
            balanceRepository.save(newBalance);
        }
    }
}
