package com.marzkyy.onlinebanking.service;

import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.repository.TransactionRepository;
import com.marzkyy.onlinebanking.repository.UserRepository;
import com.marzkyy.onlinebanking.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, BalanceRepository balanceRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
    }

    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Transactional
    public void processCashIn(Transaction transaction) {
        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal newBalance = user.getBalance().getAmount().add(transaction.getAmount());
        user.getBalance().setAmount(newBalance);

        userRepository.save(user);
        saveTransaction(transaction);
    }

    @Transactional
    public void processCashOut(Transaction transaction) {
        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal currentBalance = user.getBalance().getAmount();
        if (transaction.getAmount().compareTo(currentBalance) > 0) {
            throw new RuntimeException("Insufficient funds");
        }

        BigDecimal newBalance = currentBalance.subtract(transaction.getAmount());
        user.getBalance().setAmount(newBalance);

        userRepository.save(user);
        saveTransaction(transaction);
    }

    @Transactional
    public void processCashTransfer(Transaction transaction) {
        // Retrieve the sender user from the database
        User sender = userRepository.findById(transaction.getTransferFrom().getId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Retrieve the recipient user from the database
        User recipient = userRepository.findById(transaction.getTransferTo().getId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Validate sufficient funds
        BigDecimal senderBalance = sender.getBalance().getAmount();
        if (transaction.getAmount().compareTo(senderBalance) > 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Update sender's balance
        BigDecimal newSenderBalance = senderBalance.subtract(transaction.getAmount());
        sender.getBalance().setAmount(newSenderBalance);

        // Update recipient's balance
        BigDecimal newRecipientBalance = recipient.getBalance().getAmount().add(transaction.getAmount());
        recipient.getBalance().setAmount(newRecipientBalance);

        // Save the updated balances
        balanceRepository.save(sender.getBalance());
        balanceRepository.save(recipient.getBalance());

        // Save the transaction with correct user information
        transaction.setUser(sender); // Set the user as the sender
        transaction.setTransferTo(recipient);
        transaction.setTransferFrom(sender);
        transaction.setDate(LocalDateTime.now());

        saveTransaction(transaction);
    }

    // Method to find transactions by user ID
    public List<Transaction> findTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getTransactionsForUser(Long userId) {
        return transactionRepository.findTransactionsForUser(userId);
    }
}
