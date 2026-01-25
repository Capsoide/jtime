package it.unicam.cs.mpgc.jtime122631.repository;

import it.unicam.cs.mpgc.jtime122631.model.Task;
import java.time.LocalDate;
import java.util.List;

public interface TaskRepository {
    Task save(Task task);
    Task findById(int id);
    List<Task> findAll();
    List<Task> findByProject(int projectId);
    List<Task> findByDate(LocalDate date);
    List<Task> findOverdueTasks(LocalDate today);
    void deleteById(int id);
    void updateDate(int taskId, LocalDate newDate);
}