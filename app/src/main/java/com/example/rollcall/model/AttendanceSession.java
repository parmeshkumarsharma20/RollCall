package com.example.rollcall.model;

import java.io.Serializable;

public class AttendanceSession implements Serializable {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_ENDED = "ended";

    private String sessionId;
    private String teacherId;
    private String teacherName;
    private String classId;
    private String subjectId;
    private String subjectName;
    private String roomId;
    private String roomNumber;
    private double latitude;
    private double longitude;
    private String courseName;
    private String status = STATUS_ACTIVE;
    private int qrValiditySeconds = 50;
    private float allowedRadiusMeters = 25.0f;
    private long currentGeneration = 1;
    private String currentToken;
    private long tokenIssuedAt;
    private long tokenExpiresAt;
    private long createdAt;

    public AttendanceSession() {
    }

    public AttendanceSession(String sessionId, String teacherId, String teacherName, String classId, String subjectId, String subjectName, String roomId, String roomNumber, double latitude, double longitude) {
        this.sessionId = sessionId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.classId = classId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = STATUS_ACTIVE;
        this.qrValiditySeconds = 50;
        this.allowedRadiusMeters = 25.0f;
        this.currentGeneration = 1;
        this.createdAt = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQrValiditySeconds() {
        return qrValiditySeconds;
    }

    public void setQrValiditySeconds(int qrValiditySeconds) {
        this.qrValiditySeconds = qrValiditySeconds;
    }

    public float getAllowedRadiusMeters() {
        return allowedRadiusMeters;
    }

    public void setAllowedRadiusMeters(float allowedRadiusMeters) {
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public long getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(long currentGeneration) {
        this.currentGeneration = currentGeneration;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(String currentToken) {
        this.currentToken = currentToken;
    }

    public long getTokenIssuedAt() {
        return tokenIssuedAt;
    }

    public void setTokenIssuedAt(long tokenIssuedAt) {
        this.tokenIssuedAt = tokenIssuedAt;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public long getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(long tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
