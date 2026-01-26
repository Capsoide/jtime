package it.unicam.cs.mpgc.jtime122631.repository;

import it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager;
import it.unicam.cs.mpgc.jtime122631.model.Task;
import it.unicam.cs.mpgc.jtime122631.model.TaskStatus;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class H2TaskRepository implements TaskRepository {

    @Override
    public Task save(Task task) {
        String sql;
        boolean isUpdate = task.getId() > 0;

        if (isUpdate) {
            sql = "UPDATE task SET title=?, status=?, estimated_minutes=?, actual_minutes=?, scheduled_date=? WHERE id=?";
        } else {
            sql = "INSERT INTO task (project_id, title, status, estimated_minutes, actual_minutes, scheduled_date) VALUES (?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            if (!isUpdate) stmt.setInt(i++, task.getProjectId());

            stmt.setString(i++, task.getTitle());
            stmt.setString(i++, task.getStatus().name());
            stmt.setLong(i++, task.getEstimatedDuration().toMinutes());
            stmt.setLong(i++, task.getActualDuration().toMinutes());

            if (task.getScheduledDate() != null) {
                stmt.setDate(i++, Date.valueOf(task.getScheduledDate()));
            } else {
                stmt.setNull(i++, Types.DATE);
            }

            if (isUpdate) {
                stmt.setInt(i, task.getId());
                stmt.executeUpdate();
                return task;
            } else {
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return new Task(
                                generatedKeys.getInt(1),
                                task.getProjectId(),
                                task.getTitle(),
                                task.getStatus(),
                                task.getEstimatedDuration(),
                                task.getActualDuration(),
                                task.getScheduledDate()
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio task", e);
        }
        return null;
    }

    @Override
    public List<Task> findByProject(int projectId) {
        String sql = "SELECT * FROM task WHERE project_id = ?";
        return executeQuery(sql, projectId);
    }

    @Override
    public List<Task> findByDate(LocalDate date) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE scheduled_date = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca task per data", e);
        }
        return list;
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM task WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminazione task", e);
        }
    }

    @Override
    public Task findById(int id) {
        String sql = "SELECT * FROM task WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore lettura task con id " + id, e);
        }
        return null;
    }

    private List<Task> executeQuery(String sql, int paramId) {
        List<Task> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, paramId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore query task", e);
        }
        return list;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("scheduled_date");
        LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        return new Task(
                rs.getInt("id"),
                rs.getInt("project_id"),
                rs.getString("title"),
                TaskStatus.valueOf(rs.getString("status")),
                Duration.ofMinutes(rs.getLong("estimated_minutes")),
                Duration.ofMinutes(rs.getLong("actual_minutes")),
                localDate
        );
    }

    @Override
    public void updateDate(int taskId, LocalDate newDate) {
        String sql = "UPDATE task SET scheduled_date = ? WHERE id = ?";
        try (java.sql.Connection conn = it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (newDate != null) {
                stmt.setDate(1, java.sql.Date.valueOf(newDate));
            } else {
                stmt.setNull(1, java.sql.Types.DATE);
            }
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Errore aggiornamento data task", e);
        }
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM task";
        List<Task> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore lettura di tutti i task", e);
        }
        return list;
    }

    @Override
    public List<Task> findOverdueTasks(LocalDate today) {
        String sql = "SELECT * FROM TASK WHERE STATUS != 'COMPLETED' AND SCHEDULED_DATE < ?";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(today));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca task scadute", e);
        }
        return tasks;
    }
}