package com.marzkyy.onlinebanking.repository;

import com.marzkyy.onlinebanking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Additional custom query methods can be defined here if needed
}
