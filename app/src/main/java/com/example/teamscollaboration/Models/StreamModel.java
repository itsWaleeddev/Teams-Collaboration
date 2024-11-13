package com.example.teamscollaboration.Models;

import java.io.Serializable;

public class StreamModel implements Serializable {
    String topicName = null;
    String topicComment = null;
    String topicFile = null;
    String fileName = null;
    String uID = null;
    String userImage = null;
    String userName = null;
   Long date = null;

    public StreamModel(String topicName, String topicComment, String topicFile, String fileName,
                       String uID, String userImage, String userName, Long date) {
        this.topicName = topicName;
        this.topicComment = topicComment;
        this.topicFile = topicFile;
        this.fileName = fileName;
        this.uID = uID;
        this.userImage = userImage;
        this.userName = userName;
        this.date = date;
    }

    public StreamModel() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicComment() {
        return topicComment;
    }

    public void setTopicComment(String topicComment) {
        this.topicComment = topicComment;
    }

    public String getTopicFile() {
        return topicFile;
    }

    public void setTopicFile(String topicFile) {
        this.topicFile = topicFile;
    }
}
