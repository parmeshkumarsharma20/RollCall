# RollCall - Phase 1, 2 & 3 Testing Report

## 🧪 Test Matrix & Verification Results

| Test Scenario | User Role | Action Performed | Expected Result | Pass/Fail |
|---|---|---|---|---|
| First Launch | Guest | App launch | Splash animation -> Onboarding slides | PASS |
| Role Selection | Guest | Select Student/Teacher/Admin | Navigates to Login with chosen role | PASS |
| Demo Login | Admin | Quick-fill admin credentials | Authenticates & opens Admin Dashboard | PASS |
| User Management | Admin | Add Student/Teacher | Saves profile to Firestore & local DB | PASS |
| Session Creation | Teacher | Select Subject & Room | Creates session & launches 50s QR | PASS |
| 50s QR Rotation | Teacher | Wait 50 seconds | Token rotates & fresh QR displays automatically | PASS |
| Valid QR Scan | Student | Scan active QR within 25m | Verification succeeds -> Success Result Screen | PASS |
| Expired QR Scan | Student | Scan QR > 50s old | Rejected with "QR Code Expired" message | PASS |
| Out of Range Scan | Student | Scan from > 25m away | Rejected with "Location Out of Range" message | PASS |
| Duplicate Scan | Student | Scan same QR twice | Rejected with "Already Marked" message | PASS |
| Live Roster Sync | Teacher | Student marks attendance | Live present counter updates immediately | PASS |
| Report Export | Admin | Click Export PDF / CSV | Generates PDF/CSV and triggers Share Intent | PASS |
| Dark Mode | All | Toggle theme in Settings | UI adapts to Dark Navy palette seamlessly | PASS |
