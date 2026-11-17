package com.example.rollcall.model;

import java.io.Serializable;

public class AcademicSession implements Serializable {
    private String sessionId;
    private String sessionName; // e.g. "2026-27"
    private boolean activeStatus = true;
    private long startDate;
    private long endDate;

    public AcademicSession() {
    }

    public AcademicSession(String sessionId, String sessionName, boolean activeStatus) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.activeStatus = activeStatus;
        this.startDate = System.currentTimeMillis();
        this.endDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return sessionName;
    }
}
