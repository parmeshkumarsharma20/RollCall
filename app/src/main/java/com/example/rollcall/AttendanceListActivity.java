package com.example.rollcall;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.ReportExporter;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class AttendanceListActivity extends AppCompatActivity {

    private TextView tvRosterTitle, tvRosterPresent, tvRosterAbsent;
    private Spinner spnRosterCourse;
    private EditText etRosterSearch;
    private Button btnFilterAll, btnFilterPresent, btnFilterAbsent, btnExportPDF;
    private View layoutEmptyRoster;
    private RecyclerView rvAttendanceRoster;

    private AttendanceRepository repository;
    private AdminRepository adminRepo;
    private RosterAdapter rosterAdapter;
    private List<Course> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        repository = AttendanceRepository.getInstance(this);
        adminRepo = AdminRepository.getInstance(this);

        tvRosterTitle = findViewById(R.id.tvRosterTitle);
        tvRosterPresent = findViewById(R.id.tvRosterPresent);
        tvRosterAbsent = findViewById(R.id.tvRosterAbsent);
        spnRosterCourse = findViewById(R.id.spnRosterCourse);
        etRosterSearch = findViewById(R.id.etRosterSearch);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPresent = findViewById(R.id.btnFilterPresent);
        btnFilterAbsent = findViewById(R.id.btnFilterAbsent);
        btnExportPDF = findViewById(R.id.btnExportRosterPDF);
        Button btnExportCSV = findViewById(R.id.btnExportRosterCSV);
        layoutEmptyRoster = findViewById(R.id.layoutEmptyRoster);
        ImageButton btnBackRoster = findViewById(R.id.btnBackRoster);
        rvAttendanceRoster = findViewById(R.id.rvAttendanceRoster);

        rvAttendanceRoster.setLayoutManager(new LinearLayoutManager(this));

        if (btnBackRoster != null) btnBackRoster.setOnClickListener(v -> finish());

        if (btnExportPDF != null) {
            btnExportPDF.setOnClickListener(v -> {
                List<AttendanceRecord> records = repository.getAttendanceRecords();
                ReportExporter.exportToPDF(AttendanceListActivity.this, "Live Class Attendance Roster", records);
            });
        }

        if (btnExportCSV != null) {
            btnExportCSV.setOnClickListener(v -> {
                List<AttendanceRecord> records = repository.getAttendanceRecords();
                ReportExporter.exportToCSV(AttendanceListActivity.this, "Live Class Attendance Roster", records);
            });
        }

        loadCourseFilterSpinner();
        updateStats();
        loadRoster();

        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> {
                if (rosterAdapter != null) {
                    int visibleCount = rosterAdapter.setFilterStatus("ALL");
                    updateEmptyState(visibleCount);
                }
            });
        }

        if (btnFilterPresent != null) {
            btnFilterPresent.setOnClickListener(v -> {
                if (rosterAdapter != null) {
                    int visibleCount = rosterAdapter.setFilterStatus("PRESENT");
                    updateEmptyState(visibleCount);
                }
            });
        }

        if (btnFilterAbsent != null) {
            btnFilterAbsent.setOnClickListener(v -> {
                if (rosterAdapter != null) {
                    int visibleCount = rosterAdapter.setFilterStatus("ABSENT");
                    updateEmptyState(visibleCount);
                }
            });
        }

        if (etRosterSearch != null) {
            etRosterSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (rosterAdapter != null) {
                        int visibleCount = rosterAdapter.filterSearch(s.toString());
                        updateEmptyState(visibleCount);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadCourseFilterSpinner() {
        adminRepo.fetchCourses(new AdminRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                courseList = new ArrayList<>();
                courseList.add(new Course("all", "All Courses / Programs", "ALL", 4, "All enrolled courses"));
                courseList.addAll(items);
                setupCourseSpinner();
            }

            @Override
            public void onError(String message) {
                courseList.clear();
                courseList.add(new Course("all", "All Courses / Programs", "ALL", 4, "All enrolled courses"));
                courseList.add(new Course("c_1", "B.Tech CSE", "CSE", 4, "Bachelor of Technology"));
                courseList.add(new Course("c_2", "BCA", "BCA", 3, "Bachelor of Computer Applications"));
                setupCourseSpinner();
            }
        });
    }

    private void setupCourseSpinner() {
        if (spnRosterCourse == null) return;
        ArrayAdapter<Course> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRosterCourse.setAdapter(adapter);

        spnRosterCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Course sel = courseList.get(position);
                String courseName = "ALL".equalsIgnoreCase(sel.getCourseCode()) ? "ALL" : sel.getCourseName();
                if (rosterAdapter != null) {
                    int visibleCount = rosterAdapter.filterCourse(courseName);
                    updateEmptyState(visibleCount);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyRoster != null && rvAttendanceRoster != null) {
            if (visibleCount == 0) {
                layoutEmptyRoster.setVisibility(View.VISIBLE);
                rvAttendanceRoster.setVisibility(View.GONE);
            } else {
                layoutEmptyRoster.setVisibility(View.GONE);
                rvAttendanceRoster.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateStats() {
        loadRoster();
    }

    private void loadRoster() {
        adminRepo.fetchStudents(new AdminRepository.ListCallback<User>() {
            @Override
            public void onSuccess(List<User> items) {
                List<User> allStudents = new ArrayList<>(repository.getStudents());
                if (items != null) {
                    for (User u : items) {
                        boolean exists = false;
                        for (User existing : allStudents) {
                            if (existing.getId() != null && existing.getId().equals(u.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) allStudents.add(u);
                    }
                }
                displayRosterWithList(allStudents);
            }

            @Override
            public void onError(String message) {
                displayRosterWithList(repository.getStudents());
            }
        });
    }

    private void displayRosterWithList(List<User> students) {
        List<AttendanceRecord> records = repository.getAttendanceRecords();
        rosterAdapter = new RosterAdapter(students, records, (student, currentIsPresent) -> showOverrideDialog(student, currentIsPresent));
        rvAttendanceRoster.setAdapter(rosterAdapter);

        String preFilterCourse = getIntent().getStringExtra("extra_course_filter");
        if (preFilterCourse != null && !preFilterCourse.isEmpty() && rosterAdapter != null) {
            int count = rosterAdapter.filterCourse(preFilterCourse);
            updateEmptyState(count);
        } else {
            updateEmptyState(students.size());
        }

        updateStatsForList(students, records);
    }

    private void updateStatsForList(List<User> students, List<AttendanceRecord> records) {
        int presentCount = 0;
        int total = students.size();

        for (User u : students) {
            for (AttendanceRecord r : records) {
                if (r != null && r.getStudentId() != null && u.getId() != null && r.getStudentId().equalsIgnoreCase(u.getId()) && "PRESENT".equalsIgnoreCase(r.getStatus())) {
                    presentCount++;
                    break;
                }
            }
        }

        int absentCount = Math.max(0, total - presentCount);

        if (tvRosterTitle != null) {
            tvRosterTitle.setText("Attendance Roster (" + total + " Enrolled)");
        }

        if (tvRosterPresent != null) tvRosterPresent.setText(String.valueOf(presentCount));
        if (tvRosterAbsent != null) tvRosterAbsent.setText(String.valueOf(absentCount));

        if (btnFilterAll != null) btnFilterAll.setText("All (" + total + ")");
        if (btnFilterPresent != null) btnFilterPresent.setText("Present (" + presentCount + ")");
        if (btnFilterAbsent != null) btnFilterAbsent.setText("Absent (" + absentCount + ")");
    }

    private void showOverrideDialog(User student, boolean currentIsPresent) {
        String actionStr = currentIsPresent ? "Mark ABSENT" : "Mark PRESENT";
        new AlertDialog.Builder(this)
                .setTitle("Quick Attendance Override")
                .setMessage("Student: " + student.getName() + " (" + student.getRollNumber() + ")\n\nDo you want to toggle attendance status for this student?")
                .setPositiveButton(actionStr, (dialog, which) -> {
                    repository.markAttendance(student, 5.0f);
                    updateStats();
                    loadRoster();
                    Toast.makeText(AttendanceListActivity.this, "Updated attendance for " + student.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public interface OnStudentClickListener {
        void onStudentClick(User student, boolean isPresent);
    }

    private static class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.ViewHolder> {
        private final List<User> allStudents;
        private final List<User> displayedList;
        private final List<AttendanceRecord> records;
        private final OnStudentClickListener clickListener;
        private String currentCourseFilter = "ALL";
        private String currentStatusFilter = "ALL";
        private String currentQuery = "";

        public RosterAdapter(List<User> students, List<AttendanceRecord> records, OnStudentClickListener listener) {
            this.allStudents = students;
            this.displayedList = new ArrayList<>(students);
            this.records = records;
            this.clickListener = listener;
        }

        public int filterCourse(String courseName) {
            this.currentCourseFilter = courseName;
            return applyFilters();
        }

        public int setFilterStatus(String status) {
            this.currentStatusFilter = status;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (User u : allStudents) {
                boolean matchesCourse = "ALL".equalsIgnoreCase(currentCourseFilter) ||
                        (u.getCourse() != null && u.getCourse().equalsIgnoreCase(currentCourseFilter));

                if (!matchesCourse) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        u.getName().toLowerCase().contains(currentQuery) ||
                        u.getRollNumber().toLowerCase().contains(currentQuery) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(currentQuery));

                if (!matchesQuery) continue;

                boolean isPresent = false;
                for (AttendanceRecord r : records) {
                    if (r.getStudentId().equals(u.getId()) && "PRESENT".equalsIgnoreCase(r.getStatus())) {
                        isPresent = true;
                        break;
                    }
                }

                if ("ALL".equals(currentStatusFilter)) {
                    displayedList.add(u);
                } else if ("PRESENT".equals(currentStatusFilter) && isPresent) {
                    displayedList.add(u);
                } else if ("ABSENT".equals(currentStatusFilter) && !isPresent) {
                    displayedList.add(u);
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
            User u = displayedList.get(position);
            holder.tvName.setText(u.getName());

            AttendanceRecord record = null;
            for (AttendanceRecord r : records) {
                if (r.getStudentId().equals(u.getId()) && "PRESENT".equalsIgnoreCase(r.getStatus())) {
                    record = r;
                    break;
                }
            }

            final boolean isPresent = (record != null);

            if (isPresent) {
                holder.tvRollEmail.setText(u.getRollNumber() + " • Present (" + (int) record.getDistanceMeters() + "m GPS Verified)");
                holder.tvBadge.setText("PRESENT");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.status_present));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            } else {
                holder.tvRollEmail.setText(u.getRollNumber() + " • " + (u.getEmail() != null ? u.getEmail() : "N/A"));
                holder.tvBadge.setText("ABSENT");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.status_absent));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            }

            String initials = u.getRollNumber() != null && u.getRollNumber().length() >= 4 ? u.getRollNumber().substring(0, 4) : "RC";
            holder.tvAvatar.setText(initials);

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onStudentClick(u, isPresent);
                }
            });
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
