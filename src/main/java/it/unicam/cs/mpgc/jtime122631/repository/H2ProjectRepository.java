package it.unicam.cs.mpgc.jtime122631.repository;

import it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager;
import it.unicam.cs.mpgc.jtime122631.model.Project;
import it.unicam.cs.mpgc.jtime122631.model.ProjectStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class H2ProjectRepository implements ProjectRepository {

    @Override
    public Project save(Project project) {
        String sql;
        boolean isUpdate = project.getId() > 0 && existsById(project.getId());

        if (isUpdate) {
            sql = "UPDATE project SET name=?, description=?, status=? WHERE id=?";
        } else {
            sql = "INSERT INTO project (name, description, status) VALUES (?, ?, ?)";
        }

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
                        int newId = generatedKeys.getInt(1);
                        return new Project(newId, project.getName(), project.getDescription(), project.getStatus());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio progetto", e);
        }
        return null;
    }

    @Override
    public Project findById(int id) {
        String sql = "SELECT * FROM project WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore lettura progetto", e);
        }
        return null;
    }

    @Override
    public List<Project> findAll() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Errore lettura lista progetti", e);
        }
        return list;
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM project WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminazione progetto", e);
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

    private Project mapRow(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                ProjectStatus.valueOf(rs.getString("status"))
        );
    }
}