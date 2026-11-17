package com.example.rollcall.model;

import java.io.Serializable;

public class Semester implements Serializable {
    private String semesterId;
    private String courseId;
    private int semesterNumber;
    private String semesterName; // e.g. "Semester 1"
    private boolean activeStatus = true;

    public Semester() {
    }

    public Semester(String semesterId, String courseId, int semesterNumber, String semesterName) {
        this.semesterId = semesterId;
        this.courseId = courseId;
        this.semesterNumber = semesterNumber;
        this.semesterName = semesterName;
        this.activeStatus = true;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return semesterName;
    }
}
