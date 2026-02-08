package it.unicam.cs.mpgc.jtime122631.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./jtime_db";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initialize() {
        String sqlProject = """
            CREATE TABLE IF NOT EXISTS project (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description VARCHAR(1000),
                status VARCHAR(50) NOT NULL
            );
        """;

        String sqlTask = """
            CREATE TABLE IF NOT EXISTS task (
                id INT AUTO_INCREMENT PRIMARY KEY,
                project_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                status VARCHAR(50) NOT NULL,
                priority VARCHAR(20) DEFAULT 'NORMALE',
                estimated_minutes BIGINT DEFAULT 0,
                actual_minutes BIGINT DEFAULT 0,
                scheduled_date DATE,
                FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlProject);
            stmt.execute(sqlTask);
            System.out.println("Database inizializzato correttamente: Tabelle pronte.");

        } catch (SQLException e) {
            System.err.println("Errore inizializzazione DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}