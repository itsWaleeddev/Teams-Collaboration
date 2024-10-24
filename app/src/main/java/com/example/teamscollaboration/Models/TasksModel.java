package com.example.teamscollaboration.Models;

import java.io.Serializable;
import java.util.List;

public class TasksModel implements Serializable {
    String taskKey = null;
    String taskName = null;
    String taskDescription = null;
    String deadLine = null;
    String priority = null;
    Long created_at = null;
    String teamLeadId = null;
    List<MembersModel> membersList;
    String TeamLeader = null;
    String workSpaceKey = null;
    String taskStatus = null;

    public TasksModel(String taskKey, String taskName, String taskDescription, String deadLine, String priority,
                      Long created_at, String adminId, List<MembersModel> membersList, String teamLeader, String workSpaceKey,
                      String taskStatus) {
        this.taskKey = taskKey;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.deadLine = deadLine;
        this.priority = priority;
        this.created_at = created_at;
        this.teamLeadId = adminId;
        this.membersList = membersList;
        TeamLeader = teamLeader;
        this.workSpaceKey = workSpaceKey;
        this.taskStatus = taskStatus;
    }

    public TasksModel() {
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getWorkSpaceKey() {
        return workSpaceKey;
    }

    public void setWorkSpaceKey(String workSpaceKey) {
        this.workSpaceKey = workSpaceKey;
    }

    public List<MembersModel> getMembersList() {
        return membersList;
    }

    public void setMembersList(List<MembersModel> membersList) {
        this.membersList = membersList;
    }

    public String getTeamLeader() {
        return TeamLeader;
    }

    public void setTeamLeader(String teamLeader) {
        TeamLeader = teamLeader;
    }

    public String getTeamLeadId() {
        return teamLeadId;
    }

    public void setTeamLeadId(String teamLeadId) {
        this.teamLeadId = teamLeadId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
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
