package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;
    private static String adminConnectionUrl;
    private static String dbDriver;
    private static String dbType;







    public static Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unknown database") || ex.getMessage().contains("does not exist")) {
                try {
                    // Attempt to create the database, then retry connection
                    createDatabase();
                    createTables();
                    return DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
                } catch (SQLException retryEx) {
                    throw new DataAccessException("Failed to create and connect to database", retryEx);
                }
            } else {
                throw new DataAccessException("Failed to get connection", ex);
            }
        }
    }
    public static void createDatabase() throws DataAccessException {

        System.out.println("Creating database: " + databaseName);
        // ... existing code ...
        System.out.println("Database created (or already exists).");

        String statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (Connection conn = DriverManager.getConnection(adminConnectionUrl, dbUsername, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(statement)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create database", ex);
        }
    }
    static {
        loadPropertiesFromResources();
    }

    public static void createTables() throws DataAccessException {
        String[] sqlCommands;
        System.out.println("Creating tables in database: " + databaseName);
        // ... existing code ...
        System.out.println("Tables created (or already exist).");

        switch (dbType) {
            case "mysql" -> sqlCommands = new String[]{
                    """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(100) PRIMARY KEY,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(255)
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS auth_token (
                    token VARCHAR(100) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS game (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    white_username VARCHAR(100),
                    black_username VARCHAR(100),
                    game_name VARCHAR(255) NOT NULL UNIQUE,
                    game_state TEXT NOT NULL,
                    FOREIGN KEY (white_username) REFERENCES user(username) ON DELETE SET NULL,
                    FOREIGN KEY (black_username) REFERENCES user(username) ON DELETE SET NULL
                )
                """
            };
            case "sqlite" -> sqlCommands = new String[]{
                    """
                CREATE TABLE IF NOT EXISTS user (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL,
                    email TEXT
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS auth_token (
                    token TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS game (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    white_username TEXT,
                    black_username TEXT,
                    game_name TEXT NOT NULL UNIQUE,
                    game_state TEXT NOT NULL,
                    FOREIGN KEY (white_username) REFERENCES user(username) ON DELETE SET NULL,
                    FOREIGN KEY (black_username) REFERENCES user(username) ON DELETE SET NULL
                )
                """
            };
            case "postgresql" -> sqlCommands = new String[]{
                    """
                CREATE TABLE IF NOT EXISTS "user" (
                    username VARCHAR(100) PRIMARY KEY,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(255)
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS auth_token (
                    token VARCHAR(100) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    FOREIGN KEY (username) REFERENCES "user"(username) ON DELETE CASCADE
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS game (
                    id SERIAL PRIMARY KEY,
                    white_username VARCHAR(100),
                    black_username VARCHAR(100),
                    game_name VARCHAR(255) NOT NULL UNIQUE,
                    game_state TEXT NOT NULL,
                    FOREIGN KEY (white_username) REFERENCES "user"(username) ON DELETE SET NULL,
                    FOREIGN KEY (black_username) REFERENCES "user"(username) ON DELETE SET NULL
                )
                """
            };
            default -> throw new DataAccessException("Unsupported database type: " + dbType);
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String command : sqlCommands) {
                stmt.executeUpdate(command);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create tables", ex);
        }
    }
    /**
     * Loads properties from the db.properties file.
     */
    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                System.err.println("ERROR: db.properties file not found in resources!");
                throw new RuntimeException("Unable to load db.properties");
            }
            System.out.println("db.properties loaded successfully!");
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
            System.out.println("Loaded properties: " + props);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Unable to process db.properties", ex);
        }
    }


    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        String host = props.getProperty("db.host");
        int port = Integer.parseInt(props.getProperty("db.port"));

        dbType = props.getProperty("db.type");
        if (dbType == null) {
            dbType = "mysql";  // default to mysql since your props imply mysql settings
        }

        if (dbType.equals("mysql")) {
            adminConnectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
            connectionUrl = String.format("jdbc:mysql://%s:%d/%s?allowMultiQueries=true", host, port, databaseName);
        }
        // Add else if here for other db types if needed in future
    }
}