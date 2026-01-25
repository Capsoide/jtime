package it.unicam.cs.mpgc.jtime122631.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

public class Task implements InfoTask {
    private final int id;
    private final int projectId;
    private final String title;
    private final TaskStatus status;
    private final Duration estimatedDuration;
    private final Duration actualDuration;
    private final LocalDate scheduledDate;

    public Task(int projectId, String title) {
        this(0, projectId, title, TaskStatus.PENDING, Duration.ZERO, Duration.ZERO, null);
    }

    public Task(int id, int projectId, String title, TaskStatus status,
                Duration estimated, Duration actual, LocalDate scheduledDate) {
        this.id = id;
        this.projectId = projectId;
        this.title = Objects.requireNonNull(title, "Il titolo e' obbligatorio");
        this.status = status != null ? status : TaskStatus.PENDING;
        this.estimatedDuration = estimated != null ? estimated : Duration.ZERO;
        this.actualDuration = actual != null ? actual : Duration.ZERO;
        this.scheduledDate = scheduledDate;
    }

    @Override public int getId() { return id; }
    @Override public int getProjectId() { return projectId; }
    @Override public String getTitle() { return title; }
    @Override public TaskStatus getStatus() { return status; }
    @Override public Duration getEstimatedDuration() { return estimatedDuration; }
    @Override public Duration getActualDuration() { return actualDuration; }
    @Override public LocalDate getScheduledDate() { return scheduledDate; }

    @Override
    public String toString() {
        return title;
    }
}