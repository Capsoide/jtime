package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.*;
import it.unicam.cs.mpgc.jtime122631.repository.ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.TaskRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepo, ProjectRepository projectRepo) {
        this.taskRepository = taskRepo;
        this.projectRepository = projectRepo;
    }

    public void createTask(int projectId, String title, LocalDate scheduledDate, Duration estimated) {
        Project project = projectRepository.findById(projectId);
        if (project == null) {
            throw new JTimeException("Progetto non trovato.");
        }
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new JTimeException("Non puoi aggiungere attività a un progetto chiuso.");
        }

        Task newTask = new Task(0, projectId, title, TaskStatus.PENDING, estimated, Duration.ZERO, scheduledDate);

        taskRepository.save(newTask);
    }

    public void updateTask(int id, String title, LocalDate scheduledDate, Duration estimated) {
        Task existing = taskRepository.findById(id);
        if (existing == null) {
            throw new JTimeException("Task non trovato.");
        }

        Task updatedTask = new Task(
                existing.getId(),
                existing.getProjectId(),
                title,
                existing.getStatus(),
                estimated != null ? estimated : Duration.ZERO,
                existing.getActualDuration(),
                scheduledDate
        );

        taskRepository.save(updatedTask);
    }

    public void completeTask(int taskId, Duration actualDuration) {
        Task existingTask = taskRepository.findById(taskId);
        if (existingTask == null) {
            throw new JTimeException("Impossibile completare: Task non trovato (ID: " + taskId + ")");
        }
        if (existingTask.getStatus() == TaskStatus.COMPLETED) {
            throw new JTimeException("Il task è già stato completato.");
        }

        Task completedTask = new Task(
                existingTask.getId(),
                existingTask.getProjectId(),
                existingTask.getTitle(),
                TaskStatus.COMPLETED,
                existingTask.getEstimatedDuration(),
                actualDuration,
                existingTask.getScheduledDate()
        );

        taskRepository.save(completedTask);
    }

    public void deleteTask(int taskId) {
        if (taskRepository.findById(taskId) == null) {
            throw new JTimeException("Task non trovato, impossibile eliminare.");
        }
        taskRepository.deleteById(taskId);
    }

    public List<InfoTask> getTasksByProject(int projectId) {
        return taskRepository.findByProject(projectId).stream()
                .map(t -> (InfoTask) t)
                .collect(Collectors.toList());
    }

    public List<InfoTask> getDailyPlan(LocalDate date) {
        return taskRepository.findByDate(date).stream()
                .map(t -> (InfoTask) t)
                .collect(Collectors.toList());
    }

    public Duration getTotalEstimatedTimeForDate(LocalDate date) {
        List<Task> tasks = taskRepository.findByDate(date);
        return tasks.stream()
                .map(Task::getEstimatedDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    public void rescheduleTask(int taskId, LocalDate newDate) {
        taskRepository.updateDate(taskId, newDate);
    }

    public long countTotalTasks() {
        return taskRepository.findAll().size();
    }

    public long countCompletedTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
    }

    public Duration getTotalEstimatedTime() {
        return taskRepository.findAll().stream()
                .map(Task::getEstimatedDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    public Duration getTotalActualTime() {
        return taskRepository.findAll().stream()
                .map(Task::getActualDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    public java.util.Map<java.time.LocalDate, Long> getWeeklyProductivity() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate oneWeekAgo = today.minusDays(6);

        java.util.List<it.unicam.cs.mpgc.jtime122631.model.Task> allTasks = taskRepository.findAll();

        java.util.Map<java.time.LocalDate, Long> counts = allTasks.stream()
                .filter(t -> t.getStatus() == it.unicam.cs.mpgc.jtime122631.model.TaskStatus.COMPLETED)
                .filter(t -> t.getScheduledDate() != null)
                .filter(t -> !t.getScheduledDate().isBefore(oneWeekAgo) && !t.getScheduledDate().isAfter(today))
                .collect(java.util.stream.Collectors.groupingBy(
                        it.unicam.cs.mpgc.jtime122631.model.Task::getScheduledDate,
                        java.util.stream.Collectors.counting()
                ));

        java.util.Map<java.time.LocalDate, Long> result = new java.util.TreeMap<>();
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate d = oneWeekAgo.plusDays(i);
            result.put(d, counts.getOrDefault(d, 0L));
        }
        return result;
    }

    public List<InfoTask> getOverdueTasks() {
        return new ArrayList<>(taskRepository.findOverdueTasks(LocalDate.now()));
    }

    public void rescheduleAllToToday(List<InfoTask> tasks) {
        LocalDate today = LocalDate.now();
        for (InfoTask t : tasks) {
            taskRepository.updateDate(t.getId(), today);
        }
    }
}