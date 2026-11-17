package com.example.rollcall.model;

import java.io.Serializable;

public class AttendanceRecord implements Serializable {
    private String recordId;
    private String studentId;
    private String studentName;
    private String rollNumber;
    private String sessionId;
    private String classId;
    private String subjectId;
    private long timestamp;
    private long markedAt;
    private float distanceMeters;
    private float locationAccuracyMeters;
    private String status = "PRESENT"; // "PRESENT", "ABSENT", "REJECTED"

    public AttendanceRecord() {
    }

    public AttendanceRecord(String recordId, String studentId, String studentName, String rollNumber, long timestamp, float distanceMeters, String status) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.timestamp = timestamp;
        this.markedAt = timestamp;
        this.distanceMeters = distanceMeters;
        this.status = status;
    }

    public AttendanceRecord(String recordId, String studentId, String studentName, String rollNumber, String sessionId, String classId, String subjectId, long markedAt, float distanceMeters, float locationAccuracyMeters, String status) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.sessionId = sessionId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.timestamp = markedAt;
        this.markedAt = markedAt;
        this.distanceMeters = distanceMeters;
        this.locationAccuracyMeters = locationAccuracyMeters;
        this.status = status;
    }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public long getTimestamp() { return timestamp > 0 ? timestamp : markedAt; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; this.markedAt = timestamp; }

    public long getMarkedAt() { return markedAt > 0 ? markedAt : timestamp; }
    public void setMarkedAt(long markedAt) { this.markedAt = markedAt; this.timestamp = markedAt; }

    public float getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(float distanceMeters) { this.distanceMeters = distanceMeters; }

    public float getLocationAccuracyMeters() { return locationAccuracyMeters; }
    public void setLocationAccuracyMeters(float locationAccuracyMeters) { this.locationAccuracyMeters = locationAccuracyMeters; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
