# RollCall - Feature Documentation

## 📄 Complete System Features

### 1. Authentication & Security
- Firebase Email/Password Authentication.
- Firestore trusted role lookup (Student, Teacher, Admin).
- Anti-role spoofing protection.
- Password reset email delivery.

### 2. Branding & UX
- Original vector logo (`ic_rollcall_logo.xml`).
- Adaptive launcher icon with graduation cap and QR badge.
- Live Light/Dark mode switcher.
- Smooth transitions and Lottie-compatible XML animations.

### 3. Attendance Engine
- **Dynamic 50s QR Code:** Token rotates every 50 seconds.
- **25m GPS Geofence:** Fused Location Provider proximity check using Haversine formula.
- **Transactional Duplicate Prevention:** Document ID `sessionId_studentId`.
- **Live Roster Listener:** Real-time attendance updates.

### 4. Reports & Exports
- Native PDF report generation (`android.graphics.pdf.PdfDocument`).
- CSV export for Excel analysis.
- PDF/CSV FileProvider sharing.
- Attendance Warning System for students below 75%.
