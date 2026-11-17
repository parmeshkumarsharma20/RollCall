package com.example.rollcall.data;

import android.content.Context;

import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.AttendanceSession;
import com.example.rollcall.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionRepository {
    private static SessionRepository instance;
    private final Context context;
    private final FirebaseFirestore firestore;

    public interface SessionCallback {
        void onSuccess(AttendanceSession session);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LiveAttendanceCallback {
        void onAttendanceUpdated(List<AttendanceRecord> records, int presentCount, int totalCount);
        void onError(String message);
    }

    private SessionRepository(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized SessionRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SessionRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void createSession(String teacherId, String teacherName, String classId, String subjectId, String subjectName, String roomId, String roomNumber, double lat, double lng, SessionCallback callback) {
        String sessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();
        long expiresAt = now + (Constants.QR_VALIDITY_SECONDS * 1000L);
        String initialToken = "token_1_" + UUID.randomUUID().toString().substring(0, 8);

        AttendanceSession session = new AttendanceSession(
                sessionId, teacherId, teacherName, classId, subjectId, subjectName, roomId, roomNumber, lat, lng
        );
        session.setCurrentGeneration(1);
        session.setCurrentToken(initialToken);
        session.setTokenIssuedAt(now);
        session.setTokenExpiresAt(expiresAt);

        firestore.collection(Constants.COLL_SESSIONS).document(sessionId).set(session)
                .addOnSuccessListener(aVoid -> callback.onSuccess(session))
                .addOnFailureListener(e -> callback.onError("Failed to create session: " + e.getMessage()));
    }

    public void rotateQRToken(String sessionId, SessionCallback callback) {
        firestore.collection(Constants.COLL_SESSIONS).document(sessionId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AttendanceSession session = documentSnapshot.toObject(AttendanceSession.class);
                        if (session != null && AttendanceSession.STATUS_ACTIVE.equals(session.getStatus())) {
                            long nextGen = session.getCurrentGeneration() + 1;
                            String newToken = "token_" + nextGen + "_" + UUID.randomUUID().toString().substring(0, 8);
                            long now = System.currentTimeMillis();
                            long expiresAt = now + (Constants.QR_VALIDITY_SECONDS * 1000L);

                            session.setCurrentGeneration(nextGen);
                            session.setCurrentToken(newToken);
                            session.setTokenIssuedAt(now);
                            session.setTokenExpiresAt(expiresAt);

                            firestore.collection(Constants.COLL_SESSIONS).document(sessionId).set(session)
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(session))
                                    .addOnFailureListener(e -> callback.onError("Failed to rotate token: " + e.getMessage()));
                        } else {
                            callback.onError("Session is not active.");
                        }
                    } else {
                        callback.onError("Session not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void setSessionStatus(String sessionId, String status, ActionCallback callback) {
        firestore.collection(Constants.COLL_SESSIONS).document(sessionId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public ListenerRegistration listenLiveAttendance(String sessionId, LiveAttendanceCallback callback) {
        return firestore.collection(Constants.COLL_ATTENDANCE)
                .whereEqualTo("sessionId", sessionId)
                .orderBy("markedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<AttendanceRecord> records = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            AttendanceRecord rec = doc.toObject(AttendanceRecord.class);
                            if (rec != null) {
                                records.add(rec);
                            }
                        }
                        int totalStudents = AttendanceRepository.getInstance(context).getTotalStudentCount();
                        callback.onAttendanceUpdated(records, records.size(), totalStudents);
                    }
                });
    }
}
