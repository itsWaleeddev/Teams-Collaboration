package com.example.teamscollaboration.Models;

public class UserModel {
    String userId = null;
    String name = null;
    String role = null;
    String email = null;
    String userImage = null;

    public UserModel(String userId, String name, String role, String email, String userImage) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.email = email;
        this.userImage = userImage;
    }

    public UserModel() {
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
