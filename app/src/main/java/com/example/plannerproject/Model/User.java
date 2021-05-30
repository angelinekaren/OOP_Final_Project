package com.example.plannerproject.Model;

// User model
public class User {
    private String fullname;
    private String email;

    // Empty constructor
    public User(){}

    // Constructor
    public User(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }

    // Getter

    // Get fullname
    public String getFullname() {
        return fullname;
    }
    // Get email
    public String getEmail() {
        return email;
    }
}
