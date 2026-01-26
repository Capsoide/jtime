package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TaskService {
    void createTask(int projectId, String title, LocalDate scheduledDate, Duration estimated);
    void updateTask(int id, String title, LocalDate scheduledDate, Duration estimated);
    void completeTask(int taskId, Duration actualDuration);
    void deleteTask(int taskId);
    void rescheduleTask(int taskId, LocalDate newDate);

    List<InfoTask> getTasksByProject(int projectId);
    List<InfoTask> getDailyPlan(LocalDate date);
    List<InfoTask> getOverdueTasks();
    void rescheduleAllToToday(List<InfoTask> tasks);

    long countTotalTasks();
    long countCompletedTasks();
    Duration getTotalEstimatedTime();
    Duration getTotalActualTime();
    Duration getTotalEstimatedTimeForDate(LocalDate date);
    Map<LocalDate, Long> getWeeklyProductivity();
}