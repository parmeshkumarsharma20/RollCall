package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.PhoneAuthRepository;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotMobile;
    private Button btnSendResetLink;
    private PhoneAuthRepository phoneAuthRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        phoneAuthRepo = PhoneAuthRepository.getInstance(this);

        etForgotMobile = findViewById(R.id.etForgotMobile);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        ImageButton btnBack = findViewById(R.id.btnBackForgotHeader);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        View.OnClickListener goBack = v -> finish();
        if (btnBack != null) btnBack.setOnClickListener(goBack);
        if (tvBackToLogin != null) tvBackToLogin.setOnClickListener(goBack);

        if (btnSendResetLink != null) {
            btnSendResetLink.setOnClickListener(v -> initiatePasswordResetOtp());
        }
    }

    private void initiatePasswordResetOtp() {
        String mobile = etForgotMobile.getText().toString().trim();

        if (TextUtils.isEmpty(mobile) || mobile.length() < 10) {
            etForgotMobile.setError("Please enter a valid 10-digit mobile number");
            etForgotMobile.requestFocus();
            return;
        }

        btnSendResetLink.setEnabled(false);
        btnSendResetLink.setText("Sending OTP...");

        phoneAuthRepo.sendOtp(mobile, this, new PhoneAuthRepository.OtpSendCallback() {
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                btnSendResetLink.setEnabled(true);
                btnSendResetLink.setText("SEND RESET OTP 📲");
                Toast.makeText(ForgotPasswordActivity.this, "Reset OTP sent to " + mobile, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                intent.putExtra("extra_verification_id", verificationId);
                intent.putExtra("extra_raw_phone", mobile);
                intent.putExtra("is_forgot_password_flow", true);
                startActivity(intent);
                finish();
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                btnSendResetLink.setEnabled(true);
                btnSendResetLink.setText("SEND RESET OTP 📲");
            }

            @Override
            public void onError(String message) {
                btnSendResetLink.setEnabled(true);
                btnSendResetLink.setText("SEND RESET OTP 📲");
                Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
