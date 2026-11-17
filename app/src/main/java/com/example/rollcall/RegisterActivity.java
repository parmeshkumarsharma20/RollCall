package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.rollcall.data.PhoneAuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etMobile, etRoll, etDept;
    private MaterialButtonToggleGroup toggleGroupRegRole;
    private Button btnSubmitRegister, btnGoToLogin;
    private PhoneAuthRepository phoneAuthRepo;

    private String currentSelectedRole = User.ROLE_STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        phoneAuthRepo = PhoneAuthRepository.getInstance(this);

        etName = findViewById(R.id.etRegName);
        etMobile = findViewById(R.id.etRegMobile);
        etRoll = findViewById(R.id.etRegRoll);
        etDept = findViewById(R.id.etRegDept);
        toggleGroupRegRole = findViewById(R.id.toggleGroupRegRole);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        CardView cardRegister = findViewById(R.id.cardRegister);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (cardRegister != null && slideUp != null) {
            cardRegister.startAnimation(slideUp);
        }

        if (toggleGroupRegRole != null) {
            toggleGroupRegRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.rbTeacher) {
                        currentSelectedRole = User.ROLE_TEACHER;
                    } else {
                        currentSelectedRole = User.ROLE_STUDENT;
                    }
                }
            });
        }

        btnSubmitRegister.setOnClickListener(v -> initiateRegistrationOtp());

        View.OnClickListener backToLogin = v -> finish();
        if (btnGoToLogin != null) btnGoToLogin.setOnClickListener(backToLogin);
    }

    private void initiateRegistrationOtp() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();
        String dept = etDept.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(mobile) || mobile.length() < 10) {
            etMobile.setError("Please enter a valid 10-digit mobile number");
            etMobile.requestFocus();
            return;
        }

        btnSubmitRegister.setEnabled(false);
        btnSubmitRegister.setText("Sending OTP...");

        phoneAuthRepo.sendOtp(mobile, this, new PhoneAuthRepository.OtpSendCallback() {
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                btnSubmitRegister.setEnabled(true);
                btnSubmitRegister.setText("VERIFY PHONE & SEND OTP 📲");
                Toast.makeText(RegisterActivity.this, "OTP Triggered! Enter Test Code: 123456", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                intent.putExtra("extra_verification_id", verificationId);
                intent.putExtra("extra_raw_phone", mobile);
                intent.putExtra("extra_role", currentSelectedRole);
                intent.putExtra("extra_name", name);
                intent.putExtra("extra_roll", roll);
                intent.putExtra("extra_dept", dept);
                intent.putExtra("is_registration_flow", true);
                startActivity(intent);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                btnSubmitRegister.setEnabled(true);
                btnSubmitRegister.setText("VERIFY PHONE & SEND OTP 📲");
            }

            @Override
            public void onError(String message) {
                btnSubmitRegister.setEnabled(true);
                btnSubmitRegister.setText("VERIFY PHONE & SEND OTP 📲");
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
