# RollCall - Firebase & Security Setup Guide

## 1. Firebase Authentication Setup
1. Open [Firebase Console](https://console.firebase.google.com/) and select your RollCall project.
2. Go to **Authentication > Sign-in method**.
3. Enable **Email/Password** authentication provider.

---

## 2. Cloud Firestore Database Collections
Ensure the following collections exist in Firestore:

- **`users`**: Stores user profiles (Fields: `uid`, `name`, `email`, `role`, `rollNumber`, `department`, `employeeId`, `activeStatus`).
- **`sessions`**: Active attendance sessions (Fields: `sessionId`, `teacherId`, `subjectId`, `roomId`, `latitude`, `longitude`, `status`, `currentGeneration`, `currentToken`, `tokenExpiresAt`).
- **`attendance`**: Recorded attendance records (Document ID: `sessionId_studentId`).
- **`rooms`**: Registered classrooms with GPS coordinates.
- **`subjects`**: Course subject details.
- **`enrollments`**: Student-subject enrollment mappings.

---

## 3. Deploy Cloud Functions
Navigate to the `functions/` folder in terminal and deploy:

```bash
cd functions
npm install
firebase deploy --only functions
```

Functions deployed:
- `verifyAndMarkAttendance`: Validates student token, 50s expiration, enrollment, and 25m classroom GPS proximity before recording attendance.

---

## 4. Firestore Security Rules
Deploy the security rules from `firestore.rules`:

```bash
firebase deploy --only firestore:rules
```
