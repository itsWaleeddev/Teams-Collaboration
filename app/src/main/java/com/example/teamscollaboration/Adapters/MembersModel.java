package com.example.teamscollaboration.Adapters;

import java.io.Serializable;

public class MembersModel implements Serializable {
    String name = null;
    Boolean isChecked = false;

    public MembersModel(String name, Boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    public MembersModel() {
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
