package com.example.rollcall;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.QRPayload;
import com.example.rollcall.model.User;
import com.example.rollcall.util.QRGenerator;
import com.example.rollcall.utils.Constants;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.UUID;

public class TeacherActivity extends AppCompatActivity {

    private static final int DURATION_SECONDS = Constants.QR_VALIDITY_SECONDS;
    private static final double DEFAULT_LAT = 28.6139;
    private static final double DEFAULT_LNG = 77.2090;

    private TextView tvTeacherAvatarInitials, tvTeacherName, tvRoomName, tvPresentCounter, tvTimerText;
    private ImageView imgQRCode;
    private ProgressBar pbTimer;
    private Button btnViewAttendanceList, btnCreateNewSession;
    private ImageButton btnLogoutTeacher;
    private FrameLayout frameQR;

    private AttendanceRepository repository;
    private AuthRepository authRepo;
    private CountDownTimer countDownTimer;
    private String currentSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        repository = AttendanceRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        tvTeacherAvatarInitials = findViewById(R.id.tvTeacherAvatarInitials);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvPresentCounter = findViewById(R.id.tvPresentCounter);
        tvTimerText = findViewById(R.id.tvTimerText);
        imgQRCode = findViewById(R.id.imgQRCode);
        pbTimer = findViewById(R.id.pbTimer);
        btnViewAttendanceList = findViewById(R.id.btnViewAttendanceList);
        btnCreateNewSession = findViewById(R.id.btnCreateNewSession);
        btnLogoutTeacher = findViewById(R.id.btnLogoutTeacher);
        frameQR = findViewById(R.id.frameQR);

        User currentUser = authRepo.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getName() != null ? currentUser.getName() : "Faculty Teacher";
            String dept = currentUser.getDepartment() != null ? currentUser.getDepartment() : "Computer Science";

            if (tvTeacherName != null) tvTeacherName.setText(name);
            if (tvRoomName != null) tvRoomName.setText(dept + " • Room 301");

            if (name.length() >= 2 && tvTeacherAvatarInitials != null) {
                tvTeacherAvatarInitials.setText(name.substring(0, 2).toUpperCase());
            }
        }

        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        if (frameQR != null && pulse != null) {
            frameQR.startAnimation(pulse);
        }

        if (btnViewAttendanceList != null) {
            btnViewAttendanceList.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherActivity.this, AttendanceListActivity.class);
                startActivity(intent);
            });
        }

        if (btnCreateNewSession != null) {
            btnCreateNewSession.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherActivity.this, CreateSessionActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogoutTeacher != null) {
            btnLogoutTeacher.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        startQRGenerationCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAttendanceCounter();
    }

    private void updateAttendanceCounter() {
        int present = repository.getPresentCount();
        int total = repository.getTotalStudentCount();
        if (tvPresentCounter != null) {
            tvPresentCounter.setText(present + " / " + total);
        }
    }

    private void startQRGenerationCycle() {
        currentSessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);
        generateAndDisplayQR();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (pbTimer != null) pbTimer.setMax(DURATION_SECONDS);

        countDownTimer = new CountDownTimer(DURATION_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                if (pbTimer != null) pbTimer.setProgress(secondsLeft);
                if (tvTimerText != null) tvTimerText.setText(secondsLeft + "s remaining");
                updateAttendanceCounter();
            }

            @Override
            public void onFinish() {
                if (tvTimerText != null) tvTimerText.setText("Regenerating...");
                startQRGenerationCycle();
            }
        }.start();
    }

    private void generateAndDisplayQR() {
        try {
            long now = System.currentTimeMillis();
            QRPayload payload = new QRPayload(
                    currentSessionId,
                    "Room 301",
                    now,
                    DEFAULT_LAT,
                    DEFAULT_LNG,
                    DURATION_SECONDS
            );

            Bitmap bitmap = QRGenerator.generateQRCode(payload.toJsonString(), 500, 500);
            if (imgQRCode != null) imgQRCode.setImageBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of RollCall?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authRepo.logout();
                    Toast.makeText(TeacherActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
