package utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing passwords and verifying password hashes
 * using the BCrypt hashing algorithm.
 */
public class PasswordHasher {
    
    /**
     * Hash a password using BCrypt algorithm
     * 
     * @param plainTextPassword The password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        // Generate a salt and hash the password
        String salt = BCrypt.gensalt(12); // 12 is the log rounds (work factor)
        return BCrypt.hashpw(plainTextPassword, salt);
    }
    
    /**
     * Verify a password against a stored hash
     * 
     * @param plainTextPassword The password to check
     * @param hashedPassword The stored hashed password to check against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            // Check if the password matches the hash
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // This can happen if the stored hash is not a valid BCrypt hash
            // (e.g., if it's a plain text password from before hashing was implemented)
            return false;
        }
    }
    
    /**
     * Check if a password is already hashed with BCrypt
     * 
     * @param password The password to check
     * @return true if the password is already a BCrypt hash, false otherwise
     */
    public static boolean isPasswordHashed(String password) {
        if (password == null || password.length() < 60) { // BCrypt hashes are at least 60 chars
            return false;
        }
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}