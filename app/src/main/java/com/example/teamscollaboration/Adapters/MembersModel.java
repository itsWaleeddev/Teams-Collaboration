package com.example.teamscollaboration.Adapters;

import java.io.Serializable;

public class MembersModel implements Serializable {
    String uID = null;
    String email = null;
    String name = null;
    Boolean isChecked = false;

    public MembersModel(String email, String uID, String name, Boolean isChecked) {
        this.email = email;
        this.uID = uID;
        this.name = name;
        this.isChecked = isChecked;
    }

    public MembersModel() {
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
