package com.example.rollcall.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WindowInsetUtils {

    /**
     * Applies top system status bar inset padding to a header/root view
     * and bottom navigation bar inset padding to the root layout.
     */
    public static void applySystemBarInsets(View rootView, View headerView, View bottomNavView) {
        if (rootView == null) return;

        try {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                try {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    if (headerView != null) {
                        headerView.setPadding(
                                headerView.getPaddingLeft(),
                                insets.top > 0 ? insets.top : headerView.getPaddingTop(),
                                headerView.getPaddingRight(),
                                headerView.getPaddingBottom()
                        );
                    } else {
                        v.setPadding(
                                v.getPaddingLeft(),
                                insets.top > 0 ? insets.top : v.getPaddingTop(),
                                v.getPaddingRight(),
                                v.getPaddingBottom()
                        );
                    }

                    if (bottomNavView != null) {
                        bottomNavView.setPadding(
                                bottomNavView.getPaddingLeft(),
                                bottomNavView.getPaddingTop(),
                                bottomNavView.getPaddingRight(),
                                insets.bottom > 0 ? insets.bottom : bottomNavView.getPaddingBottom()
                        );
                    }
                } catch (Exception ignored) {}
                return windowInsets;
            });
        } catch (Exception ignored) {}
    }
}
