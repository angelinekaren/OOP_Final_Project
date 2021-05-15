package com.example.plannerproject;

public class User {
    private String fullname;
    private String email;

    public User(){}

    public User(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }
}
