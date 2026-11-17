package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.data.PhoneAuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.PreferenceManager;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButtonToggleGroup toggleGroupRole;
    private String currentSelectedRole = User.ROLE_STUDENT;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        authRepository = AuthRepository.getInstance(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        toggleGroupRole = findViewById(R.id.toggleGroupRole);

        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);
        Button btnDemoAdmin = findViewById(R.id.btnDemoAdmin);
        Button btnDemoTeacher = findViewById(R.id.btnDemoTeacher);
        Button btnDemoStudent = findViewById(R.id.btnDemoStudent);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvContactAdmin = findViewById(R.id.tvContactAdmin);

        CardView cardLogin = findViewById(R.id.cardLogin);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (cardLogin != null && slideUp != null) {
            cardLogin.startAnimation(slideUp);
        }

        String passedRole = getIntent().getStringExtra(RoleSelectionActivity.EXTRA_SELECTED_ROLE);
        if (passedRole != null) {
            currentSelectedRole = passedRole;
        } else {
            PreferenceManager pref = new PreferenceManager(this);
            if (pref.getSelectedRole() != null) {
                currentSelectedRole = pref.getSelectedRole();
            }
        }

        if (toggleGroupRole != null) {
            if (User.ROLE_TEACHER.equalsIgnoreCase(currentSelectedRole)) {
                toggleGroupRole.check(R.id.btnRoleTeacher);
            } else if (User.ROLE_ADMIN.equalsIgnoreCase(currentSelectedRole)) {
                toggleGroupRole.check(R.id.btnRoleAdmin);
            } else {
                toggleGroupRole.check(R.id.btnRoleStudent);
            }

            toggleGroupRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnRoleStudent) {
                        currentSelectedRole = User.ROLE_STUDENT;
                    } else if (checkedId == R.id.btnRoleTeacher) {
                        currentSelectedRole = User.ROLE_TEACHER;
                    } else if (checkedId == R.id.btnRoleAdmin) {
                        currentSelectedRole = User.ROLE_ADMIN;
                    }
                }
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> attemptLogin());
        }

        if (btnGoToRegister != null) {
            btnGoToRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        if (btnDemoAdmin != null) {
            btnDemoAdmin.setOnClickListener(v -> fillDemoCredentials("8882062772", "admin123", User.ROLE_ADMIN));
        }

        if (btnDemoTeacher != null) {
            btnDemoTeacher.setOnClickListener(v -> fillDemoCredentials("9999999999", "teacher123", User.ROLE_TEACHER));
        }

        if (btnDemoStudent != null) {
            btnDemoStudent.setOnClickListener(v -> fillDemoCredentials("5555555555", "student123", User.ROLE_STUDENT));
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        if (tvContactAdmin != null) {
            tvContactAdmin.setOnClickListener(v -> Toast.makeText(this, "Please contact your institution administrator to request account credentials.", Toast.LENGTH_LONG).show());
        }
    }

    private void fillDemoCredentials(String identifier, String password, String role) {
        currentSelectedRole = role;
        if (toggleGroupRole != null) {
            if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
                toggleGroupRole.check(R.id.btnRoleAdmin);
            } else if (User.ROLE_TEACHER.equalsIgnoreCase(role)) {
                toggleGroupRole.check(R.id.btnRoleTeacher);
            } else {
                toggleGroupRole.check(R.id.btnRoleStudent);
            }
        }

        if (etEmail != null) etEmail.setText(identifier);
        if (etPassword != null) etPassword.setText(password);

        Toast.makeText(this, role + " demo auto-filled! Signing in...", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> performDirectLogin(identifier, password, role), 800);
    }

    private void performDirectLogin(String identifier, String password, String role) {
        currentSelectedRole = role;

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        PhoneAuthRepository.getInstance(this).loginWithPhoneAndPassword(identifier, password, role, new PhoneAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                btnLogin.setEnabled(true);
                btnLogin.setText("SIGN IN TO ACCOUNT 🔒");
                authRepository.setCurrentUser(user);
                PhoneAuthRepository.getInstance(LoginActivity.this).setCurrentUser(user);
                AttendanceRepository.getInstance(LoginActivity.this).setCurrentUser(user);
                Toast.makeText(LoginActivity.this, "Welcome back, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                redirectUserToDashboard(user);
            }

            @Override
            public void onError(String phoneMsg) {
                authRepository.loginUser(identifier, password, role, new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("SIGN IN TO ACCOUNT 🔒");
                        authRepository.setCurrentUser(user);
                        PhoneAuthRepository.getInstance(LoginActivity.this).setCurrentUser(user);
                        AttendanceRepository.getInstance(LoginActivity.this).setCurrentUser(user);
                        Toast.makeText(LoginActivity.this, "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                        redirectUserToDashboard(user);
                    }

                    @Override
                    public void onError(String emailMsg) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("SIGN IN TO ACCOUNT 🔒");
                        Toast.makeText(LoginActivity.this, phoneMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void attemptLogin() {
        String identifier = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(identifier)) {
            etEmail.setError("Please enter your mobile number or email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        performDirectLogin(identifier, password, currentSelectedRole);
    }

    private void redirectUserToDashboard(User user) {
        if (user != null) {
            PreferenceManager prefManager = new PreferenceManager(this);
            prefManager.setLoggedInUser(user.getUid() != null ? user.getUid() : "u_demo", user.getRole());
            authRepository.setCurrentUser(user);
            PhoneAuthRepository.getInstance(this).setCurrentUser(user);
            AttendanceRepository.getInstance(this).setCurrentUser(user);
        }

        Intent intent;
        if (user != null && User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else if (user != null && User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
