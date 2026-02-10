package it.unicam.cs.mpgc.jtime122631.repository;

import it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager;
import it.unicam.cs.mpgc.jtime122631.model.Task;
import it.unicam.cs.mpgc.jtime122631.model.TaskStatus;
import it.unicam.cs.mpgc.jtime122631.model.TaskPriority;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class H2TaskRepository implements TaskRepository {

    @Override
    public Task save(Task task) {
        boolean isUpdate = task.getId() > 0;
        String sql = isUpdate
                ? "UPDATE task SET title=?, status=?, priority=?, estimated_minutes=?, actual_minutes=?, scheduled_date=? WHERE id=?"
                : "INSERT INTO task (project_id, title, status, priority, estimated_minutes, actual_minutes, scheduled_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            if (!isUpdate) stmt.setInt(i++, task.getProjectId());

            stmt.setString(i++, task.getTitle());
            stmt.setString(i++, task.getStatus().name());
            stmt.setString(i++, task.getPriority() != null ? task.getPriority().name() : TaskPriority.NORMALE.name());
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
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return new Task(rs.getInt(1), task.getProjectId(), task.getTitle(), task.getStatus(),
                                task.getPriority(), task.getEstimatedDuration(), task.getActualDuration(), task.getScheduledDate());
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
        return executeQuery("SELECT * FROM task WHERE project_id = ?", stmt -> stmt.setInt(1, projectId));
    }

    @Override
    public List<Task> findByDate(LocalDate date) {
        return executeQuery("SELECT * FROM task WHERE scheduled_date = ?", stmt -> stmt.setDate(1, Date.valueOf(date)));
    }

    @Override
    public List<Task> findOverdueTasks(LocalDate today) {
        return executeQuery("SELECT * FROM task WHERE status != 'COMPLETED' AND scheduled_date < ?", stmt -> stmt.setDate(1, Date.valueOf(today)));
    }

    @Override
    public Task findById(int id) {
        List<Task> results = executeQuery("SELECT * FROM task WHERE id = ?", stmt -> stmt.setInt(1, id));
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Task> findAll() {
        return executeQuery("SELECT * FROM task", stmt -> {});
    }

    @Override
    public void deleteById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM task WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminazione task", e);
        }
    }

    @Override
    public void updateDate(int taskId, LocalDate newDate) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE task SET scheduled_date = ? WHERE id = ?")) {
            if (newDate != null) stmt.setDate(1, Date.valueOf(newDate));
            else stmt.setNull(1, Types.DATE);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento data task", e);
        }
    }

    private List<Task> executeQuery(String sql, StatementBinder binder) {
        List<Task> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore esecuzione query task", e);
        }
        return list;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("scheduled_date");
        return new Task(
                rs.getInt("id"),
                rs.getInt("project_id"),
                rs.getString("title"),
                TaskStatus.valueOf(rs.getString("status")),
                TaskPriority.valueOf(rs.getString("priority")),
                Duration.ofMinutes(rs.getLong("estimated_minutes")),
                Duration.ofMinutes(rs.getLong("actual_minutes")),
                sqlDate != null ? sqlDate.toLocalDate() : null
        );
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement stmt) throws SQLException;
    }
}