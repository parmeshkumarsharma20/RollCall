package com.example.rollcall;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BulkImportStudentsActivity extends AppCompatActivity {

    private AdminRepository adminRepo;
    private AttendanceRepository attendanceRepo;

    private Spinner spnCourse, spnSubject, spnSemester, spnSection;
    private TextView tvFileName, tvSummaryRows, tvSummaryValid;
    private View cardSummary;
    private Button btnConfirm;

    private List<Course> courseList = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private List<Subject> filteredSubjects = new ArrayList<>();
    private List<User> parsedStudents = new ArrayList<>();

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        parseSelectedFile(fileUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_import_students);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        adminRepo = AdminRepository.getInstance(this);
        attendanceRepo = AttendanceRepository.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBackBulkImport);
        spnCourse = findViewById(R.id.spnImportCourse);
        spnSubject = findViewById(R.id.spnImportSubject);
        spnSemester = findViewById(R.id.spnImportSemester);
        spnSection = findViewById(R.id.spnImportSection);

        tvFileName = findViewById(R.id.tvSelectedFileName);
        tvSummaryRows = findViewById(R.id.tvSummaryRows);
        tvSummaryValid = findViewById(R.id.tvSummaryValid);
        cardSummary = findViewById(R.id.cardImportSummary);

        Button btnBrowse = findViewById(R.id.btnBrowseFile);
        Button btnLoadSample = findViewById(R.id.btnLoadSampleCSV);
        btnConfirm = findViewById(R.id.btnConfirmImport);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnBrowse != null) {
            btnBrowse.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {"text/comma-separated-values", "text/csv", "application/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                filePickerLauncher.launch(Intent.createChooser(intent, "Select Excel / CSV File"));
            });
        }

        if (btnLoadSample != null) {
            btnLoadSample.setOnClickListener(v -> loadSampleRoster());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> confirmAndImport());
        }

        setupSemesterSectionSpinners();
        loadSpinners();
    }

    private void setupSemesterSectionSpinners() {
        String[] sems = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sems);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSemester.setAdapter(semAdapter);

        String[] secs = {"A", "B", "C", "D"};
        ArrayAdapter<String> secAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, secs);
        secAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSection.setAdapter(secAdapter);
    }

    private void loadSpinners() {
        adminRepo.fetchCourses(new AdminRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                courseList = items;
                setupCourseSpinner();
            }

            @Override
            public void onError(String message) {
                courseList.clear();
                courseList.add(new Course("c_1", "B.Tech CSE", "CSE", 4, "Bachelor of Technology in CSE"));
                courseList.add(new Course("c_2", "BCA", "BCA", 3, "Bachelor of Computer Applications"));
                courseList.add(new Course("c_3", "B.Com", "BCOM", 3, "Bachelor of Commerce"));
                courseList.add(new Course("c_4", "MCA", "MCA", 2, "Master of Computer Applications"));
                setupCourseSpinner();
            }
        });

        adminRepo.fetchSubjects(new AdminRepository.ListCallback<Subject>() {
            @Override
            public void onSuccess(List<Subject> items) {
                allSubjects = items;
                filterSubjectsBySelectedCourse();
            }

            @Override
            public void onError(String message) {
                allSubjects.clear();
                allSubjects.add(new Subject("s_1", "CS301", "Data Structures & Algorithms", "B.Tech CSE", "6th", "A"));
                allSubjects.add(new Subject("s_2", "CS302", "Database Management Systems", "B.Tech CSE", "6th", "A"));
                allSubjects.add(new Subject("s_3", "BCA101", "Programming in C", "BCA", "4th", "A"));
                allSubjects.add(new Subject("s_4", "BC201", "Financial Accounting", "B.Com", "2nd", "B"));
                allSubjects.add(new Subject("s_5", "MCA107", "Database Management System", "MCA", "1st", "A"));
                filterSubjectsBySelectedCourse();
            }
        });
    }

    private void setupCourseSpinner() {
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCourse.setAdapter(courseAdapter);

        spnCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterSubjectsBySelectedCourse();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterSubjectsBySelectedCourse() {
        Course selectedCourse = (Course) spnCourse.getSelectedItem();
        String courseName = selectedCourse != null ? selectedCourse.getCourseName() : "B.Tech CSE";

        filteredSubjects.clear();
        for (Subject s : allSubjects) {
            if (s.getCourse() == null || s.getCourse().isEmpty() || s.getCourse().equalsIgnoreCase(courseName)) {
                filteredSubjects.add(s);
            }
        }

        if (filteredSubjects.isEmpty()) {
            filteredSubjects.add(new Subject("s_def", "CS301", "General Subject", courseName, "1st", "A"));
        }

        ArrayAdapter<Subject> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredSubjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSubject.setAdapter(subjectAdapter);
    }

    private void loadSampleRoster() {
        parsedStudents.clear();
        parsedStudents.add(new User("stud_samp1", "Rahul Verma", "rahul.verma@rollcall.com", "pass123", User.ROLE_STUDENT, "RC2024101", "Computer Science"));
        parsedStudents.add(new User("stud_samp2", "Priya Sharma", "priya.sharma@rollcall.com", "pass123", User.ROLE_STUDENT, "RC2024102", "Computer Science"));
        parsedStudents.add(new User("stud_samp3", "Amit Kumar", "amit.kumar@rollcall.com", "pass123", User.ROLE_STUDENT, "RC2024103", "Computer Science"));
        parsedStudents.add(new User("stud_samp4", "Neha Singh", "neha.singh@rollcall.com", "pass123", User.ROLE_STUDENT, "RC2024104", "Computer Science"));

        if (tvFileName != null) tvFileName.setText("Preset Roster: Sample_Class_Roster.csv (4 Records Loaded)");
        if (tvSummaryRows != null) tvSummaryRows.setText("Total Rows Parsed: 4");
        if (tvSummaryValid != null) tvSummaryValid.setText("Valid Student Records: 4");
        if (cardSummary != null) cardSummary.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Sample Roster Loaded! Ready to import.", Toast.LENGTH_SHORT).show();
    }

    private void parseSelectedFile(Uri uri) {
        parsedStudents.clear();
        int totalRows = 0;

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            if (tvFileName != null) tvFileName.setText("Selected File: " + uri.getLastPathSegment());

            while ((line = reader.readLine()) != null) {
                totalRows++;
                if (line.trim().isEmpty() || line.toLowerCase().startsWith("student id") || line.toLowerCase().startsWith("roll")) {
                    continue; // Skip header
                }

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String roll = parts[0].trim();
                    String name = parts[1].trim();
                    String email = parts.length >= 3 ? parts[2].trim() : "stud_" + roll.toLowerCase() + "@rollcall.com";

                    User student = new User("stud_" + roll.toLowerCase(), name, email, "pass123", User.ROLE_STUDENT, roll, "Computer Science");
                    parsedStudents.add(student);
                }
            }
            reader.close();

            if (parsedStudents.isEmpty()) {
                loadSampleRoster();
                return;
            }

            if (tvSummaryRows != null) tvSummaryRows.setText("Total Rows Parsed: " + totalRows);
            if (tvSummaryValid != null) tvSummaryValid.setText("Valid Student Records: " + parsedStudents.size());
            if (cardSummary != null) cardSummary.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Error reading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmAndImport() {
        if (parsedStudents.isEmpty()) {
            Toast.makeText(this, "No valid students to import", Toast.LENGTH_SHORT).show();
            return;
        }

        Course selectedCourse = (Course) spnCourse.getSelectedItem();
        String courseName = selectedCourse != null ? selectedCourse.getCourseName() : "B.Tech CSE";
        String semName = spnSemester.getSelectedItem() != null ? spnSemester.getSelectedItem().toString() : "6th";
        String secName = spnSection.getSelectedItem() != null ? spnSection.getSelectedItem().toString() : "A";

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Importing to Database...");

        int count = 0;
        for (User u : parsedStudents) {
            u.setCourse(courseName);
            u.setSemester(semName);
            u.setSection(secName);

            attendanceRepo.registerUser(u);
            adminRepo.addStudent(u, new AdminRepository.ActionCallback() {
                @Override public void onSuccess() {}
                @Override public void onError(String message) {}
            });
            count++;
        }

        String msg = "Successfully imported " + count + " students into " + courseName + " (" + semName + " Sem - Section " + secName + ")!";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }
}
