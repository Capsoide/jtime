package it.unicam.cs.mpgc.jtime122631.repository;

import it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager;
import it.unicam.cs.mpgc.jtime122631.model.Project;
import it.unicam.cs.mpgc.jtime122631.model.ProjectStatus;
import it.unicam.cs.mpgc.jtime122631.service.JTimeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class H2ProjectRepository implements ProjectRepository {

    @Override
    public Project save(Project project) {
        boolean isUpdate = project.getId() > 0 && existsById(project.getId());
        String sql = isUpdate
                ? "UPDATE project SET name=?, description=?, status=? WHERE id=?"
                : "INSERT INTO project (name, description, status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getStatus().name());

            if (isUpdate) {
                stmt.setInt(4, project.getId());
                stmt.executeUpdate();
                return project;
            } else {
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return new Project(generatedKeys.getInt(1), project.getName(), project.getDescription(), project.getStatus());
                    }
                }
            }
        } catch (SQLException e) {
            throw new JTimeException("Errore durante il salvataggio del progetto: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Project findById(int id) {
        List<Project> result = executeQuery("SELECT * FROM project WHERE id = ?", stmt -> stmt.setInt(1, id));
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Project> findAll() {
        return executeQuery("SELECT * FROM project", stmt -> {});
    }

    @Override
    public void deleteById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new JTimeException("Errore durante l'eliminazione del progetto: " + e.getMessage());
        }
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT 1 FROM project WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private List<Project> executeQuery(String sql, StatementBinder binder) {
        List<Project> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Project(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            ProjectStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new JTimeException("Errore esecuzione query progetto: " + e.getMessage());
        }
        return list;
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement stmt) throws SQLException;
    }
}