package it.unicam.cs.mpgc.jtime122631.model;

import java.time.Duration;
import java.time.LocalDate;

public interface InfoTask {
    int getId();
    int getProjectId();
    String getTitle();
    TaskStatus getStatus();
    TaskPriority getPriority();

    Duration getEstimatedDuration();
    Duration getActualDuration();
    LocalDate getScheduledDate();
}