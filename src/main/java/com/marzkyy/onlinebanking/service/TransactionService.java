package com.marzkyy.onlinebanking.service;

import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.repository.TransactionRepository;
import com.marzkyy.onlinebanking.repository.UserRepository; // Assuming you have a UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository; // Assuming you have a UserRepository

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Transactional
    public void processCashIn(Transaction transaction) {
        // Retrieve the user from the database
        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user's balance
        BigDecimal newBalance = user.getBalance().getAmount().add(transaction.getAmount());
        user.getBalance().setAmount(newBalance);

        // Save the updated user
        userRepository.save(user);

        // Save the transaction
        saveTransaction(transaction);
    }
}
