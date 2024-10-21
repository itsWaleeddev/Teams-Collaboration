package com.example.teamscollaboration.Models;

import java.io.Serializable;

public class MembersModel implements Serializable {
    String uID = null;
    String email = null;
    String name = null;
    Boolean isChecked = false;
    String role = null;
    String userImage = null;

    public MembersModel(String email, String uID, String name, Boolean isChecked, String role, String userImage) {
        this.email = email;
        this.uID = uID;
        this.name = name;
        this.isChecked = isChecked;
        this.role = role;
        this.userImage = userImage;
    }

    public MembersModel() {
    }

    public String getRole() {
        return role;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
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

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }
}
