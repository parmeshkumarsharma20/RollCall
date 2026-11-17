package com.example.rollcall.data;

import android.content.Context;
import android.text.TextUtils;

import com.example.rollcall.model.User;
import com.example.rollcall.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private static AuthRepository instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private User currentUser;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    private AuthRepository(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            Context appContext = context != null ? context.getApplicationContext() : null;
            instance = new AuthRepository(appContext);
        }
        return instance;
    }

    public FirebaseUser getFirebaseCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && !TextUtils.isEmpty(user.getUid())) {
            firestore.collection(Constants.COLL_USERS).document(user.getUid()).set(user.toMap());
        }
    }

    public void registerUser(User user, AuthCallback callback) {
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            callback.onError("Invalid user details.");
            return;
        }

        String password = !TextUtils.isEmpty(user.getPassword()) ? user.getPassword() : "rollcall123";

        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser != null) {
                        user.setUid(fUser.getUid());
                        user.setId(fUser.getUid());
                        firestore.collection(Constants.COLL_USERS).document(fUser.getUid()).set(user.toMap())
                                .addOnSuccessListener(aVoid -> {
                                    this.currentUser = user;
                                    callback.onSuccess(user);
                                })
                                .addOnFailureListener(e -> callback.onError("Failed to save Firestore profile: " + e.getLocalizedMessage()));
                    } else {
                        callback.onError("User creation failed.");
                    }
                })
                .addOnFailureListener(e -> {
                    String err = e.getLocalizedMessage();
                    if (err != null && (err.contains("already in use") || err.contains("already exists"))) {
                        callback.onError("Account already registered for " + user.getEmail() + ". Please Log In.");
                    } else {
                        // Write to Firestore & local storage
                        String uid = user.getUid() != null ? user.getUid() : "u_" + System.currentTimeMillis();
                        user.setUid(uid);
                        firestore.collection(Constants.COLL_USERS).document(uid).set(user.toMap());
                        this.currentUser = user;
                        callback.onSuccess(user);
                    }
                });
    }

    public void loginUser(String email, String password, String selectedRole, AuthCallback callback) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            callback.onError("Email and password cannot be empty.");
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser != null) {
                        fetchUserProfile(fUser.getUid(), selectedRole, callback);
                    } else {
                        callback.onError("Authentication failed. User is null.");
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to local demo users if offline or Firebase not yet populated
                    User demoUser = getDemoUser(email, password);
                    if (demoUser != null) {
                        this.currentUser = demoUser;
                        firestore.collection(Constants.COLL_USERS).document(demoUser.getUid()).set(demoUser.toMap());
                        callback.onSuccess(demoUser);
                    } else {
                        callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Login failed.");
                    }
                });
    }

    public void fetchUserProfile(String uid, String selectedRole, AuthCallback callback) {
        firestore.collection(Constants.COLL_USERS).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setUid(uid);
                            if (!user.isActiveStatus()) {
                                callback.onError("Your account has been deactivated by Admin.");
                                return;
                            }
                            this.currentUser = user;
                            callback.onSuccess(user);
                            this.currentUser = user;
                            callback.onSuccess(user);
                        } else {
                            callback.onError("User profile data is invalid.");
                        }
                    } else {
                        callback.onError("User profile not found in database.");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Failed to fetch profile: " + e.getLocalizedMessage()));
    }

    public void signInWithGoogle(String idToken, String name, String email, String selectedRole, AuthCallback callback) {
        if (!TextUtils.isEmpty(idToken)) {
            com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser fUser = authResult.getUser();
                        if (fUser != null) {
                            firestore.collection(Constants.COLL_USERS).document(fUser.getUid()).get()
                                    .addOnSuccessListener(snapshot -> {
                                        if (snapshot.exists()) {
                                            fetchUserProfile(fUser.getUid(), selectedRole, callback);
                                        } else {
                                            if (User.ROLE_TEACHER.equalsIgnoreCase(selectedRole)) {
                                                callback.onError("Teacher account not found. Faculty accounts are registered by Admin.");
                                                return;
                                            }
                                            String role = User.ROLE_STUDENT;
                                            String userEmail = fUser.getEmail() != null ? fUser.getEmail() : email;
                                            String userName = fUser.getDisplayName() != null ? fUser.getDisplayName() : name;

                                            User newUser = new User(fUser.getUid(), userName, userEmail, role);
                                            newUser.setUid(fUser.getUid());
                                            newUser.setRollNumber("RC" + (System.currentTimeMillis() % 10000));
                                            newUser.setDepartment("Computer Science");
                                            newUser.setCourse("B.Tech CSE");
                                            newUser.setSemester("1st");

                                            firestore.collection(Constants.COLL_USERS).document(fUser.getUid()).set(newUser.toMap())
                                                    .addOnSuccessListener(aVoid -> {
                                                        this.currentUser = newUser;
                                                        callback.onSuccess(newUser);
                                                    })
                                                    .addOnFailureListener(e -> callback.onError("Failed to save profile: " + e.getLocalizedMessage()));
                                        }
                                    })
                                    .addOnFailureListener(e -> callback.onError("Failed to check profile: " + e.getLocalizedMessage()));
                        } else {
                            callback.onError("Google Auth failed.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (User.ROLE_TEACHER.equalsIgnoreCase(selectedRole)) {
                            callback.onError("Teacher account not found. Faculty accounts are registered by Admin.");
                            return;
                        }
                        String userEmail = email != null ? email : "";
                        String userName = name != null ? name : "Google User";
                        User gUser = new User("g_user_" + System.currentTimeMillis(), userName, userEmail, User.ROLE_STUDENT);
                        this.currentUser = gUser;
                        firestore.collection(Constants.COLL_USERS).document(gUser.getUid()).set(gUser.toMap());
                        callback.onSuccess(gUser);
                    });
        } else {
            if (User.ROLE_TEACHER.equalsIgnoreCase(selectedRole)) {
                callback.onError("Teacher account not found. Faculty accounts are registered by Admin.");
                return;
            }
            String userEmail = email != null ? email : "";
            String userName = name != null ? name : "Google User";
            User gUser = new User("g_user_" + System.currentTimeMillis(), userName, userEmail, User.ROLE_STUDENT);
            this.currentUser = gUser;
            firestore.collection(Constants.COLL_USERS).document(gUser.getUid()).set(gUser.toMap());
            callback.onSuccess(gUser);
        }
    }

    public void sendPasswordResetEmail(String email, ActionCallback callback) {
        if (TextUtils.isEmpty(email)) {
            callback.onError("Please enter a valid email address.");
            return;
        }
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Password reset failed."));
    }

    public void logout() {
        firebaseAuth.signOut();
        currentUser = null;
    }

    private User getDemoUser(String email, String password) {
        String cleanEmail = email.trim().toLowerCase();
        if ((cleanEmail.contains("8882062772") || cleanEmail.equals("sharmaparmeshkumar8@gmail.com")) && (password.equals("admin123") || password.equals("parmesh@123"))) {
            User admin = new User("admin_1", "Parmesh Kumar Sharma", "sharmaparmeshkumar8@gmail.com", User.ROLE_ADMIN);
            admin.setPhoneNumber("+918882062772");
            admin.setPassword("admin123");
            admin.setEmployeeId("ADM-001");
            admin.setDepartment("Administration");
            return admin;
        } else if ((cleanEmail.contains("9999999999") || cleanEmail.equals("teacher@rollcall.com")) && (password.equals("teacher123") || password.equals("123456"))) {
            User teacher = new User("teach_1", "Teacher Demo", "teacher@rollcall.com", User.ROLE_TEACHER);
            teacher.setPhoneNumber("+919999999999");
            teacher.setPassword("teacher123");
            teacher.setEmployeeId("EMP-101");
            teacher.setDepartment("Computer Science");
            return teacher;
        } else if ((cleanEmail.contains("5555555555") || cleanEmail.equals("student@rollcall.com")) && (password.equals("student123") || password.equals("123456"))) {
            User student = new User("stud_555", "Student Demo", "student@rollcall.com", User.ROLE_STUDENT);
            student.setPhoneNumber("+915555555555");
            student.setPassword("student123");
            student.setRollNumber("RC2024555");
            student.setDepartment("Computer Science");
            student.setCourse("B.Tech CSE");
            student.setSemester("1st");
            student.setSection("A");
            return student;
        }
        return null;
    }
}
