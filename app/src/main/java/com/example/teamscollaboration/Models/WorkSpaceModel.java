package com.example.teamscollaboration.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceModel implements Serializable {
    String workSpaceName = null;
    String workSpaceDescription = null;
    String deadLine = null;
    String priority = null;
    Long created_at = null;
    String adminId = null;
    String adminName = null;
    List<MembersModel> membersList;
    String TeamLeader = null;
    String workSpaceKey = null;
    String adminImage = null;
    ArrayList<StreamModel> streamModel = null;
    int background;

    public WorkSpaceModel(String workSpaceKey, String workSpaceName, String workSpaceDescription, String deadLine, String priority,
                          Long created_at, String adminId, String adminName, List<MembersModel> membersList,
                          String teamLeader, String adminImage, ArrayList<StreamModel> streamModel, int background) {
        this.workSpaceKey = workSpaceKey;
        this.workSpaceName = workSpaceName;
        this.workSpaceDescription = workSpaceDescription;
        this.deadLine = deadLine;
        this.priority = priority;
        this.created_at = created_at;
        this.adminId = adminId;
        this.adminName = adminName;
        this.membersList = membersList;
        this.TeamLeader = teamLeader;
        this.adminImage = adminImage;
        this.streamModel = streamModel;
        this.background = background;
    }

    public WorkSpaceModel() {
    }

    @Override
    public String toString() {
        return "WorkSpaceModel{" +
                "workSpaceName='" + workSpaceName + '\'' +
                ", workSpaceDescription='" + workSpaceDescription + '\'' +
                ", deadLine='" + deadLine + '\'' +
                ", priority='" + priority + '\'' +
                ", created_at=" + created_at +
                ", adminId='" + adminId + '\'' +
                ", adminName='" + adminName + '\'' +
                ", membersList=" + membersList +
                ", TeamLeader='" + TeamLeader + '\'' +
                ", workSpaceKey='" + workSpaceKey + '\'' +
                ", adminImage='" + adminImage + '\'' +
                ", streamModel=" + streamModel +
                '}';
    }

    public ArrayList<StreamModel> getStreamModel() {
        return streamModel;
    }

    public void setStreamModel(ArrayList<StreamModel> streamModel) {
        this.streamModel = streamModel;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public String getAdminImage() {
        return adminImage;
    }

    public void setAdminImage(String adminImage) {
        this.adminImage = adminImage;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
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
