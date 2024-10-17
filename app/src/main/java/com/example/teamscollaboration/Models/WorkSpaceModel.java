package com.example.teamscollaboration.Models;

public class WorkSpaceModel {
    String workSpaceName = null;
    String workSpaceDescription = null;
    String deadLine = null;
    String priority = null;
    Long created_at = null;
    String adminId = null;

    public WorkSpaceModel(String workSpaceName, String workSpaceDescription, String deadLine, String priority, Long created_at, String adminId) {
        this.workSpaceName = workSpaceName;
        this.workSpaceDescription = workSpaceDescription;
        this.deadLine = deadLine;
        this.priority = priority;
        this.created_at = created_at;
        this.adminId = adminId;
    }

    public WorkSpaceModel() {
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getWorkSpaceName() {
        return workSpaceName;
    }

    public void setWorkSpaceName(String workSpaceName) {
        this.workSpaceName = workSpaceName;
    }

    public String getWorkSpaceDescription() {
        return workSpaceDescription;
    }

    public void setWorkSpaceDescription(String workSpaceDescription) {
        this.workSpaceDescription = workSpaceDescription;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }
}
