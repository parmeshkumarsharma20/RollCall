package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AcademicRepository;
import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.Room;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminAvatar, tvAdminGreeting;
    private TextView tvCountStudents, tvCountTeachers, tvCountCourses, tvCountAttendance;
    private TextView tvBadgeStudents, tvBadgeTeachers, tvBadgeCourses, tvBadgeSubjects, tvBadgeRooms;

    private AdminRepository adminRepo;
    private AcademicRepository academicRepo;
    private AttendanceRepository attendanceRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        View rootView = findViewById(android.R.id.content);
        View bottomNav = findViewById(R.id.layoutAdminBottomNav);
        WindowInsetUtils.applySystemBarInsets(rootView, null, bottomNav);

        adminRepo = AdminRepository.getInstance(this);
        academicRepo = AcademicRepository.getInstance(this);
        attendanceRepo = AttendanceRepository.getInstance(this);

        initViews();
        setupUserHeader();
        setupClickListeners();
        loadDashboardStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void initViews() {
        tvAdminAvatar = findViewById(R.id.tvAdminAvatar);
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);

        tvCountStudents = findViewById(R.id.tvCountStudents);
        tvCountTeachers = findViewById(R.id.tvCountTeachers);
        tvCountCourses = findViewById(R.id.tvCountCourses);
        tvCountAttendance = findViewById(R.id.tvCountAttendance);

        tvBadgeStudents = findViewById(R.id.tvBadgeStudents);
        tvBadgeTeachers = findViewById(R.id.tvBadgeTeachers);
        tvBadgeCourses = findViewById(R.id.tvBadgeCourses);
        tvBadgeSubjects = findViewById(R.id.tvBadgeSubjects);
        tvBadgeRooms = findViewById(R.id.tvBadgeRooms);
    }

    private void setupUserHeader() {
        AuthRepository authRepo = AuthRepository.getInstance(this);
        User currentUser = authRepo.getCurrentUser();

        if (currentUser != null) {
            if (tvAdminGreeting != null) {
                tvAdminGreeting.setText("Welcome, " + currentUser.getName());
            }
            String name = currentUser.getName();
            if (name != null && name.length() >= 2 && tvAdminAvatar != null) {
                tvAdminAvatar.setText(name.substring(0, 2).toUpperCase());
            }
        }
    }

    private void loadDashboardStats() {
        // 1. Fetch Students count
        adminRepo.fetchStudents(new AdminRepository.ListCallback<User>() {
            @Override
            public void onSuccess(List<User> items) {
                int count = items.size();
                if (count == 0) count = attendanceRepo.getTotalStudentCount();
                updateStudentsCount(count);
            }

            @Override
            public void onError(String message) {
                int count = attendanceRepo.getTotalStudentCount();
                updateStudentsCount(count);
            }
        });

        // 2. Fetch Teachers count
        adminRepo.fetchTeachers(new AdminRepository.ListCallback<User>() {
            @Override
            public void onSuccess(List<User> items) {
                int count = items.size();
                if (count == 0) count = attendanceRepo.getTeachers().size();
                updateTeachersCount(count);
            }

            @Override
            public void onError(String message) {
                int count = attendanceRepo.getTeachers().size();
                updateTeachersCount(count);
            }
        });

        // 3. Fetch Courses count
        academicRepo.fetchCourses(new AcademicRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                int count = items.size();
                updateCoursesCount(count);
            }

            @Override
            public void onError(String message) {
                updateCoursesCount(5); // Default fallback
            }
        });

        // 4. Fetch Subjects count
        adminRepo.fetchSubjects(new AdminRepository.ListCallback<Subject>() {
            @Override
            public void onSuccess(List<Subject> items) {
                int count = items.size();
                if (tvBadgeSubjects != null) {
                    tvBadgeSubjects.setText(count + " Subjects");
                }
            }

            @Override
            public void onError(String message) {
                if (tvBadgeSubjects != null) {
                    tvBadgeSubjects.setText("3 Subjects");
                }
            }
        });

        // 5. Fetch Rooms count
        adminRepo.fetchRooms(new AdminRepository.ListCallback<Room>() {
            @Override
            public void onSuccess(List<Room> items) {
                int count = items.size();
                if (tvBadgeRooms != null) {
                    tvBadgeRooms.setText(count + " Rooms");
                }
            }

            @Override
            public void onError(String message) {
                if (tvBadgeRooms != null) {
                    tvBadgeRooms.setText("1 Room");
                }
            }
        });

        // 6. Fetch Live Attendance Present count
        int presentCount = attendanceRepo.getPresentCount();
        if (tvCountAttendance != null) {
            tvCountAttendance.setText(String.valueOf(presentCount));
        }
    }

    private void updateStudentsCount(int count) {
        if (tvCountStudents != null) tvCountStudents.setText(String.valueOf(count));
        if (tvBadgeStudents != null) tvBadgeStudents.setText(count + " Students");
    }

    private void updateTeachersCount(int count) {
        if (tvCountTeachers != null) tvCountTeachers.setText(String.valueOf(count));
        if (tvBadgeTeachers != null) tvBadgeTeachers.setText(count + " Teachers");
    }

    private void updateCoursesCount(int count) {
        if (tvCountCourses != null) tvCountCourses.setText(String.valueOf(count));
        if (tvBadgeCourses != null) tvBadgeCourses.setText(count + " Courses");
    }

    private void showCourseSelectionDialog() {
        List<String> options = new ArrayList<>();
        options.add("All Courses");
        options.add("B.Tech CSE");
        options.add("BCA");
        options.add("B.Com");
        options.add("BBA");
        options.add("MCA");
        options.add("M.Com");

        academicRepo.fetchCourses(new AcademicRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                if (items != null) {
                    for (Course c : items) {
                        String name = c.getCourseCode() != null ? c.getCourseCode() : c.getCourseName();
                        if (name != null && !options.contains(name)) {
                            options.add(name);
                        }
                    }
                }
                displayCourseDialog(options);
            }

            @Override
            public void onError(String message) {
                displayCourseDialog(options);
            }
        });
    }

    private void displayCourseDialog(List<String> options) {
        String[] itemsArray = options.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Select Course to View Students")
                .setItems(itemsArray, (dialog, which) -> {
                    String selectedCourse = itemsArray[which];
                    Intent intent = new Intent(AdminDashboardActivity.this, ManageStudentsActivity.class);
                    intent.putExtra(ManageStudentsActivity.EXTRA_COURSE_FILTER, selectedCourse);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupClickListeners() {
        ImageButton btnNotification = findViewById(R.id.btnAdminNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        }

        // Top Stat Cards Click Listeners
        View cardStatStudents = findViewById(R.id.cardStatStudents);
        if (cardStatStudents != null) {
            cardStatStudents.setOnClickListener(v -> showCourseSelectionDialog());
        }

        View.OnClickListener openTeachersRoster = v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminActivity.class);
            intent.putExtra(AdminActivity.EXTRA_SHOW_TEACHERS, true);
            startActivity(intent);
        };

        View cardStatTeachers = findViewById(R.id.cardStatTeachers);
        if (cardStatTeachers != null) {
            cardStatTeachers.setOnClickListener(openTeachersRoster);
        }

        View cardStatCourses = findViewById(R.id.cardStatCourses);
        if (cardStatCourses != null) {
            cardStatCourses.setOnClickListener(v -> startActivity(new Intent(this, ManageCoursesActivity.class)));
        }

        View cardStatAttendance = findViewById(R.id.cardStatAttendance);
        if (cardStatAttendance != null) {
            cardStatAttendance.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        }

        // Section 1: Academic & User Management Cards
        View cardManageUsers = findViewById(R.id.cardManageUsers);
        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> showCourseSelectionDialog());
        }

        View cardManageTeachers = findViewById(R.id.cardManageTeachers);
        if (cardManageTeachers != null) {
            cardManageTeachers.setOnClickListener(openTeachersRoster);
        }

        View cardManageClasses = findViewById(R.id.cardManageClasses);
        if (cardManageClasses != null) {
            cardManageClasses.setOnClickListener(v -> startActivity(new Intent(this, ManageCoursesActivity.class)));
        }

        View cardManageSubjects = findViewById(R.id.cardManageSubjects);
        if (cardManageSubjects != null) {
            cardManageSubjects.setOnClickListener(v -> startActivity(new Intent(this, ManageSubjectsActivity.class)));
        }

        View cardManageRooms = findViewById(R.id.cardManageRooms);
        if (cardManageRooms != null) {
            cardManageRooms.setOnClickListener(v -> startActivity(new Intent(this, ManageRoomsActivity.class)));
        }

        View cardManageTimetable = findViewById(R.id.cardManageTimetable);
        if (cardManageTimetable != null) {
            cardManageTimetable.setOnClickListener(v -> startActivity(new Intent(this, ManageTimetableActivity.class)));
        }

        // Section 2: Attendance & Reporting Tools Cards
        View cardAdminReports = findViewById(R.id.cardAdminReports);
        if (cardAdminReports != null) {
            cardAdminReports.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        }

        View cardAttendanceLogs = findViewById(R.id.cardAttendanceLogs);
        if (cardAttendanceLogs != null) {
            cardAttendanceLogs.setOnClickListener(v -> startActivity(new Intent(this, AttendanceListActivity.class)));
        }

        View cardGenerateQR = findViewById(R.id.cardGenerateQR);
        if (cardGenerateQR != null) {
            cardGenerateQR.setOnClickListener(v -> startActivity(new Intent(this, CreateSessionActivity.class)));
        }

        View cardExportReports = findViewById(R.id.cardExportReports);
        if (cardExportReports != null) {
            cardExportReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        }

        View cardAttendanceCorrection = findViewById(R.id.cardAttendanceCorrection);
        if (cardAttendanceCorrection != null) {
            cardAttendanceCorrection.setOnClickListener(v -> startActivity(new Intent(this, AttendanceCorrectionActivity.class)));
        }

        View cardBulkImport = findViewById(R.id.cardBulkImport);
        if (cardBulkImport != null) {
            cardBulkImport.setOnClickListener(v -> startActivity(new Intent(this, BulkImportStudentsActivity.class)));
        }

        // Section 3: System Control & Security Cards
        View cardAcademicSessions = findViewById(R.id.cardAcademicSessions);
        if (cardAcademicSessions != null) {
            cardAcademicSessions.setOnClickListener(v -> startActivity(new Intent(this, CreateSessionActivity.class)));
        }

        // Bottom Navigation Bar Listeners (5 Admin Core Tabs)
        View navDashboard = findViewById(R.id.navAdminDashboard);
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                loadDashboardStats();
                Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
            });
        }

        View navUsers = findViewById(R.id.navAdminUsers);
        if (navUsers != null) {
            navUsers.setOnClickListener(v -> showCourseSelectionDialog());
        }

        View navManagement = findViewById(R.id.navAdminManagement);
        if (navManagement != null) {
            navManagement.setOnClickListener(v -> startActivity(new Intent(this, ManageCoursesActivity.class)));
        }

        View navReportsTab = findViewById(R.id.navAdminReportsTab);
        if (navReportsTab != null) {
            navReportsTab.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        }

        View navProfile = findViewById(R.id.navAdminProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }
}
