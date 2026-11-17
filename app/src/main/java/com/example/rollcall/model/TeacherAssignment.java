package com.example.rollcall.model;

import java.io.Serializable;

public class TeacherAssignment implements Serializable {
    private String assignmentId;
    private String teacherId;
    private String teacherName;
    private String classId;
    private String subjectId;
    private String subjectName;

    public TeacherAssignment() {
    }

    public TeacherAssignment(String assignmentId, String teacherId, String teacherName, String classId, String subjectId, String subjectName) {
        this.assignmentId = assignmentId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.classId = classId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
