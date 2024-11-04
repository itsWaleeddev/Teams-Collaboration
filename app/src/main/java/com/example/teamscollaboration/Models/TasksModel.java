package com.example.teamscollaboration.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TasksModel implements Serializable {
    String taskKey = null;
    String taskName = null;
    String taskDescription = null;
    String deadLine = null;
    String endTime = null;
    Long created_at = null;
    String ownerID = null;
    String taskOwner = null;
    List<MembersModel> membersList;
    String TeamLeader = null;
    String workSpaceKey = null;
    String taskStatus = null;
    String fileUri = null;
    String fileName = null;
    ArrayList<TaskUploadModel> taskUploads;

    public TasksModel(String taskKey, String taskName, String taskDescription, String deadLine, String endTime,
                      Long created_at,String ownerID, String taskOwner, List<MembersModel> membersList, String teamLeader, String workSpaceKey,
                      String taskStatus, String fileUri, String fileName, ArrayList<TaskUploadModel> taskUploads) {
        this.taskKey = taskKey;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.deadLine = deadLine;
        this.endTime = endTime;
        this.created_at = created_at;
        this.ownerID = ownerID;
        this.taskOwner = taskOwner;
        this.membersList = membersList;
        TeamLeader = teamLeader;
        this.workSpaceKey = workSpaceKey;
        this.taskStatus = taskStatus;
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.taskUploads = taskUploads;
    }

    public TasksModel() {
    }

    public ArrayList<TaskUploadModel> getTaskUploads() {
        return taskUploads;
    }

    public void setTaskUploads(ArrayList<TaskUploadModel> taskUploads) {
        this.taskUploads = taskUploads;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
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

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }
}
