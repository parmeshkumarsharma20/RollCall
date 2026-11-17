const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Distance calculation using Haversine formula
 */
function calculateHaversineDistanceMeters(lat1, lon1, lat2, lon2) {
  const R = 6371000; // Radius of Earth in meters
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) *
      Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

/**
 * Cloud Function: verifyAndMarkAttendance
 * Validates Auth, Student role, Active account, Session active, QR 50s Expiry, Enrollment, Room 25m Radius, Duplicate prevention
 */
exports.verifyAndMarkAttendance = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated.");
  }

  const studentUid = context.auth.uid;
  const { sessionId, token, latitude, longitude, locationAccuracy } = data;

  if (!sessionId || !token || latitude === undefined || longitude === undefined) {
    throw new functions.https.HttpsError("invalid-argument", "Missing required verification details.");
  }

  // 1. Verify Student Profile & Active Status
  const userSnap = await db.collection("users").doc(studentUid).get();
  if (!userSnap.exists) {
    throw new functions.https.HttpsError("not-found", "Student user profile not found.");
  }
  const user = userSnap.data();
  if (user.role !== "STUDENT" && user.role !== "student") {
    throw new functions.https.HttpsError("permission-denied", "Only students can mark attendance.");
  }
  if (user.activeStatus === false) {
    throw new functions.https.HttpsError("permission-denied", "Student account is deactivated.");
  }

  // 2. Retrieve Attendance Session
  const sessionDoc = await db.collection("sessions").doc(sessionId).get();
  if (!sessionDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Attendance session not found.");
  }
  const session = sessionDoc.data();

  if (session.status !== "active") {
    throw new functions.https.HttpsError("failed-precondition", session.status === "paused" ? "Attendance session is currently paused." : "Attendance session has ended.");
  }

  // 3. Token Validity & Expiry (50 Seconds Server Timestamp Check)
  const now = Date.now();
  if (session.currentToken !== token) {
    throw new functions.https.HttpsError("invalid-argument", "Invalid or outdated QR code scanned.");
  }
  if (session.tokenExpiresAt && now > session.tokenExpiresAt) {
    throw new functions.https.HttpsError("deadline-exceeded", "QR code has expired. Please scan the latest QR code.");
  }

  // 4. Enrollment Check
  const enrollmentQuery = await db
    .collection("enrollments")
    .where("studentId", "==", studentUid)
    .where("subjectId", "==", session.subjectId)
    .get();

  if (enrollmentQuery.empty) {
    // If enrollments collection is empty or not enforced, allow classId check fallback
    if (user.classId && user.classId !== session.classId) {
      throw new functions.https.HttpsError("permission-denied", "You are not enrolled in this class/subject.");
    }
  }

  // 5. Proximity Location Verification (25m Classroom Radius)
  const roomLat = session.latitude || 28.6139;
  const roomLng = session.longitude || 77.2090;
  const maxRadius = session.allowedRadiusMeters || 25.0;

  const distanceMeters = calculateHaversineDistanceMeters(latitude, longitude, roomLat, roomLng);

  if (distanceMeters > maxRadius) {
    throw new functions.https.HttpsError(
      "out-of-range",
      `Location verification failed. You are ${Math.round(distanceMeters)}m away from the classroom (Max allowed: ${maxRadius}m).`
    );
  }

  // 6. Duplicate Attendance Check & Transactional Record Write
  const attendanceDocId = `${sessionId}_${studentUid}`;
  const attendanceRef = db.collection("attendance").doc(attendanceDocId);

  try {
    await db.runTransaction(async (transaction) => {
      const doc = await transaction.get(attendanceRef);
      if (doc.exists) {
        throw new functions.https.HttpsError("already-exists", "Your attendance has already been recorded for this session.");
      }

      transaction.set(attendanceRef, {
        recordId: attendanceDocId,
        studentId: studentUid,
        studentName: user.name || "Student",
        rollNumber: user.rollNumber || "N/A",
        sessionId: sessionId,
        classId: session.classId,
        subjectId: session.subjectId,
        markedAt: now,
        timestamp: now,
        distanceMeters: Math.round(distanceMeters * 10) / 10,
        locationAccuracyMeters: locationAccuracy || 0,
        status: "PRESENT",
      });
    });

    return {
      success: true,
      message: "Attendance marked successfully!",
      distanceMeters: Math.round(distanceMeters * 10) / 10,
      markedAt: now,
      studentName: user.name,
      rollNumber: user.rollNumber,
      subjectName: session.subjectName,
      roomNumber: session.roomNumber,
    };
  } catch (error) {
    if (error.code === "already-exists") {
      throw error;
    }
    throw new functions.https.HttpsError("internal", error.message || "Failed to record attendance.");
  }
});
