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

import com.example.rollcall.data.AcademicRepository;
import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.Room;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.TimetableSlot;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class ManageTimetableActivity extends AppCompatActivity {

    private AcademicRepository academicRepo;
    private AdminRepository adminRepo;
    private AuthRepository authRepo;

    private TimetableAdapter adapter;
    private List<TimetableSlot> slotList = new ArrayList<>();

    private EditText etSearchTimetable;
    private View layoutEmptyTimetable;
    private RecyclerView rvTimetable;
    private View fabAdd;

    private User currentUser;
    private boolean isStaff = false;

    private List<Course> courseList = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private List<Room> roomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_timetable);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        academicRepo = AcademicRepository.getInstance(this);
        adminRepo = AdminRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        currentUser = authRepo.getCurrentUser();
        isStaff = currentUser == null || !User.ROLE_STUDENT.equalsIgnoreCase(currentUser.getRole());

        ImageButton btnBack = findViewById(R.id.btnBackTimetable);
        fabAdd = findViewById(R.id.fabAddTimetable);
        etSearchTimetable = findViewById(R.id.etSearchTimetable);
        layoutEmptyTimetable = findViewById(R.id.layoutEmptyTimetable);
        rvTimetable = findViewById(R.id.rvTimetableSlots);

        TextView tvSubtitle = findViewById(R.id.tvTimetableSubtitle);

        if (!isStaff) {
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
            if (tvSubtitle != null) tvSubtitle.setText("My Enrolled Course Schedule & Lecture Slots");
        } else {
            if (fabAdd != null) {
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(v -> showAddEditSlotDialog(null));
            }
        }

        rvTimetable.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupDayFilterButtons();

        if (etSearchTimetable != null) {
            etSearchTimetable.addTextChangedListener(new TextWatcher() {
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

        loadTimetable();
    }

    private void setupDayFilterButtons() {
        View btnAll = findViewById(R.id.btnDayAll);
        View btnMon = findViewById(R.id.btnDayMon);
        View btnTue = findViewById(R.id.btnDayTue);
        View btnWed = findViewById(R.id.btnDayWed);
        View btnThu = findViewById(R.id.btnDayThu);
        View btnFri = findViewById(R.id.btnDayFri);
        View btnSat = findViewById(R.id.btnDaySat);

        View.OnClickListener listener = v -> {
            String day = "ALL";
            if (v.getId() == R.id.btnDayMon) day = "Monday";
            else if (v.getId() == R.id.btnDayTue) day = "Tuesday";
            else if (v.getId() == R.id.btnDayWed) day = "Wednesday";
            else if (v.getId() == R.id.btnDayThu) day = "Thursday";
            else if (v.getId() == R.id.btnDayFri) day = "Friday";
            else if (v.getId() == R.id.btnDaySat) day = "Saturday";

            if (adapter != null) {
                int visibleCount = adapter.filterDay(day);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btnMon != null) btnMon.setOnClickListener(listener);
        if (btnTue != null) btnTue.setOnClickListener(listener);
        if (btnWed != null) btnWed.setOnClickListener(listener);
        if (btnThu != null) btnThu.setOnClickListener(listener);
        if (btnFri != null) btnFri.setOnClickListener(listener);
        if (btnSat != null) btnSat.setOnClickListener(listener);
    }

    private void loadTimetable() {
        String courseFilter = currentUser != null && currentUser.getCourse() != null ? currentUser.getCourse() : "crs_bca";

        academicRepo.fetchTimetable(courseFilter, new AcademicRepository.ListCallback<TimetableSlot>() {
            @Override
            public void onSuccess(List<TimetableSlot> items) {
                slotList = items;
                setupAdapter();
            }

            @Override
            public void onError(String message) {
                slotList.clear();
                slotList.add(new TimetableSlot("slot_1", "B.Tech CSE", "6th", "A", "CS301", "Data Structures & Algorithms", "teach_1", "Prof. Alan Turing", "room_301", "Room 301", "Monday", "09:00 AM", "10:00 AM"));
                slotList.add(new TimetableSlot("slot_2", "B.Tech CSE", "6th", "A", "CS302", "Database Management Systems", "teach_2", "Dr. Ada Lovelace", "room_301", "Room 301", "Monday", "10:00 AM", "11:00 AM"));
                slotList.add(new TimetableSlot("slot_3", "BCA", "4th", "A", "BCA101", "Programming in C", "teach_1", "Prof. Alan Turing", "room_102", "Room 102", "Tuesday", "11:00 AM", "12:00 PM"));
                slotList.add(new TimetableSlot("slot_4", "MCA", "1st", "A", "MCA107", "Database Management System", "teach_2", "Dr. Ada Lovelace", "room_301", "Room 301", "Wednesday", "02:00 PM", "03:00 PM"));
                setupAdapter();
            }
        });
    }

    private void setupAdapter() {
        adapter = new TimetableAdapter(slotList, isStaff, new ManageTimetableActivity.OnSlotClickListener() {
            @Override
            public void onSlotClick(TimetableSlot slot) {
                if (isStaff) {
                    showSlotOptionsDialog(slot);
                }
            }
        });
        rvTimetable.setAdapter(adapter);

        if (!isStaff && currentUser != null && currentUser.getCourse() != null) {
            int visibleCount = adapter.filterSearch(currentUser.getCourse());
            updateEmptyState(visibleCount);
        } else {
            updateEmptyState(slotList.size());
        }
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyTimetable != null && rvTimetable != null) {
            if (visibleCount == 0) {
                layoutEmptyTimetable.setVisibility(View.VISIBLE);
                rvTimetable.setVisibility(View.GONE);
            } else {
                layoutEmptyTimetable.setVisibility(View.GONE);
                rvTimetable.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showSlotOptionsDialog(TimetableSlot slot) {
        String[] options = {"Edit Slot", "Delete Slot", "Cancel"};
        new AlertDialog.Builder(this)
                .setTitle(slot.getSubjectName() + " (" + slot.getDayOfWeek() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddEditSlotDialog(slot);
                    } else if (which == 1) {
                        confirmDeleteSlot(slot);
                    }
                })
                .show();
    }

    private void confirmDeleteSlot(TimetableSlot slot) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Timetable Slot?")
                .setMessage("Are you sure you want to delete this lecture slot for " + slot.getSubjectName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    slotList.remove(slot);
                    if (adapter != null) adapter.filterSearch(etSearchTimetable != null ? etSearchTimetable.getText().toString() : "");
                    Toast.makeText(ManageTimetableActivity.this, "Slot deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAddEditSlotDialog(TimetableSlot existingSlot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_timetable, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDlgTimetableTitle);
        Spinner spnCourse = dialogView.findViewById(R.id.spnDlgSlotCourse);
        Spinner spnSubject = dialogView.findViewById(R.id.spnDlgSlotSubject);
        Spinner spnDay = dialogView.findViewById(R.id.spnDlgSlotDay);
        Spinner spnRoom = dialogView.findViewById(R.id.spnDlgSlotRoom);
        EditText etStart = dialogView.findViewById(R.id.etDlgStartTime);
        EditText etEnd = dialogView.findViewById(R.id.etDlgEndTime);
        EditText etTeacher = dialogView.findViewById(R.id.etDlgTeacherName);
        Button btnCancel = dialogView.findViewById(R.id.btnDlgCancelSlot);
        Button btnSave = dialogView.findViewById(R.id.btnDlgSaveSlot);

        if (existingSlot != null && tvTitle != null) {
            tvTitle.setText("Edit Timetable Slot");
            if (etStart != null) etStart.setText(existingSlot.getStartTime());
            if (etEnd != null) etEnd.setText(existingSlot.getEndTime());
            if (etTeacher != null) etTeacher.setText(existingSlot.getTeacherName());
        }

        // Setup Day Spinner
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spnDay != null) spnDay.setAdapter(dayAdapter);

        // Fetch Courses & Subjects & Rooms
        adminRepo.fetchCourses(new AdminRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                courseList = items;
                setupDialogCourseSpinner(spnCourse, spnSubject);
            }

            @Override
            public void onError(String message) {
                courseList.clear();
                courseList.add(new Course("c_1", "B.Tech CSE", "CSE", 4, "Bachelor of Technology in CSE"));
                courseList.add(new Course("c_2", "BCA", "BCA", 3, "Bachelor of Computer Applications"));
                courseList.add(new Course("c_3", "B.Com", "BCOM", 3, "Bachelor of Commerce"));
                courseList.add(new Course("c_4", "MCA", "MCA", 2, "Master of Computer Applications"));
                setupDialogCourseSpinner(spnCourse, spnSubject);
            }
        });

        adminRepo.fetchRooms(new AdminRepository.ListCallback<Room>() {
            @Override
            public void onSuccess(List<Room> items) {
                roomList = items;
                ArrayAdapter<Room> roomAdapter = new ArrayAdapter<>(ManageTimetableActivity.this, android.R.layout.simple_spinner_item, roomList);
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (spnRoom != null) spnRoom.setAdapter(roomAdapter);
            }

            @Override
            public void onError(String message) {
                roomList.clear();
                roomList.add(new Room("room_301", "Room 301", "CS Building", 28.6139, 77.2090, 25.0f));
                roomList.add(new Room("room_102", "Room 102", "IT Building", 28.6145, 77.2095, 25.0f));
                ArrayAdapter<Room> roomAdapter = new ArrayAdapter<>(ManageTimetableActivity.this, android.R.layout.simple_spinner_item, roomList);
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (spnRoom != null) spnRoom.setAdapter(roomAdapter);
            }
        });

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Course selCourse = (Course) spnCourse.getSelectedItem();
                Subject selSubject = (Subject) spnSubject.getSelectedItem();
                String selDay = spnDay.getSelectedItem() != null ? spnDay.getSelectedItem().toString() : "Monday";
                Room selRoom = (Room) spnRoom.getSelectedItem();
                String startTime = etStart.getText().toString().trim();
                String endTime = etEnd.getText().toString().trim();
                String teacherName = etTeacher.getText().toString().trim();

                if (startTime.isEmpty() || endTime.isEmpty()) {
                    Toast.makeText(this, "Start and end times are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String courseName = selCourse != null ? selCourse.getCourseName() : "B.Tech CSE";
                String subjectName = selSubject != null ? selSubject.getSubjectName() : "Data Structures";
                String roomNo = selRoom != null ? selRoom.getRoomNumber() : "Room 301";
                String teacher = teacherName.isEmpty() ? "Prof. Alan Turing" : teacherName;

                if (existingSlot != null) {
                    existingSlot.setCourseId(courseName);
                    existingSlot.setSubjectName(subjectName);
                    existingSlot.setDayOfWeek(selDay);
                    existingSlot.setRoomNumber(roomNo);
                    existingSlot.setStartTime(startTime);
                    existingSlot.setEndTime(endTime);
                    existingSlot.setTeacherName(teacher);
                    Toast.makeText(this, "Slot updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    TimetableSlot newSlot = new TimetableSlot(
                            "slot_" + System.currentTimeMillis(),
                            courseName,
                            "6th",
                            "A",
                            "subj_new",
                            subjectName,
                            "teach_1",
                            teacher,
                            "room_301",
                            roomNo,
                            selDay,
                            startTime,
                            endTime
                    );
                    slotList.add(0, newSlot);
                    Toast.makeText(this, "New timetable slot added!", Toast.LENGTH_SHORT).show();
                }

                if (adapter != null) adapter.filterSearch(etSearchTimetable != null ? etSearchTimetable.getText().toString() : "");
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void setupDialogCourseSpinner(Spinner spnCourse, Spinner spnSubject) {
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spnCourse != null) spnCourse.setAdapter(courseAdapter);

        adminRepo.fetchSubjects(new AdminRepository.ListCallback<Subject>() {
            @Override
            public void onSuccess(List<Subject> items) {
                allSubjects = items;
                filterDialogSubjects(spnCourse, spnSubject);
            }

            @Override
            public void onError(String message) {
                allSubjects.clear();
                allSubjects.add(new Subject("s_1", "CS301", "Data Structures & Algorithms", "B.Tech CSE", "6th", "A"));
                allSubjects.add(new Subject("s_2", "CS302", "Database Management Systems", "B.Tech CSE", "6th", "A"));
                allSubjects.add(new Subject("s_3", "BCA101", "Programming in C", "BCA", "4th", "A"));
                allSubjects.add(new Subject("s_4", "BC201", "Financial Accounting", "B.Com", "2nd", "B"));
                allSubjects.add(new Subject("s_5", "MCA107", "Database Management System", "MCA", "1st", "A"));
                filterDialogSubjects(spnCourse, spnSubject);
            }
        });

        if (spnCourse != null) {
            spnCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    filterDialogSubjects(spnCourse, spnSubject);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void filterDialogSubjects(Spinner spnCourse, Spinner spnSubject) {
        if (spnCourse == null || spnSubject == null) return;
        Course selectedCourse = (Course) spnCourse.getSelectedItem();
        String courseName = selectedCourse != null ? selectedCourse.getCourseName() : "B.Tech CSE";

        List<Subject> filtered = new ArrayList<>();
        for (Subject s : allSubjects) {
            if (s.getCourse() == null || s.getCourse().isEmpty() || s.getCourse().equalsIgnoreCase(courseName)) {
                filtered.add(s);
            }
        }

        if (filtered.isEmpty()) {
            filtered.add(new Subject("s_def", "CS301", "General Subject", courseName, "1st", "A"));
        }

        ArrayAdapter<Subject> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtered);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSubject.setAdapter(subjectAdapter);
    }

    public interface OnSlotClickListener {
        void onSlotClick(TimetableSlot slot);
    }

    private static class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {
        private final List<TimetableSlot> originalList;
        private final List<TimetableSlot> displayedList;
        private final boolean isStaff;
        private final ManageTimetableActivity.OnSlotClickListener listener;
        private String currentDay = "ALL";
        private String currentQuery = "";

        public TimetableAdapter(List<TimetableSlot> slots, boolean isStaff, ManageTimetableActivity.OnSlotClickListener listener) {
            this.originalList = slots;
            this.displayedList = new ArrayList<>(slots);
            this.isStaff = isStaff;
            this.listener = listener;
        }

        public int filterDay(String day) {
            this.currentDay = day;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (TimetableSlot slot : originalList) {
                boolean matchesDay = "ALL".equalsIgnoreCase(currentDay) ||
                        (slot.getDayOfWeek() != null && slot.getDayOfWeek().equalsIgnoreCase(currentDay));

                if (!matchesDay) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (slot.getSubjectName() != null && slot.getSubjectName().toLowerCase().contains(currentQuery)) ||
                        (slot.getCourseId() != null && slot.getCourseId().toLowerCase().contains(currentQuery)) ||
                        (slot.getDayOfWeek() != null && slot.getDayOfWeek().toLowerCase().contains(currentQuery)) ||
                        (slot.getRoomNumber() != null && slot.getRoomNumber().toLowerCase().contains(currentQuery)) ||
                        (slot.getTeacherName() != null && slot.getTeacherName().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(slot);
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
            TimetableSlot slot = displayedList.get(position);
            holder.tvName.setText(slot.getSubjectName());

            String course = slot.getCourseId() != null ? slot.getCourseId() : "Course";
            String teacher = slot.getTeacherName() != null ? slot.getTeacherName() : "Faculty";
            String room = slot.getRoomNumber() != null ? slot.getRoomNumber() : "Room 301";
            String day = slot.getDayOfWeek() != null ? slot.getDayOfWeek() : "Mon";
            String time = slot.getStartTime() + " - " + slot.getEndTime();

            String actionText = isStaff ? " • Click to Manage ›" : "";
            holder.tvRollEmail.setText(course + " • " + day + " (" + time + ") • " + room + "\nFaculty: " + teacher + actionText);

            String avatarInitials = day.length() >= 3 ? day.substring(0, 3).toUpperCase() : "TT";
            holder.tvAvatar.setText(avatarInitials);

            holder.tvBadge.setText("SCHEDULED");
            holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.primary_blue));
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSlotClick(slot);
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
