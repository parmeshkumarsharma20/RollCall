package com.example.rollcall.utils;

public class Constants {
    // Attendance Rules
    public static final int QR_VALIDITY_SECONDS = 50;
    public static final float ALLOWED_RADIUS_METERS = 25.0f;
    public static final float ATTENDANCE_THRESHOLD_PERCENT = 75.0f;
    public static final int INITIAL_CAPACITY_STUDENTS = 90;

    // Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";

    // Firestore Collection Names
    public static final String COLL_USERS = "users";
    public static final String COLL_COURSES = "courses";
    public static final String COLL_CLASSES = "classes";
    public static final String COLL_SUBJECTS = "subjects";
    public static final String COLL_ROOMS = "rooms";
    public static final String COLL_ENROLLMENTS = "enrollments";
    public static final String COLL_TEACHER_ASSIGNMENTS = "teacher_assignments";
    public static final String COLL_SESSIONS = "sessions";
    public static final String COLL_QR_TOKENS = "qr_tokens";
    public static final String COLL_ATTENDANCE = "attendance";
    public static final String COLL_NOTIFICATIONS = "notifications";
    public static final String COLL_AUDIT_LOGS = "audit_logs";

    // Session Status
    public static final String SESSION_ACTIVE = "active";
    public static final String SESSION_PAUSED = "paused";
    public static final String SESSION_ENDED = "ended";

    // Attendance Status
    public static final String STATUS_PRESENT = "present";
    public static final String STATUS_ABSENT = "absent";
}