package utils;

import models.User;

/**
 * Singleton class to manage user session across the application
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String currentUserEmail;

    // Private constructor to prevent instantiation
    private SessionManager() {
        // Initialize with default values
        currentUserEmail = null;
        currentUser = null;
    }

    /**
     * Get the singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getEmail() != null) {
            this.currentUserEmail = user.getEmail();
        }
    }

    /**
     * Get the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Set the current user email
     */
    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    /**
     * Get the current user email
     */
    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    /**
     * Check if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear the current session (logout)
     */
    public void clearSession() {
        currentUser = null;
        currentUserEmail = null;
    }
}