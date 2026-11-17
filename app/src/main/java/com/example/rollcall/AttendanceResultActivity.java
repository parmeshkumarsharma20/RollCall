package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.utils.WindowInsetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceResultActivity extends AppCompatActivity {

    public static final String EXTRA_SUCCESS = "extra_result_success";
    public static final String EXTRA_TITLE = "extra_result_title";
    public static final String EXTRA_MESSAGE = "extra_result_message";
    public static final String EXTRA_STUDENT = "extra_result_student";
    public static final String EXTRA_DISTANCE = "extra_result_distance";
    public static final String EXTRA_SUBJECT = "extra_result_subject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_result);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        ImageView imgIcon = findViewById(R.id.imgResultStatusIcon);
        TextView tvTitle = findViewById(R.id.tvResultTitle);
        TextView tvSubtitle = findViewById(R.id.tvResultSubtitle);
        TextView tvStudent = findViewById(R.id.tvResultStudentName);
        TextView tvSubjectInfo = findViewById(R.id.tvResultSubjectInfo);
        TextView tvDistance = findViewById(R.id.tvResultDistance);
        TextView tvTimestamp = findViewById(R.id.tvResultTimestamp);
        TextView tvStatusBadge = findViewById(R.id.tvResultStatusBadge);

        Button btnDone = findViewById(R.id.btnDoneResult);
        Button btnViewRoster = findViewById(R.id.btnViewRosterResult);

        boolean isSuccess = getIntent().getBooleanExtra(EXTRA_SUCCESS, true);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String message = getIntent().getStringExtra(EXTRA_MESSAGE);
        String studentName = getIntent().getStringExtra(EXTRA_STUDENT);
        String subjectInfo = getIntent().getStringExtra(EXTRA_SUBJECT);
        float distance = getIntent().getFloatExtra(EXTRA_DISTANCE, 12.5f);

        if (imgIcon != null) {
            imgIcon.setImageResource(isSuccess ? R.drawable.ic_check : R.drawable.ic_error);
            imgIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(this, isSuccess ? R.color.white : R.color.error_red));
        }

        if (tvTitle != null) {
            tvTitle.setText(title != null ? title : (isSuccess ? "Attendance Marked!" : "Verification Failed"));
        }

        if (tvSubtitle != null) {
            tvSubtitle.setText(message != null ? message : (isSuccess ?
                    "Your attendance was verified and recorded in Firestore." :
                    "Location or QR verification failed. Please try again within classroom radius."));
        }

        if (tvStatusBadge != null) {
            tvStatusBadge.setText(isSuccess ? "VERIFIED & LOGGED" : "FAILED / OUT OF RANGE");
            tvStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, isSuccess ? R.color.success_green : R.color.error_red));
            tvStatusBadge.setBackgroundResource(isSuccess ? R.drawable.bg_badge_present : R.drawable.bg_badge_absent);
        }

        if (tvStudent != null) {
            tvStudent.setText(studentName != null ? studentName : "Student Account");
        }

        if (tvSubjectInfo != null) {
            tvSubjectInfo.setText(subjectInfo != null ? subjectInfo : "Data Structures & Algorithms (CS301)");
        }

        if (tvDistance != null) {
            tvDistance.setText(String.format(Locale.US, "%.1fm away (%s)", distance, isSuccess ? "Inside 25m Room" : "Outside Allowed Radius"));
            tvDistance.setTextColor(androidx.core.content.ContextCompat.getColor(this, isSuccess ? R.color.success_green : R.color.error_red));
        }

        if (tvTimestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            String timeStr = sdf.format(new Date());
            tvTimestamp.setText("Just now (" + timeStr + ")");
        }

        if (btnDone != null) {
            btnDone.setOnClickListener(v -> finish());
        }

        if (btnViewRoster != null) {
            btnViewRoster.setOnClickListener(v -> {
                Intent intent = new Intent(AttendanceResultActivity.this, ReportsActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
