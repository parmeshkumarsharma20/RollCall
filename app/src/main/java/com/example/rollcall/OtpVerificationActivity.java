package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.example.rollcall.data.PhoneAuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationActivity extends AppCompatActivity {

    private String verificationId;
    private String rawPhone;
    private String selectedRole;
    private String regName, regRoll, regDept;
    private boolean isRegistrationFlow = false;
    private boolean isForgotPasswordFlow = false;

    private EditText etOtpCode, etOtpPassword, etOtpConfirmPassword;
    private Button btnVerifyOtp, btnResendOtp, btnChangeNumber;
    private TextView tvMaskedPhoneSub, tvResendTimer;
    private View layoutPasswordFields;
    private CountDownTimer timer;

    private PhoneAuthRepository phoneAuthRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        phoneAuthRepo = PhoneAuthRepository.getInstance(this);

        verificationId = getIntent().getStringExtra("extra_verification_id");
        rawPhone = getIntent().getStringExtra("extra_raw_phone");
        selectedRole = getIntent().getStringExtra("extra_role");
        if (selectedRole == null) selectedRole = User.ROLE_STUDENT;

        regName = getIntent().getStringExtra("extra_name");
        regRoll = getIntent().getStringExtra("extra_roll");
        regDept = getIntent().getStringExtra("extra_dept");
        isRegistrationFlow = getIntent().getBooleanExtra("is_registration_flow", false);
        isForgotPasswordFlow = getIntent().getBooleanExtra("is_forgot_password_flow", false);

        etOtpCode = findViewById(R.id.etOtpCode);
        etOtpPassword = findViewById(R.id.etOtpPassword);
        etOtpConfirmPassword = findViewById(R.id.etOtpConfirmPassword);

        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        btnChangeNumber = findViewById(R.id.btnChangeNumber);

        tvMaskedPhoneSub = findViewById(R.id.tvMaskedPhoneSub);
        tvResendTimer = findViewById(R.id.tvResendTimer);
        layoutPasswordFields = findViewById(R.id.layoutPasswordFields);

        CardView cardOtp = findViewById(R.id.cardOtp);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (cardOtp != null && slideUp != null) {
            cardOtp.startAnimation(slideUp);
        }

        if (isRegistrationFlow || isForgotPasswordFlow) {
            if (layoutPasswordFields != null) layoutPasswordFields.setVisibility(View.VISIBLE);
            if (btnVerifyOtp != null) {
                btnVerifyOtp.setText(isForgotPasswordFlow ? "RESET PASSWORD & SIGN IN 🔒" : "VERIFY OTP & CREATE ACCOUNT 🔒");
            }
        } else {
            if (layoutPasswordFields != null) layoutPasswordFields.setVisibility(View.GONE);
        }

        if (rawPhone != null && tvMaskedPhoneSub != null) {
            String masked = rawPhone.length() >= 10 ? "+91 ***** **" + rawPhone.substring(rawPhone.length() - 4) : rawPhone;
            tvMaskedPhoneSub.setText("Enter 6-digit SMS OTP sent to " + masked);
        }

        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> performOtpVerification());
        }

        if (btnChangeNumber != null) {
            btnChangeNumber.setOnClickListener(v -> finish());
        }

        if (btnResendOtp != null) {
            btnResendOtp.setOnClickListener(v -> resendOtpCode());
        }

        startResendTimer();
    }

    private void startResendTimer() {
        if (btnResendOtp != null) btnResendOtp.setEnabled(false);

        if (timer != null) timer.cancel();

        timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvResendTimer != null) {
                    tvResendTimer.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
                }
            }

            @Override
            public void onFinish() {
                if (tvResendTimer != null) tvResendTimer.setText("Didn't receive SMS OTP?");
                if (btnResendOtp != null) btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void resendOtpCode() {
        if (TextUtils.isEmpty(rawPhone)) return;

        Toast.makeText(this, "Resending OTP code...", Toast.LENGTH_SHORT).show();
        phoneAuthRepo.sendOtp(rawPhone, this, new PhoneAuthRepository.OtpSendCallback() {
            @Override
            public void onCodeSent(String newId, PhoneAuthProvider.ForceResendingToken token) {
                verificationId = newId;
                Toast.makeText(OtpVerificationActivity.this, "New OTP sent!", Toast.LENGTH_SHORT).show();
                startResendTimer();
            }

            @Override
            public void onVerificationCompleted(com.google.firebase.auth.PhoneAuthCredential credential) {
                performDirectPhoneSignIn(credential);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performOtpVerification() {
        String code = etOtpCode.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() < 6) {
            etOtpCode.setError("Enter 6-digit OTP code");
            etOtpCode.requestFocus();
            return;
        }

        final String pass = !TextUtils.isEmpty(etOtpPassword.getText().toString().trim()) ? etOtpPassword.getText().toString().trim() : "123456";

        if (isRegistrationFlow) {
            saveRegistrationUserAndNavigate(pass);
            return;
        }

        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Verifying OTP...");

        if (isForgotPasswordFlow) {
            phoneAuthRepo.resetPhonePasswordWithOtp(verificationId, code, rawPhone, pass, new PhoneAuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    btnVerifyOtp.setEnabled(true);
                    Toast.makeText(OtpVerificationActivity.this, "Password Reset Successful! Logging in...", Toast.LENGTH_SHORT).show();
                    handleUserNavigation(user);
                }

                @Override
                public void onError(String message) {
                    btnVerifyOtp.setEnabled(true);
                    btnVerifyOtp.setText("RESET PASSWORD & SIGN IN 🔒");
                    Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        phoneAuthRepo.verifyOtp(verificationId, code, rawPhone, selectedRole, new PhoneAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                btnVerifyOtp.setEnabled(true);
                if (isRegistrationFlow) {
                    saveRegistrationUserAndNavigate(pass);
                } else {
                    handleUserNavigation(user);
                }
            }

            @Override
            public void onError(String message) {
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText(isRegistrationFlow ? "VERIFY OTP & CREATE ACCOUNT 🔒" : "VERIFY OTP & SIGN IN 🔒");
                Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveRegistrationUserAndNavigate(String password) {
        String normalizedPhone = PhoneAuthRepository.normalizePhoneNumber(rawPhone);
        String uid = "u_ph_" + System.currentTimeMillis();

        User newUser = new User(uid, !TextUtils.isEmpty(regName) ? regName : "User", normalizedPhone + "@rollcall.phone", selectedRole);
        newUser.setPhoneNumber(normalizedPhone);
        newUser.setPassword(password);
        newUser.setAccountStatus(User.STATUS_ACTIVE);
        newUser.setActiveStatus(true);

        if (User.ROLE_TEACHER.equalsIgnoreCase(selectedRole)) {
            newUser.setEmployeeId(!TextUtils.isEmpty(regRoll) ? regRoll : "EMP" + (System.currentTimeMillis() % 1000));
            newUser.setDepartment(!TextUtils.isEmpty(regDept) ? regDept : "Computer Science");
        } else {
            newUser.setRollNumber(!TextUtils.isEmpty(regRoll) ? regRoll : "RC" + (System.currentTimeMillis() % 1000));
            newUser.setCourse(!TextUtils.isEmpty(regDept) ? regDept : "B.Tech CSE");
            newUser.setDepartment(!TextUtils.isEmpty(regDept) ? regDept : "Computer Science");
            newUser.setSemester("1st");
            newUser.setSection("A");
        }

        phoneAuthRepo.savePhoneProfile(newUser, new PhoneAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(OtpVerificationActivity.this, "Account Created! Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                handleUserNavigation(user);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OtpVerificationActivity.this, "Account created: " + message, Toast.LENGTH_SHORT).show();
                handleUserNavigation(newUser);
            }
        });
    }

    private void performDirectPhoneSignIn(com.google.firebase.auth.PhoneAuthCredential credential) {
        phoneAuthRepo.signInWithPhoneCredential(credential, rawPhone, selectedRole, new PhoneAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                handleUserNavigation(user);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleUserNavigation(User user) {
        Intent intent;
        if (user != null && User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(OtpVerificationActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(OtpVerificationActivity.this, StudentDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
