package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.*;
import it.unicam.cs.mpgc.jtime122631.repository.ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.TaskRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
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
        if (project.getStatus() == ProjectStatus.COMPLETED) throw new JTimeException("Progetto già chiuso.");

        taskRepository.save(new Task(0, projectId, title, TaskStatus.PENDING,
                priority != null ? priority : TaskPriority.NORMALE, estimated, Duration.ZERO, scheduledDate));
    }

    @Override
    public void updateTask(int id, String title, LocalDate scheduledDate, Duration estimated, TaskPriority priority) {
        Task existing = taskRepository.findById(id);
        if (existing == null) throw new JTimeException("Task non trovato.");

        taskRepository.save(new Task(existing.getId(), existing.getProjectId(), title, existing.getStatus(),
                priority, estimated, existing.getActualDuration(), scheduledDate));
    }

    @Override
    public void completeTask(int taskId, Duration actualDuration) {
        Task t = taskRepository.findById(taskId);
        if (t == null) throw new JTimeException("Task non trovato.");

        taskRepository.save(new Task(t.getId(), t.getProjectId(), t.getTitle(), TaskStatus.COMPLETED,
                t.getPriority(), t.getEstimatedDuration(), actualDuration, t.getScheduledDate()));
    }

    @Override public void deleteTask(int taskId) { taskRepository.deleteById(taskId); }
    @Override public void rescheduleTask(int taskId, LocalDate newDate) { taskRepository.updateDate(taskId, newDate); }

    @Override public List<InfoTask> getTasksByProject(int projectId) {
        return taskRepository.findByProject(projectId).stream().map(t -> (InfoTask) t).collect(Collectors.toList());
    }

    @Override public List<InfoTask> getDailyPlan(LocalDate date) {
        return taskRepository.findByDate(date).stream().map(t -> (InfoTask) t).collect(Collectors.toList());
    }

    @Override public List<InfoTask> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now()).stream().map(t -> (InfoTask) t).collect(Collectors.toList());
    }

    @Override public void rescheduleAllToToday(List<InfoTask> tasks) {
        LocalDate today = LocalDate.now();
        tasks.forEach(t -> taskRepository.updateDate(t.getId(), today));
    }

    @Override public long countTotalTasks() { return taskRepository.findAll().size(); }
    @Override public long countCompletedTasks() {
        return taskRepository.findAll().stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
    }
    @Override public Duration getTotalActualTime() {
        return taskRepository.findAll().stream().map(Task::getActualDuration).reduce(Duration.ZERO, Duration::plus);
    }
    @Override public Duration getRemainingMinutesForDate(LocalDate date) {
        return taskRepository.findByDate(date).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .map(Task::getEstimatedDuration).reduce(Duration.ZERO, Duration::plus);
    }
    @Override public Map<TaskPriority, Long> getPendingTasksByPriority() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }
    @Override public Map<LocalDate, Long> getWeeklyProductivity() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> result = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long count = taskRepository.findByDate(d).stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            result.put(d, count);
        }
        return result;
    }
}