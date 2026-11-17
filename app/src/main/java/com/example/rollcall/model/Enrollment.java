package com.example.rollcall.model;

import java.io.Serializable;

public class Enrollment implements Serializable {
    private String enrollmentId;
    private String studentId;
    private String studentName;
    private String rollNumber;
    private String classId;
    private String subjectId;
    private long enrolledAt;

    public Enrollment() {
    }

    public Enrollment(String enrollmentId, String studentId, String studentName, String rollNumber, String classId, String subjectId) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.classId = classId;
        this.subjectId = subjectId;
        this.enrolledAt = System.currentTimeMillis();
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
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

    public long getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(long enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
