package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.rollcall.utils.PreferenceManager;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private Button btnNext;
    private PreferenceManager prefManager;
    private List<OnboardingAdapter.OnboardingItem> onboardingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        prefManager = new PreferenceManager(this);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        layoutDots = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btnNext);
        View btnSkip = findViewById(R.id.btnSkip);

        setupOnboardingItems();
        OnboardingAdapter adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Get Started 🚀");
                } else {
                    btnNext.setText("Next Slide ›");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < onboardingItems.size()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> finishOnboarding());
        }
    }

    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingAdapter.OnboardingItem(
                "Dynamic QR Attendance",
                "Mark classroom attendance instantly with 50-second auto-rotating cryptographic QR codes.",
                R.drawable.ic_qr_code
        ));
        onboardingItems.add(new OnboardingAdapter.OnboardingItem(
                "25m GPS Geofence Radius",
                "Attendance is verified in real-time using strict classroom GPS location proximity.",
                R.drawable.ic_location
        ));
        onboardingItems.add(new OnboardingAdapter.OnboardingItem(
                "Course Analytics & Reports",
                "View subject-wise attendance percentages, threshold warnings, and export instant PDF/CSV reports.",
                R.drawable.ic_reports
        ));
    }

    private void setupDots(int currentPosition) {
        layoutDots.removeAllViews();
        View[] dots = new View[onboardingItems.size()];

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == currentPosition ? 32 : 10,
                    10
            );
            params.setMargins(6, 0, 6, 0);
            dots[i].setLayoutParams(params);
            dots[i].setBackgroundResource(i == currentPosition ? R.drawable.bg_button_primary : R.drawable.bg_input_field);
            layoutDots.addView(dots[i]);
        }
    }

    private void finishOnboarding() {
        prefManager.setOnboardingCompleted(true);
        Intent intent = new Intent(OnboardingActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
