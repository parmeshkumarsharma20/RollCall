package com.example.rollcall.model;

import java.io.Serializable;

public class Section implements Serializable {
    private String sectionId;
    private String courseId;
    private String semesterId;
    private String sectionName; // e.g. "Section A"
    private boolean activeStatus = true;

    public Section() {
    }

    public Section(String sectionId, String courseId, String semesterId, String sectionName) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.semesterId = semesterId;
        this.sectionName = sectionName;
        this.activeStatus = true;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return sectionName;
    }
}
