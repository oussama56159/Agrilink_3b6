package services;

import models.User;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserServiceImpl implements UserService {
    private Connection connection;
    private Set<String> availableColumns = new HashSet<>();

    public UserServiceImpl() {
        try {
            connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null) {
                throw new SQLException("Failed to obtain database connection");
            }
            
            // Initialize availableColumns by querying database metadata
            try (ResultSet rs = connection.getMetaData().getColumns(null, null, "users", null)) {
                System.out.println("Available columns in users table:");
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                    availableColumns.add(columnName);
                    System.out.println("- " + columnName);
                }
            }
            System.out.println("Total available columns: " + availableColumns.size());

        } catch (Exception e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }


    private boolean hasColumn(String columnName) {
        return availableColumns.contains(columnName.toLowerCase());
    }

    @Override
    public void Create(User user) throws Exception {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        System.out.println("Attempting to create user: " + user.getFirstName() + " " + user.getLastName());

        // Check if email already exists
        if (emailExists(user.getEmail())) {
            throw new Exception("A user with this email already exists");
        }

        // Build the SQL query dynamically based on available columns
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO users (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");

        // Add required fields
        queryBuilder.append("first_name, last_name, email, password, role, type, status, registration_date");
        valuesBuilder.append("?, ?, ?, ?, ?, ?, ?, ?");

        // Add optional fields if they exist in the table
        if (hasColumn("address")) {
            queryBuilder.append(", address");
            valuesBuilder.append(", ?");
        }
        if (hasColumn("city")) {
            queryBuilder.append(", city");
            valuesBuilder.append(", ?");
        }
        if (hasColumn("postal_code")) {
            queryBuilder.append(", postal_code");
            valuesBuilder.append(", ?");
        }
        if (hasColumn("profile_image_path")) {
            queryBuilder.append(", profile_image_path");
            valuesBuilder.append(", ?");
        }
        if (hasColumn("birth_date")) {
            queryBuilder.append(", birth_date");
            valuesBuilder.append(", ?");
        }
        if (hasColumn("biography")) {
            queryBuilder.append(", biography");
            valuesBuilder.append(", ?");
        }

        // Close the query
        queryBuilder.append(") ");
        valuesBuilder.append(")");

        String query = queryBuilder.toString() + valuesBuilder.toString();
        System.out.println("Executing SQL: " + query);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, user.getFirstName());
            ps.setString(paramIndex++, user.getLastName());
            ps.setString(paramIndex++, user.getEmail());
            ps.setString(paramIndex++, user.getPassword());
            ps.setString(paramIndex++, user.getRole());
            ps.setString(paramIndex++, user.getType());
            ps.setString(paramIndex++, user.getStatus());
            ps.setObject(paramIndex++, user.getRegistrationDate());

            if (hasColumn("address")) ps.setString(paramIndex++, user.getAddress());
            if (hasColumn("city")) ps.setString(paramIndex++, user.getCity());
            if (hasColumn("postal_code")) ps.setString(paramIndex++, user.getPostalCode());
            if (hasColumn("profile_image_path")) ps.setString(paramIndex++, user.getProfileImagePath());
            if (hasColumn("birth_date")) ps.setObject(paramIndex++, user.getBirthDate());
            if (hasColumn("biography")) ps.setString(paramIndex++, user.getBiography());

            int rowsAffected = ps.executeUpdate();
            System.out.println("Insert completed. Rows affected: " + rowsAffected);

            if (rowsAffected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public void update(User user) throws Exception {
        StringBuilder queryBuilder = new StringBuilder(
                "UPDATE users SET first_name = ?, last_name = ?, password = ?, role = ?, type = ?, status = ?"
        );

        // Add phone if present
        if (hasColumn("phone")) {
            queryBuilder.append(", phone = ?");
            System.out.println("Phone column exists and will be updated");
        } else {
            System.out.println("Phone column does not exist in the database");
        }

        // Add biography if present
        if (hasColumn("biography")) {
            queryBuilder.append(", biography = ?");
        }

        // Add other optional fields
        if (hasColumn("address")) queryBuilder.append(", address = ?");
        if (hasColumn("city")) queryBuilder.append(", city = ?");
        if (hasColumn("postal_code")) queryBuilder.append(", postal_code = ?");
        if (hasColumn("profile_image_path")) queryBuilder.append(", profile_image_path = ?");
        if (hasColumn("birth_date")) queryBuilder.append(", birth_date = ?");

        queryBuilder.append(" WHERE id = ?");
        String query = queryBuilder.toString();
        System.out.println("Executing update SQL: " + query);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, user.getFirstName());
            ps.setString(paramIndex++, user.getLastName());
            ps.setString(paramIndex++, user.getPassword());
            ps.setString(paramIndex++, user.getRole());
            ps.setString(paramIndex++, user.getType());
            ps.setString(paramIndex++, user.getStatus());

            if (hasColumn("phone")) {
                ps.setString(paramIndex++, user.getPhone());
                System.out.println("Setting phone number: " + user.getPhone());
            }
            if (hasColumn("biography")) ps.setString(paramIndex++, user.getBiography());
            if (hasColumn("address")) ps.setString(paramIndex++, user.getAddress());
            if (hasColumn("city")) ps.setString(paramIndex++, user.getCity());
            if (hasColumn("postal_code")) ps.setString(paramIndex++, user.getPostalCode());
            if (hasColumn("profile_image_path")) ps.setString(paramIndex++, user.getProfileImagePath());
            if (hasColumn("birth_date")) ps.setObject(paramIndex++, user.getBirthDate());

            ps.setInt(paramIndex, user.getId());
            int rowsAffected = ps.executeUpdate();
            System.out.println("Update completed. Rows affected: " + rowsAffected);
        }
    }

    @Override
    public void delete(User user) throws Exception {
        String query = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getEmail());
            ps.executeUpdate();
        }
    }

    @Override
    public List<User> DisplayAll() throws Exception {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getObject("registration_date", LocalDate.class)
                );

                if (hasColumn("permissions")) {
                    user.setPermissions(rs.getString("permissions"));
                }
                if (hasColumn("phone")) user.setPhone(rs.getString("phone"));
                if (hasColumn("address")) user.setAddress(rs.getString("address"));
                if (hasColumn("city")) user.setCity(rs.getString("city"));
                if (hasColumn("postal_code")) user.setPostalCode(rs.getString("postal_code"));
                if (hasColumn("profile_image_path")) user.setProfileImagePath(rs.getString("profile_image_path"));
                if (hasColumn("birth_date")) user.setBirthDate(rs.getObject("birth_date", LocalDate.class));
                if (hasColumn("biography")) user.setBiography(rs.getString("biography"));

                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User login(String email, String password) throws Exception {
        if (verifyCredentials(email, password)) {
            String query = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User user = new User(
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email"),
                                rs.getString("role"),
                                rs.getString("type"),
                                rs.getString("status"),
                                rs.getObject("registration_date", LocalDate.class)
                        );
                        if (hasColumn("permissions")) {
                            user.setPermissions(rs.getString("permissions"));
                        }
                        if (hasColumn("phone")) user.setPhone(rs.getString("phone"));
                        if (hasColumn("address")) user.setAddress(rs.getString("address"));
                        if (hasColumn("city")) user.setCity(rs.getString("city"));
                        if (hasColumn("postal_code")) user.setPostalCode(rs.getString("postal_code"));
                        if (hasColumn("profile_image_path")) user.setProfileImagePath(rs.getString("profile_image_path"));
                        if (hasColumn("birth_date")) user.setBirthDate(rs.getObject("birth_date", LocalDate.class));
                        if (hasColumn("biography")) user.setBiography(rs.getString("biography"));
                        return user;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean verifyCredentials(String email, String password) throws Exception {
        String query = "SELECT password FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return storedPassword.equals(password);
                }
            }
        }
        return false;
    }

    @Override
    public User findByEmail(String email) {
        try {
            String query = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setType(rs.getString("type"));
                user.setStatus(rs.getString("status")); // <-- Add this line!
                user.setRegistrationDate(rs.getObject("registration_date", java.time.LocalDate.class));
                // Set other fields as needed
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void add(User user) {
        try {
            String query = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int[] getUserTypeStatistics() throws SQLException {
        int[] stats = new int[3]; // 0: acheteur, 1: agriculteur, 2: grossiste
        String query = "SELECT type, COUNT(*) as count FROM users GROUP BY type";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String type = rs.getString("type");
                int count = rs.getInt("count");
                
                switch (type.toLowerCase()) {
                    case "acheteur":
                        stats[0] = count;
                        break;
                    case "agriculteur":
                        stats[1] = count;
                        break;
                    case "grossiste":
                        stats[2] = count;
                        break;
                }
            }
        }
        return stats;
    }

    @Override
    public void updatePassword(String email, String newPassword) throws Exception {
        String query = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("No user found with the specified email");
            }
        }
    }
}
