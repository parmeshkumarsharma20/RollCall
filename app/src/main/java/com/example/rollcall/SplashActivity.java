package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.data.PhoneAuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.PreferenceManager;
import com.example.rollcall.utils.ThemeManager;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        ImageView imgLogo = findViewById(R.id.imgSplashLogo);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        if (imgLogo != null && scaleUp != null) {
            imgLogo.startAnimation(scaleUp);
        }

        syncDefaultUsersToFirestore();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigation, 1500);
    }

    private void syncDefaultUsersToFirestore() {
        try {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            User admin = new User("admin_1", "Parmesh Kumar Sharma", "sharmaparmeshkumar8@gmail.com", "admin123", User.ROLE_ADMIN, "ADM001", "Administration");
            admin.setPhoneNumber("+918882062772");
            admin.setPassword("admin123");

            User teacher = new User("teach_1", "Teacher Demo", "teacher@rollcall.com", "teacher123", User.ROLE_TEACHER, "EMP101", "Computer Science");
            teacher.setPhoneNumber("+919999999999");
            teacher.setPassword("teacher123");

            User student = new User("stud_555", "Student Demo", "student@rollcall.com", "student123", User.ROLE_STUDENT, "RC2024555", "Computer Science");
            student.setPhoneNumber("+915555555555");
            student.setPassword("student123");
            student.setCourse("B.Tech CSE");
            student.setSemester("1st");
            student.setSection("A");

            db.collection("users").document(admin.getUid()).set(admin.toMap());
            db.collection("users").document(teacher.getUid()).set(teacher.toMap());
            db.collection("users").document(student.getUid()).set(student.toMap());
        } catch (Exception ignored) {}
    }

    private void checkNavigation() {
        PreferenceManager prefManager = new PreferenceManager(this);
        PhoneAuthRepository phoneRepo = PhoneAuthRepository.getInstance(this);
        AuthRepository authRepo = AuthRepository.getInstance(this);

        if (prefManager.isLoggedIn()) {
            User currentUser = phoneRepo.getCurrentUser() != null ? phoneRepo.getCurrentUser() : authRepo.getCurrentUser();
            if (currentUser == null) {
                String role = prefManager.getLoggedInRole();
                if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
                    currentUser = new User("admin_1", "Parmesh Kumar Sharma", "sharmaparmeshkumar8@gmail.com", "admin123", User.ROLE_ADMIN, "ADM001", "Administration");
                } else if (User.ROLE_TEACHER.equalsIgnoreCase(role)) {
                    currentUser = new User("teach_1", "Teacher Demo", "teacher@rollcall.com", "teacher123", User.ROLE_TEACHER, "EMP101", "Computer Science");
                } else {
                    currentUser = new User("stud_555", "Student Demo", "student@rollcall.com", "student123", User.ROLE_STUDENT, "RC2024555", "Computer Science");
                }
                authRepo.setCurrentUser(currentUser);
                phoneRepo.setCurrentUser(currentUser);
            }
            navigateToDashboard(currentUser);
            return;
        }

        navigateToOnboardingOrRoleSelection();
    }

    private void navigateToOnboardingOrRoleSelection() {
        PreferenceManager prefManager = new PreferenceManager(this);
        Intent intent;
        if (prefManager.isOnboardingCompleted()) {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard(User user) {
        Intent intent;
        if (User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
        } else if (User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(SplashActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
