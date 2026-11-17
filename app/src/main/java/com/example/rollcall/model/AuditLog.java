package com.example.rollcall.model;

import java.io.Serializable;

public class AuditLog implements Serializable {
    private String logId;
    private String action; // e.g., "MANUAL_ATTENDANCE_CORRECTION", "COURSE_ADDED"
    private String performedBy;
    private String details;
    private long timestamp;

    public AuditLog() {
    }

    public AuditLog(String logId, String action, String performedBy, String details) {
        this.logId = logId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
