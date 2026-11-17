package com.example.rollcall;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.data.AcademicRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class ManageCoursesActivity extends AppCompatActivity {

    private AcademicRepository academicRepo;
    private AuthRepository authRepo;

    private CourseAdapter adapter;
    private List<Course> courseList = new ArrayList<>();

    private EditText etSearchCourses;
    private View layoutEmptyCourses, fabAdd;
    private RecyclerView rvCourses;

    private User currentUser;
    private boolean isStaff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        academicRepo = AcademicRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        currentUser = authRepo.getCurrentUser();
        isStaff = currentUser == null || !User.ROLE_STUDENT.equalsIgnoreCase(currentUser.getRole());

        ImageButton btnBack = findViewById(R.id.btnBackManageCourses);
        fabAdd = findViewById(R.id.fabAddCourse);
        etSearchCourses = findViewById(R.id.etSearchCourses);
        layoutEmptyCourses = findViewById(R.id.layoutEmptyCourses);
        rvCourses = findViewById(R.id.rvManageCourses);

        TextView tvSubtitle = findViewById(R.id.tvCoursesSubtitle);

        if (!isStaff) {
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
            if (tvSubtitle != null) tvSubtitle.setText("Active Degree Programs & Enrolled Curriculum");
        } else {
            if (fabAdd != null) {
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(v -> showAddEditCourseDialog(null));
            }
        }

        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupDurationFilterChips();

        if (etSearchCourses != null) {
            etSearchCourses.addTextChangedListener(new TextWatcher() {
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

        loadCourses();
    }

    private void setupDurationFilterChips() {
        View btnAll = findViewById(R.id.btnCourseFilterAll);
        View btn4Yr = findViewById(R.id.btnCourseFilter4Yr);
        View btn3Yr = findViewById(R.id.btnCourseFilter3Yr);
        View btn2Yr = findViewById(R.id.btnCourseFilter2Yr);

        View.OnClickListener listener = v -> {
            int durYears = 0;
            if (v.getId() == R.id.btnCourseFilter4Yr) durYears = 4;
            else if (v.getId() == R.id.btnCourseFilter3Yr) durYears = 3;
            else if (v.getId() == R.id.btnCourseFilter2Yr) durYears = 2;

            if (adapter != null) {
                int visibleCount = adapter.filterDuration(durYears);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btn4Yr != null) btn4Yr.setOnClickListener(listener);
        if (btn3Yr != null) btn3Yr.setOnClickListener(listener);
        if (btn2Yr != null) btn2Yr.setOnClickListener(listener);
    }

    private void loadCourses() {
        academicRepo.fetchCourses(new AcademicRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                courseList = items;
                setupAdapter();
            }

            @Override
            public void onError(String message) {
                courseList.clear();
                courseList.add(new Course("crs_1", "B.Tech CSE", "CSE", 4, "Bachelor of Technology in CSE"));
                courseList.add(new Course("crs_2", "BCA", "BCA", 3, "Bachelor of Computer Applications"));
                courseList.add(new Course("crs_3", "B.Com", "BCOM", 3, "Bachelor of Commerce"));
                courseList.add(new Course("crs_4", "MCA", "MCA", 2, "Master of Computer Applications"));
                setupAdapter();
            }
        });
    }

    private void setupAdapter() {
        adapter = new CourseAdapter(courseList, isStaff, this::handleCourseClick);
        rvCourses.setAdapter(adapter);
        updateEmptyState(courseList.size());
    }

    private void handleCourseClick(Course course) {
        if (isStaff) {
            String[] options = {"View Enrolled Students", "Edit Course Details", "Delete Course", "Cancel"};
            new AlertDialog.Builder(this)
                    .setTitle(course.getCourseName() + " (" + course.getCourseCode() + ")")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            openCourseStudents(course);
                        } else if (which == 1) {
                            showAddEditCourseDialog(course);
                        } else if (which == 2) {
                            confirmDeleteCourse(course);
                        }
                    })
                    .show();
        } else {
            openCourseStudents(course);
        }
    }

    private void openCourseStudents(Course course) {
        Intent intent = new Intent(this, ManageStudentsActivity.class);
        intent.putExtra(ManageStudentsActivity.EXTRA_COURSE_FILTER, course.getCourseCode() != null ? course.getCourseCode() : course.getCourseName());
        startActivity(intent);
    }

    private void confirmDeleteCourse(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course Program?")
                .setMessage("Are you sure you want to delete " + course.getCourseName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    courseList.remove(course);
                    if (adapter != null) adapter.filterSearch(etSearchCourses != null ? etSearchCourses.getText().toString() : "");
                    Toast.makeText(ManageCoursesActivity.this, "Course deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyCourses != null && rvCourses != null) {
            if (visibleCount == 0) {
                layoutEmptyCourses.setVisibility(View.VISIBLE);
                rvCourses.setVisibility(View.GONE);
            } else {
                layoutEmptyCourses.setVisibility(View.GONE);
                rvCourses.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showAddEditCourseDialog(Course existingCourse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDlgCourseTitle);
        EditText etCode = dialogView.findViewById(R.id.etDlgCourseCode);
        EditText etName = dialogView.findViewById(R.id.etDlgCourseName);
        EditText etDuration = dialogView.findViewById(R.id.etDlgCourseDuration);
        EditText etDesc = dialogView.findViewById(R.id.etDlgCourseDesc);
        Button btnCancel = dialogView.findViewById(R.id.btnDlgCancel);
        Button btnSave = dialogView.findViewById(R.id.btnDlgSave);

        if (existingCourse != null) {
            if (tvTitle != null) tvTitle.setText("Edit Degree Course");
            if (etCode != null) etCode.setText(existingCourse.getCourseCode());
            if (etName != null) etName.setText(existingCourse.getCourseName());
            if (etDuration != null) etDuration.setText(String.valueOf(existingCourse.getDurationYears()));
            if (etDesc != null) etDesc.setText(existingCourse.getDescription());
            if (btnSave != null) btnSave.setText("Save Changes");
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String code = etCode != null ? etCode.getText().toString().trim() : "";
                String name = etName != null ? etName.getText().toString().trim() : "";
                String dur = etDuration != null ? etDuration.getText().toString().trim() : "";
                String desc = etDesc != null ? etDesc.getText().toString().trim() : "College Program";

                if (code.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Course code and name required", Toast.LENGTH_SHORT).show();
                    return;
                }

                int years = dur.isEmpty() ? 3 : Integer.parseInt(dur);

                if (existingCourse != null) {
                    existingCourse.setCourseCode(code);
                    existingCourse.setCourseName(name);
                    existingCourse.setDurationYears(years);
                    existingCourse.setDescription(desc);
                    Toast.makeText(this, "Course details updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Course course = new Course("crs_" + System.currentTimeMillis(), name, code, years, desc);
                    academicRepo.addCourse(course, new AcademicRepository.ActionCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onError(String message) {}
                    });
                    courseList.add(0, course);
                    Toast.makeText(this, "Course added successfully!", Toast.LENGTH_SHORT).show();
                }

                if (adapter != null) adapter.filterSearch(etSearchCourses != null ? etSearchCourses.getText().toString() : "");
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
        private final List<Course> originalList;
        private final List<Course> displayedList;
        private final boolean isStaff;
        private final OnCourseClickListener clickListener;
        private int currentDurationFilter = 0;
        private String currentQuery = "";

        public CourseAdapter(List<Course> courses, boolean isStaff, OnCourseClickListener listener) {
            this.originalList = courses;
            this.displayedList = new ArrayList<>(courses);
            this.isStaff = isStaff;
            this.clickListener = listener;
        }

        public int filterDuration(int durationYears) {
            this.currentDurationFilter = durationYears;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (Course c : originalList) {
                boolean matchesDuration = currentDurationFilter == 0 || c.getDurationYears() == currentDurationFilter;
                if (!matchesDuration) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (c.getCourseName() != null && c.getCourseName().toLowerCase().contains(currentQuery)) ||
                        (c.getCourseCode() != null && c.getCourseCode().toLowerCase().contains(currentQuery)) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(c);
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
            Course c = displayedList.get(position);
            holder.tvName.setText(c.getCourseName());

            String code = c.getCourseCode() != null ? c.getCourseCode() : "CS";
            String actionText = isStaff ? " • Tap to Manage ›" : " • Tap for Roster ›";
            holder.tvRollEmail.setText(code + " • " + c.getDurationYears() + " Years Program (" + (c.getDescription() != null ? c.getDescription() : "UG/PG") + ")" + actionText);

            holder.tvAvatar.setText(code.length() >= 2 ? code.substring(0, 2).toUpperCase() : code.toUpperCase());

            holder.tvBadge.setText("ACTIVE");
            holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onCourseClick(c);
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
