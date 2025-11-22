package com.kuspidsamples.util;

public class Constants {

    // JWT Constants
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final long ACCESS_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 24 hours
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

    // API Endpoints
    public static final String API_BASE_PATH = "/api";
    public static final String AUTH_BASE_PATH = API_BASE_PATH + "/auth";
    public static final String USER_BASE_PATH = API_BASE_PATH + "/users";
    public static final String SAMPLE_BASE_PATH = API_BASE_PATH + "/samples";

    // Public endpoints (no authentication required)
    public static final String[] PUBLIC_URLS = {
            AUTH_BASE_PATH + "/login",
            AUTH_BASE_PATH + "/register",
            AUTH_BASE_PATH + "/refresh",
            "/error",
            "/health"
    };

    // Error Messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ACCOUNT_LOCKED = "Account is locked due to multiple failed login attempts";
    public static final String ACCOUNT_DISABLED = "Account is disabled";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String SAMPLE_NOT_FOUND = "Sample not found";
    public static final String UNAUTHORIZED_ACCESS = "You don't have permission to access this resource";

    // Validation Messages
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters";

    // Success Messages
    public static final String REGISTRATION_SUCCESS = "User registered successfully";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String PASSWORD_UPDATED = "Password updated successfully";
    public static final String PROFILE_UPDATED = "Profile updated successfully";

    // Cloudinary
    public static final String CLOUDINARY_FOLDER = "kuspid-samples";
    public static final int MAX_FILE_SIZE_MB = 10;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private Constants() {
        // Prevent instantiation
    }
}