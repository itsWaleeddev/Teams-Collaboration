package com.example.teamscollaboration.Models;

import java.io.Serializable;

public class TaskUploadModel implements Serializable {
    String userName = null;
    String uID = null;
    String fileUri = null;
    String fileName = null;
    Long upload_time = null;

    public TaskUploadModel(String userName, String uID, String fileUri, String fileName, Long upload_time) {
        this.userName = userName;
        this.uID = uID;
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.upload_time = upload_time;
    }

    public TaskUploadModel() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(Long upload_time) {
        this.upload_time = upload_time;
    }
}
