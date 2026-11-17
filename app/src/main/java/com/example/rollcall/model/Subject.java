package com.example.rollcall.model;

import java.io.Serializable;

public class Subject implements Serializable {
    private String subjectId;
    private String subjectCode;
    private String subjectName;
    private String course;
    private String semester;
    private String classId;
    private boolean activeStatus = true;

    public Subject() {
    }

    public Subject(String subjectId, String subjectCode, String subjectName, String course, String semester, String classId) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.course = course;
        this.semester = semester;
        this.classId = classId;
        this.activeStatus = true;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName;
    }
}
