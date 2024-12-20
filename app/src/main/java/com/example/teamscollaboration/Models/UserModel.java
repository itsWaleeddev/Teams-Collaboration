package com.example.teamscollaboration.Models;

public class UserModel {
    String userId = null;
    String name = null;
    String email = null;
    String userImage = null;
    String about = null;

    public UserModel(String userId, String name, String email, String userImage, String about) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.userImage = userImage;
        this.about = about;
    }

    public UserModel() {
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
