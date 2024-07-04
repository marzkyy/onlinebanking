package com.marzkyy.onlinebanking.dto;

import java.math.BigDecimal;

public class TransactionDTO {
    private String description;
    private BigDecimal amount;
    private String dateTime;

    // Default constructor (needed for Jackson)
    public TransactionDTO() {}

    public TransactionDTO(String description, BigDecimal amount, String dateTime) {
        this.description = description;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    // Getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
}
