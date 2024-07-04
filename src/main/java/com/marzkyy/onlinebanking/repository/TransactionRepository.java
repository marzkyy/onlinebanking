package com.marzkyy.onlinebanking.repository;

import com.marzkyy.onlinebanking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Method to find transactions by user ID
    List<Transaction> findByUserId(Long userId);
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId OR t.transferTo.id = :userId ORDER BY t.date DESC")
    List<Transaction> findTransactionsForUser(@Param("userId") Long userId);
}
