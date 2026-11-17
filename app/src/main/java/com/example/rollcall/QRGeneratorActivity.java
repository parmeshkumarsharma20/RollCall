package com.example.rollcall;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.SessionRepository;
import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.AttendanceSession;
import com.example.rollcall.model.QRPayload;
import com.example.rollcall.util.QRGenerator;
import com.example.rollcall.utils.Constants;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Locale;

public class QRGeneratorActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION = "extra_attendance_session";

    private SessionRepository sessionRepo;

    private AttendanceSession currentSession;
    private CountDownTimer countDownTimer;
    private ListenerRegistration liveListener;

    private ImageView imgQR;
    private ProgressBar pbTimer;
    private TextView tvTimerText, tvSubject, tvRoomDetails, tvPresentCount, tvStatusSub;
    private Button btnPauseResume;

    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        sessionRepo = SessionRepository.getInstance(this);

        currentSession = (AttendanceSession) getIntent().getSerializableExtra(EXTRA_SESSION);

        ImageButton btnBack = findViewById(R.id.btnBackQR);
        imgQR = findViewById(R.id.imgDynamicQR);
        pbTimer = findViewById(R.id.pbQRTimer);
        tvTimerText = findViewById(R.id.tvQRTimerText);
        tvSubject = findViewById(R.id.tvSessionSubjectName);
        tvRoomDetails = findViewById(R.id.tvSessionRoomDetails);
        tvPresentCount = findViewById(R.id.tvLivePresentCount);
        tvStatusSub = findViewById(R.id.tvQRStatusSub);

        btnPauseResume = findViewById(R.id.btnPauseResumeSession);
        Button btnEndSession = findViewById(R.id.btnEndSession);
        Button btnViewRoster = findViewById(R.id.btnViewLiveRoster);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (currentSession != null) {
            if (tvSubject != null) tvSubject.setText(currentSession.getSubjectName());
            if (tvRoomDetails != null) tvRoomDetails.setText(currentSession.getRoomNumber() + " • 25m Radius");
        }

        if (tvPresentCount != null) {
            int totalStudents = com.example.rollcall.data.AttendanceRepository.getInstance(this).getTotalStudentCount();
            int presentCount = com.example.rollcall.data.AttendanceRepository.getInstance(this).getPresentCount();
            tvPresentCount.setText(presentCount + " / " + totalStudents);
        }

        if (btnPauseResume != null) btnPauseResume.setOnClickListener(v -> togglePauseResume());
        if (btnEndSession != null) btnEndSession.setOnClickListener(v -> confirmEndSession());
        if (btnViewRoster != null) {
            btnViewRoster.setOnClickListener(v -> {
                Intent intent = new Intent(QRGeneratorActivity.this, AttendanceListActivity.class);
                startActivity(intent);
            });
        }

        startQRCycle();
        startLiveAttendanceListener();
    }

    private void startQRCycle() {
        if (isPaused || currentSession == null) return;

        displayCurrentQR();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        int duration = Constants.QR_VALIDITY_SECONDS;
        if (pbTimer != null) pbTimer.setMax(duration);

        countDownTimer = new CountDownTimer(duration * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                if (pbTimer != null) pbTimer.setProgress(secondsLeft);
                if (tvTimerText != null) tvTimerText.setText(String.format(Locale.US, "00:%02d", secondsLeft));
            }

            @Override
            public void onFinish() {
                if (tvTimerText != null) tvTimerText.setText("00:00");
                rotateQRToken();
            }
        }.start();
    }

    private void displayCurrentQR() {
        try {
            if (currentSession == null) return;
            long now = System.currentTimeMillis();
            QRPayload payload = new QRPayload(
                    currentSession.getSessionId(),
                    currentSession.getRoomNumber() != null ? currentSession.getRoomNumber() : "Room 301",
                    now,
                    currentSession.getLatitude(),
                    currentSession.getLongitude(),
                    Constants.QR_VALIDITY_SECONDS
            );
            payload.setCourseName(currentSession.getCourseName() != null ? currentSession.getCourseName() : "");
            payload.setSubjectName(currentSession.getSubjectName() != null ? currentSession.getSubjectName() : "");

            Bitmap bitmap = QRGenerator.generateQRCode(payload.toJsonString(), 500, 500);
            if (imgQR != null) imgQR.setImageBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void rotateQRToken() {
        if (isPaused || currentSession == null) return;

        sessionRepo.rotateQRToken(currentSession.getSessionId(), new SessionRepository.SessionCallback() {
            @Override
            public void onSuccess(AttendanceSession session) {
                currentSession = session;
                startQRCycle();
            }

            @Override
            public void onError(String message) {
                startQRCycle();
            }
        });
    }

    private void togglePauseResume() {
        if (currentSession == null) return;

        if (!isPaused) {
            isPaused = true;
            if (countDownTimer != null) countDownTimer.cancel();
            if (btnPauseResume != null) btnPauseResume.setText("Resume Session");
            if (tvStatusSub != null) tvStatusSub.setText("⏸️ Attendance Session PAUSED");

            sessionRepo.setSessionStatus(currentSession.getSessionId(), AttendanceSession.STATUS_PAUSED, new SessionRepository.ActionCallback() {
                @Override public void onSuccess() { Toast.makeText(QRGeneratorActivity.this, "Session Paused", Toast.LENGTH_SHORT).show(); }
                @Override public void onError(String message) {}
            });
        } else {
            isPaused = false;
            if (btnPauseResume != null) btnPauseResume.setText("Pause Session");
            if (tvStatusSub != null) tvStatusSub.setText("⚡ QR Token Active • Auto-rotating every 50s");

            sessionRepo.setSessionStatus(currentSession.getSessionId(), AttendanceSession.STATUS_ACTIVE, new SessionRepository.ActionCallback() {
                @Override public void onSuccess() {
                    Toast.makeText(QRGeneratorActivity.this, "Session Resumed", Toast.LENGTH_SHORT).show();
                    rotateQRToken();
                }
                @Override public void onError(String message) {}
            });
        }
    }

    private void confirmEndSession() {
        new AlertDialog.Builder(this)
                .setTitle("End Attendance Session?")
                .setMessage("This will stop QR generation and finalize the attendance session.")
                .setPositiveButton("End Session", (dialog, which) -> {
                    if (countDownTimer != null) countDownTimer.cancel();
                    if (currentSession != null) {
                        sessionRepo.setSessionStatus(currentSession.getSessionId(), AttendanceSession.STATUS_ENDED, new SessionRepository.ActionCallback() {
                            @Override public void onSuccess() {
                                Toast.makeText(QRGeneratorActivity.this, "Session Ended Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            @Override public void onError(String message) {
                                finish();
                            }
                        });
                    } else {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startLiveAttendanceListener() {
        if (currentSession == null) return;
        liveListener = sessionRepo.listenLiveAttendance(currentSession.getSessionId(), new SessionRepository.LiveAttendanceCallback() {
            @Override
            public void onAttendanceUpdated(List<AttendanceRecord> records, int presentCount, int totalCount) {
                if (tvPresentCount != null) {
                    tvPresentCount.setText(presentCount + " / " + totalCount);
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (liveListener != null) liveListener.remove();
    }
}
