package com.example.rollcall.model;

import java.io.Serializable;

public class Course implements Serializable {
    private String courseId;
    private String courseName;
    private String courseCode;
    private int durationYears;
    private String description;
    private boolean activeStatus = true;
    private long createdAt;

    public Course() {
    }

    public Course(String courseId, String courseName, String courseCode, int durationYears, String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.durationYears = durationYears;
        this.description = description;
        this.activeStatus = true;
        this.createdAt = System.currentTimeMillis();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(int durationYears) {
        this.durationYears = durationYears;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}
