package com.marzkyy.onlinebanking.service;

import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.model.Balance;
import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.repository.UserRepository;
import com.marzkyy.onlinebanking.repository.BalanceRepository;
import com.marzkyy.onlinebanking.repository.TransactionRepository; // Add this import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository; // Add this field

    @Autowired
    public UserService(UserRepository userRepository, BalanceRepository balanceRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository; // Initialize in constructor
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
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

        // Only set default balance if user does not already have a balance
        Optional<Balance> existingBalance = balanceRepository.findByUserId(savedUser.getId());
        if (!existingBalance.isPresent()) {
            // Create and save a new Balance object with a default amount
            Balance initialBalance = new Balance();
            initialBalance.setAmount(new BigDecimal("0.00")); // Default balance of 0
            initialBalance.setUser(savedUser);
            balanceRepository.save(initialBalance);
        }

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

    @Transactional
    public void cashIn(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the user's balance
        createOrUpdateBalance(userId, getBalance(userId).add(amount));

        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setName("Cash-In");
        transaction.setUser(user);
        transaction.setDate(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    // Helper method to get current balance
    @Transactional
    public BigDecimal getBalance(Long userId) {
        return balanceRepository.findByUserId(userId)
                .map(Balance::getAmount)
                .orElse(BigDecimal.ZERO); // Return zero if balance not found
    }
}
