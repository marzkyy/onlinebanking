package com.marzkyy.onlinebanking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String name;
    private String number;
    private String pin;
    private transient String rpin; // Transient field, not persisted
    private transient String npin; // Transient field, not persisted

    // Constructors
    public User() {
        // Default constructor required by JPA
    }

    public User(String email, String name, String number, String pin, String rpin) {
        this.email = email;
        this.name = name;
        this.number = number;
        this.pin = pin;
        this.rpin = rpin;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getRpin() {
        return rpin;
    }

    public void setRpin(String rpin) {
        this.rpin = rpin;
    }

    public String getNpin() {
        return npin;
    }

    public void setNpin(String npin) {
        this.npin = npin;
    }

    // Optional: Override toString() for logging and debugging purposes
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", pin='" + pin + '\'' +
                '}';
    }
}

