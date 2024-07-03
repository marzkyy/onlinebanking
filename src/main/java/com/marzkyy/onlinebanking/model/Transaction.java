package com.marzkyy.onlinebanking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "transaction_ibfk_1"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "transfer_to_id", foreignKey = @ForeignKey(name = "transaction_ibfk_2"))
    private User transferTo;

    @ManyToOne
    @JoinColumn(name = "transfer_from_id", foreignKey = @ForeignKey(name = "transaction_ibfk_3"))
    private User transferFrom;

    // Constructors
    public Transaction() {
        this.date = LocalDateTime.now(); // Default to current time
    }

    public Transaction(BigDecimal amount, String name, User user, LocalDateTime date, User transferTo, User transferFrom) {
        this.amount = amount;
        this.name = name;
        this.user = user;
        this.date = date;
        this.transferTo = transferTo;
        this.transferFrom = transferFrom;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getTransferTo() {
        return transferTo;
    }

    public void setTransferTo(User transferTo) {
        this.transferTo = transferTo;
    }

    public User getTransferFrom() {
        return transferFrom;
    }

    public void setTransferFrom(User transferFrom) {
        this.transferFrom = transferFrom;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", user=" + user +
                ", transferTo=" + transferTo +
                ", transferFrom=" + transferFrom +
                '}';
    }
}
