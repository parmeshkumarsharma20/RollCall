package com.example.rollcall.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "rollcall_app_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_completed";
    private static final String KEY_SELECTED_ROLE = "selected_role";

    private static final String KEY_LOGGED_IN_UID = "logged_in_uid";
    private static final String KEY_LOGGED_IN_ROLE = "logged_in_role";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply();
    }

    public String getSelectedRole() {
        return prefs.getString(KEY_SELECTED_ROLE, null);
    }

    public void setSelectedRole(String role) {
        prefs.edit().putString(KEY_SELECTED_ROLE, role).apply();
    }

    public void clearSelectedRole() {
        prefs.edit().remove(KEY_SELECTED_ROLE).apply();
    }

    public void setLoggedInUser(String uid, String role) {
        prefs.edit().putString(KEY_LOGGED_IN_UID, uid).putString(KEY_LOGGED_IN_ROLE, role).apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_LOGGED_IN_UID) && prefs.getString(KEY_LOGGED_IN_UID, null) != null;
    }

    public String getLoggedInUid() {
        return prefs.getString(KEY_LOGGED_IN_UID, null);
    }

    public String getLoggedInRole() {
        return prefs.getString(KEY_LOGGED_IN_ROLE, null);
    }

    public void logoutUser() {
        prefs.edit().remove(KEY_LOGGED_IN_UID).remove(KEY_LOGGED_IN_ROLE).apply();
    }
}
