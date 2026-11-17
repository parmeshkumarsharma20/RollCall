package com.example.rollcall;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.utils.ReportExporter;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {

    private AttendanceRepository attendanceRepo;
    private ReportAdapter adapter;
    private List<AttendanceRecord> recordList = new ArrayList<>();

    private EditText etSearchReports;
    private TextView tvReportsSubtitle;
    private View layoutEmptyReports;
    private RecyclerView rvRoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        attendanceRepo = AttendanceRepository.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBackReports);
        Button btnPDF = findViewById(R.id.btnReportPDF);
        Button btnCSV = findViewById(R.id.btnReportCSV);
        etSearchReports = findViewById(R.id.etSearchReports);
        tvReportsSubtitle = findViewById(R.id.tvReportsSubtitle);
        layoutEmptyReports = findViewById(R.id.layoutEmptyReports);
        rvRoster = findViewById(R.id.rvReportsRoster);

        rvRoster.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        recordList = attendanceRepo.getAttendanceRecords();
        int totalStudents = attendanceRepo.getTotalStudentCount();

        if (tvReportsSubtitle != null) {
            tvReportsSubtitle.setText("Total Records: " + recordList.size() + " • Enrolled: " + totalStudents + " Students");
        }

        adapter = new ReportAdapter(recordList);
        rvRoster.setAdapter(adapter);
        updateEmptyState(recordList.size());

        setupFilterChips();

        if (etSearchReports != null) {
            etSearchReports.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        int visibleCount = adapter.filterSearch(s.toString());
                        updateEmptyState(visibleCount);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnPDF != null) {
            btnPDF.setOnClickListener(v -> ReportExporter.exportToPDF(ReportsActivity.this, "Attendance Roster Report", recordList));
        }

        if (btnCSV != null) {
            btnCSV.setOnClickListener(v -> ReportExporter.exportToCSV(ReportsActivity.this, "Attendance Roster Report", recordList));
        }
    }

    private void setupFilterChips() {
        View btnAll = findViewById(R.id.btnReportFilterAll);
        View btnPresent = findViewById(R.id.btnReportFilterPresent);
        View btnAbsent = findViewById(R.id.btnReportFilterAbsent);

        View.OnClickListener listener = v -> {
            String status = "ALL";
            if (v.getId() == R.id.btnReportFilterPresent) status = "PRESENT";
            else if (v.getId() == R.id.btnReportFilterAbsent) status = "ABSENT";

            if (adapter != null) {
                int visibleCount = adapter.filterStatus(status);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btnPresent != null) btnPresent.setOnClickListener(listener);
        if (btnAbsent != null) btnAbsent.setOnClickListener(listener);
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyReports != null && rvRoster != null) {
            if (visibleCount == 0) {
                layoutEmptyReports.setVisibility(View.VISIBLE);
                rvRoster.setVisibility(View.GONE);
            } else {
                layoutEmptyReports.setVisibility(View.GONE);
                rvRoster.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
        private final List<AttendanceRecord> originalList;
        private final List<AttendanceRecord> displayedList;
        private String currentStatusFilter = "ALL";
        private String currentQuery = "";

        public ReportAdapter(List<AttendanceRecord> records) {
            this.originalList = records;
            this.displayedList = new ArrayList<>(records);
        }

        public int filterStatus(String status) {
            this.currentStatusFilter = status;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (AttendanceRecord r : originalList) {
                boolean isPresent = "PRESENT".equalsIgnoreCase(r.getStatus());
                boolean matchesStatus = "ALL".equalsIgnoreCase(currentStatusFilter) ||
                        ("PRESENT".equalsIgnoreCase(currentStatusFilter) && isPresent) ||
                        ("ABSENT".equalsIgnoreCase(currentStatusFilter) && !isPresent);

                if (!matchesStatus) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(currentQuery)) ||
                        (r.getRollNumber() != null && r.getRollNumber().toLowerCase().contains(currentQuery)) ||
                        (r.getStatus() != null && r.getStatus().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(r);
                }
            }
            notifyDataSetChanged();
            return displayedList.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AttendanceRecord r = displayedList.get(position);
            holder.tvName.setText(r.getStudentName() != null ? r.getStudentName() : "Student");

            String roll = r.getRollNumber() != null ? r.getRollNumber() : "RC2024001";
            holder.tvRollEmail.setText(roll + " • Distance: " + (int) r.getDistanceMeters() + "m GPS Verified");

            String initials = roll.length() >= 4 ? roll.substring(0, 4) : "RC";
            holder.tvAvatar.setText(initials);

            boolean isPresent = "PRESENT".equalsIgnoreCase(r.getStatus());
            holder.tvBadge.setText(isPresent ? "PRESENT" : "ABSENT");
            holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(isPresent ? R.color.status_present : R.color.status_absent));
            holder.tvBadge.setBackgroundResource(isPresent ? R.drawable.bg_badge_present : R.drawable.bg_badge_absent);
        }

        @Override
        public int getItemCount() {
            return displayedList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRollEmail, tvBadge, tvAvatar;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvItemName);
                tvRollEmail = itemView.findViewById(R.id.tvItemRollEmail);
                tvBadge = itemView.findViewById(R.id.tvItemStatusBadge);
                tvAvatar = itemView.findViewById(R.id.tvAvatarInitials);
            }
        }
    }
}
