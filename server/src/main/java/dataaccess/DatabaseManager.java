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

    static {
        loadPropertiesFromResources();
    }

    public static void createDatabase() throws DataAccessException {
        if (!dbType.equals("mysql")) return; // Only MySQL supports CREATE DATABASE this way
        String statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (Connection conn = DriverManager.getConnection(adminConnectionUrl, dbUsername, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(statement)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create database", ex);
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get connection", ex);
        }
    }

    public static void createTables() throws DataAccessException {
        String[] sqlCommands;

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
                throw new RuntimeException("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        String host = props.getProperty("db.host");
        int port = Integer.parseInt(props.getProperty("db.port"));

        // Connection for database creation (no DB name)
        adminConnectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
        // Connection for actual queries (includes DB name and allows multi queries)
        connectionUrl = String.format("jdbc:mysql://%s:%d/%s?allowMultiQueries=true", host, port, databaseName);
    }
}