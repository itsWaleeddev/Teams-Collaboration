package com.example.teamscollaboration.Models;

import java.io.Serializable;
import java.util.List;

public class WorkSpaceModel implements Serializable {
    String workSpaceName = null;
    String workSpaceDescription = null;
    String deadLine = null;
    String priority = null;
    Long created_at = null;
    String adminId = null;
    List<MembersModel> membersList;
    String TeamLeader = null;
    String workSpaceKey = null;

    public WorkSpaceModel(String workSpaceKey, String workSpaceName, String workSpaceDescription, String deadLine, String priority,
                          Long created_at, String adminId, List<MembersModel> membersList, String teamLeader) {
        this.workSpaceKey = workSpaceKey;
        this.workSpaceName = workSpaceName;
        this.workSpaceDescription = workSpaceDescription;
        this.deadLine = deadLine;
        this.priority = priority;
        this.created_at = created_at;
        this.adminId = adminId;
        this.membersList = membersList;
        TeamLeader = teamLeader;
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
                ", membersList=" + membersList +
                ", TeamLeader='" + TeamLeader + '\'' +
                ", workSpaceKey='" + workSpaceKey + '\'' +
                '}';
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
