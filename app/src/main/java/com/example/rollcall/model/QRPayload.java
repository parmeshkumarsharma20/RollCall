package com.example.rollcall.model;

import org.json.JSONException;
import org.json.JSONObject;

public class QRPayload {
    private String sessionId;
    private String roomName;
    private String courseName;
    private String subjectName;
    private long timestamp;
    private double teacherLatitude;
    private double teacherLongitude;
    private int validityDurationSeconds;

    public QRPayload() {}

    public QRPayload(String sessionId, String roomName, long timestamp, double teacherLatitude, double teacherLongitude, int validityDurationSeconds) {
        this.sessionId = sessionId;
        this.roomName = roomName;
        this.timestamp = timestamp;
        this.teacherLatitude = teacherLatitude;
        this.teacherLongitude = teacherLongitude;
        this.validityDurationSeconds = validityDurationSeconds;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getTeacherLatitude() { return teacherLatitude; }
    public void setTeacherLatitude(double teacherLatitude) { this.teacherLatitude = teacherLatitude; }

    public double getTeacherLongitude() { return teacherLongitude; }
    public void setTeacherLongitude(double teacherLongitude) { this.teacherLongitude = teacherLongitude; }

    public int getValidityDurationSeconds() { return validityDurationSeconds; }
    public void setValidityDurationSeconds(int validityDurationSeconds) { this.validityDurationSeconds = validityDurationSeconds; }

    public String toJsonString() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("sessionId", sessionId);
            obj.put("roomName", roomName);
            obj.put("courseName", courseName != null ? courseName : "");
            obj.put("subjectName", subjectName != null ? subjectName : "");
            obj.put("timestamp", timestamp);
            obj.put("teacherLat", teacherLatitude);
            obj.put("teacherLng", teacherLongitude);
            obj.put("validSec", validityDurationSeconds);
            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static QRPayload fromJsonString(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            QRPayload payload = new QRPayload(
                    obj.optString("sessionId"),
                    obj.optString("roomName"),
                    obj.optLong("timestamp"),
                    obj.optDouble("teacherLat"),
                    obj.optDouble("teacherLng"),
                    obj.optInt("validSec", 50)
            );
            payload.setCourseName(obj.optString("courseName"));
            payload.setSubjectName(obj.optString("subjectName"));
            return payload;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
