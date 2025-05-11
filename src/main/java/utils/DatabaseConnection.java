package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        // Get database configuration from ConfigManager
        ConfigManager config = ConfigManager.getInstance();
        String url = config.getDatabaseUrl();
        String user = config.getDatabaseUser();
        String password = config.getDatabasePassword();
        String driver = config.getDatabaseDriver();
        
        try {
            Class.forName(driver);
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database: " + url);
        } catch (ClassNotFoundException ex) {
            System.err.println("Database Driver not found: " + ex.getMessage());
            throw new SQLException("Database Driver not found", ex);
        }
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }

        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}