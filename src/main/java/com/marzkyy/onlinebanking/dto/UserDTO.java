package com.marzkyy.onlinebanking.dto;

public class UserDTO {
    private Long id;
    private String email;

    public UserDTO(Long id, String email) {
        this.id = id;
        this.email = email;
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
}
