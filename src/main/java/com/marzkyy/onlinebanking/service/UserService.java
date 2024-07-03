package com.marzkyy.onlinebanking.service;

import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.model.Balance;
import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.repository.UserRepository;
import com.marzkyy.onlinebanking.repository.BalanceRepository;
import com.marzkyy.onlinebanking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public UserService(UserRepository userRepository, BalanceRepository balanceRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(); // Fetch all users from the repository
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

    @Transactional
    public void cashOut(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate sufficient funds
        BigDecimal currentBalance = getBalance(userId);
        if (amount.compareTo(currentBalance) > 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Update the user's balance
        createOrUpdateBalance(userId, currentBalance.subtract(amount));

        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(amount.negate()); // Negative amount for the cash-out
        transaction.setName("Cash-Out");
        transaction.setUser(user);
        transaction.setDate(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Transactional
    public void cashTransfer(Long senderId, Long recipientId, BigDecimal amount) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Validate sufficient funds
        BigDecimal senderBalance = getBalance(senderId);
        if (amount.compareTo(senderBalance) > 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Update sender's balance
        createOrUpdateBalance(senderId, senderBalance.subtract(amount));

        // Update recipient's balance
        BigDecimal recipientBalance = getBalance(recipientId);
        createOrUpdateBalance(recipientId, recipientBalance.add(amount));

        // Record the sender's transaction
        Transaction senderTransaction = new Transaction();
        senderTransaction.setAmount(amount.negate()); // Negative amount for the sender
        senderTransaction.setName("Cash-Transfer");
        senderTransaction.setTransferFrom(sender); // Set the sender
        senderTransaction.setTransferTo(recipient); // Set the recipient
        senderTransaction.setDate(LocalDateTime.now());
        transactionRepository.save(senderTransaction);

        // Record the recipient's transaction
        Transaction recipientTransaction = new Transaction();
        recipientTransaction.setAmount(amount); // Positive amount for the recipient
        recipientTransaction.setName("Cash-Transfer");
        recipientTransaction.setTransferFrom(sender); // Set the sender
        recipientTransaction.setTransferTo(recipient); // Set the recipient
        recipientTransaction.setDate(LocalDateTime.now());
        transactionRepository.save(recipientTransaction);
    }

    @Transactional
    public BigDecimal getBalance(Long userId) {
        return balanceRepository.findByUserId(userId)
                .map(Balance::getAmount)
                .orElse(BigDecimal.ZERO); // Return zero if balance not found
    }

}
