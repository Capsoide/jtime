package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.*;
import it.unicam.cs.mpgc.jtime122631.repository.ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.TaskRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskServiceImpl(TaskRepository taskRepo, ProjectRepository projectRepo) {
        this.taskRepository = taskRepo;
        this.projectRepository = projectRepo;
    }

    @Override
    public void createTask(int projectId, String title, LocalDate scheduledDate, Duration estimated, TaskPriority priority) {
        Project project = projectRepository.findById(projectId);
        if (project == null) throw new JTimeException("Progetto non trovato.");
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new JTimeException("Non puoi aggiungere attività a un progetto chiuso.");
        }
        TaskPriority safePriority = (priority != null) ? priority : TaskPriority.NORMALE;
        Task newTask = new Task(0, projectId, title, TaskStatus.PENDING, safePriority, estimated, Duration.ZERO, scheduledDate);
        taskRepository.save(newTask);
    }

    @Override
    public void updateTask(int id, String title, LocalDate scheduledDate, Duration estimated, TaskPriority priority) {
        Task existing = taskRepository.findById(id);
        if (existing == null) throw new JTimeException("Task non trovato.");
        TaskPriority safePriority = (priority != null) ? priority : TaskPriority.NORMALE;
        Task updatedTask = new Task(existing.getId(), existing.getProjectId(), title, existing.getStatus(),
                safePriority, estimated != null ? estimated : Duration.ZERO, existing.getActualDuration(), scheduledDate);
        taskRepository.save(updatedTask);
    }

    @Override
    public void completeTask(int taskId, Duration actualDuration) {
        Task existingTask = taskRepository.findById(taskId);
        if (existingTask == null) throw new JTimeException("Task non trovato.");
        Task completedTask = new Task(existingTask.getId(), existingTask.getProjectId(), existingTask.getTitle(),
                TaskStatus.COMPLETED, existingTask.getPriority(), existingTask.getEstimatedDuration(), actualDuration, existingTask.getScheduledDate());
        taskRepository.save(completedTask);
    }

    @Override
    public void deleteTask(int taskId) { taskRepository.deleteById(taskId); }

    @Override
    public List<InfoTask> getTasksByProject(int projectId) {
        return taskRepository.findByProject(projectId).stream().map(t -> (InfoTask) t).collect(Collectors.toList());
    }

    @Override
    public List<InfoTask> getDailyPlan(LocalDate date) {
        return taskRepository.findByDate(date).stream().map(t -> (InfoTask) t).collect(Collectors.toList());
    }

    @Override
    public Duration getRemainingMinutesForDate(LocalDate date) {
        return taskRepository.findByDate(date).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .map(Task::getEstimatedDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Map<TaskPriority, Long> getPendingTasksByPriority() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    @Override
    public void rescheduleTask(int taskId, LocalDate newDate) { taskRepository.updateDate(taskId, newDate); }

    @Override
    public long countTotalTasks() { return taskRepository.findAll().size(); }

    @Override
    public long countCompletedTasks() {
        return taskRepository.findAll().stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
    }

    @Override
    public Duration getTotalEstimatedTime() {
        return taskRepository.findAll().stream().map(Task::getEstimatedDuration).reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Duration getTotalActualTime() {
        return taskRepository.findAll().stream().map(Task::getActualDuration).reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Map<LocalDate, Long> getWeeklyProductivity() {
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(6);
        Map<LocalDate, Long> counts = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getScheduledDate() != null)
                .filter(t -> !t.getScheduledDate().isBefore(oneWeekAgo) && !t.getScheduledDate().isAfter(today))
                .collect(Collectors.groupingBy(Task::getScheduledDate, Collectors.counting()));
        Map<LocalDate, Long> result = new TreeMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = oneWeekAgo.plusDays(i);
            result.put(d, counts.getOrDefault(d, 0L));
        }
        return result;
    }

    @Override
    public List<InfoTask> getOverdueTasks() { return new ArrayList<>(taskRepository.findOverdueTasks(LocalDate.now())); }

    @Override
    public void rescheduleAllToToday(List<InfoTask> tasks) {
        LocalDate today = LocalDate.now();
        for (InfoTask t : tasks) { taskRepository.updateDate(t.getId(), today); }
    }
}