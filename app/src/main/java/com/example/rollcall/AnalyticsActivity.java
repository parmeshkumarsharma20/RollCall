package com.example.rollcall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.Subject;
import com.example.rollcall.utils.ReportExporter;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT = "EXTRA_SUBJECT";

    private AttendanceRepository attendanceRepo;
    private Subject currentSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        android.view.View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        attendanceRepo = AttendanceRepository.getInstance(this);

        if (getIntent().hasExtra(EXTRA_SUBJECT)) {
            currentSubject = (Subject) getIntent().getSerializableExtra(EXTRA_SUBJECT);
        }

        ImageButton btnBack = findViewById(R.id.btnBackAnalytics);
        TextView tvHeaderTitle = findViewById(R.id.tvAnalyticsHeaderTitle);
        TextView tvSubtitle = findViewById(R.id.tvAnalyticsSubtitle);
        TextView tvCardTitle = findViewById(R.id.tvAnalyticsCardTitle);
        TextView tvPercent = findViewById(R.id.tvAnalyticsOverallPercent);
        TextView tvSummary = findViewById(R.id.tvAnalyticsSummaryText);
        ProgressBar pbOverall = findViewById(R.id.pbAnalyticsOverall);

        TextView tvPresent = findViewById(R.id.tvAnalyticsPresentCount);
        TextView tvAbsent = findViewById(R.id.tvAnalyticsAbsentCount);
        TextView tvTotal = findViewById(R.id.tvAnalyticsTotalStudents);
        TextView tvBadge = findViewById(R.id.tvSubjectBreakdownBadge);

        TextView tvBreakdownTitle = findViewById(R.id.tvSubjectBreakdownTitle);
        TextView tvBreakdownSub = findViewById(R.id.tvSubjectBreakdownSub);

        Button btnExportPDF = findViewById(R.id.btnExportPDF);
        Button btnExportCSV = findViewById(R.id.btnExportCSV);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        List<AttendanceRecord> allRecords = attendanceRepo.getAttendanceRecords();
        List<AttendanceRecord> targetRecords = new ArrayList<>();

        int presentCount = attendanceRepo.getPresentCount();
        int totalStudents = attendanceRepo.getTotalStudentCount();
        int absentCount = Math.max(0, totalStudents - presentCount);

        if (tvPresent != null) tvPresent.setText(String.valueOf(presentCount));
        if (tvAbsent != null) tvAbsent.setText(String.valueOf(absentCount));
        if (tvTotal != null) tvTotal.setText(String.valueOf(totalStudents));

        float percent = totalStudents > 0 ? ((float) presentCount / totalStudents) * 100f : 100f;

        if (tvBadge != null) {
            if (percent >= 75f) {
                tvBadge.setText("SATISFACTORY");
                tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.success_green));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            } else {
                tvBadge.setText("WARNING < 75%");
                tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.error_red));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            }
        }

        if (currentSubject != null) {
            String subjCode = currentSubject.getSubjectCode() != null ? currentSubject.getSubjectCode() : "";
            String subjName = currentSubject.getSubjectName() != null ? currentSubject.getSubjectName() : "";
            String courseName = currentSubject.getCourse() != null ? currentSubject.getCourse() : "B.Tech CSE";

            if (tvHeaderTitle != null) tvHeaderTitle.setText(subjName);
            if (tvSubtitle != null) tvSubtitle.setText("Subject Code: " + subjCode + " • " + courseName);
            if (tvCardTitle != null) tvCardTitle.setText("Subject Attendance Analysis (" + subjCode + ")");

            for (AttendanceRecord r : allRecords) {
                targetRecords.add(r);
            }

            if (tvPercent != null) tvPercent.setText(String.format(Locale.US, "%.1f%%", percent));
            if (pbOverall != null) pbOverall.setProgress((int) percent);
            if (tvSummary != null) tvSummary.setText(presentCount + " / " + totalStudents + " Enrolled Students Present");

            if (tvBreakdownTitle != null) tvBreakdownTitle.setText(subjName + " Roster");
            if (tvBreakdownSub != null) tvBreakdownSub.setText("Subject-specific attendance log and performance record.");

        } else {
            targetRecords.addAll(allRecords);

            if (tvPercent != null) tvPercent.setText(String.format(Locale.US, "%.1f%%", percent));
            if (pbOverall != null) pbOverall.setProgress((int) percent);
            if (tvSummary != null) tvSummary.setText(presentCount + " / " + totalStudents + " System Students Active");
        }

        final String reportTitle = currentSubject != null ?
                currentSubject.getSubjectCode() + " - " + currentSubject.getSubjectName() + " Attendance Report" :
                "System Attendance Analytics Overview";

        if (btnExportPDF != null) {
            btnExportPDF.setOnClickListener(v -> ReportExporter.exportToPDF(AnalyticsActivity.this, reportTitle, targetRecords));
        }

        if (btnExportCSV != null) {
            btnExportCSV.setOnClickListener(v -> ReportExporter.exportToCSV(AnalyticsActivity.this, reportTitle, targetRecords));
        }
    }
}
