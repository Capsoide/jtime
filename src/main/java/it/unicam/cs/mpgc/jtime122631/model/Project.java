package it.unicam.cs.mpgc.jtime122631.model;

import java.util.Objects;

public class Project implements InfoProject {
    private final int id;
    private final String name;
    private final String description;
    private final ProjectStatus status;

    public Project(String name, String description) {
        this(0, name, description, ProjectStatus.ACTIVE);
    }

    public Project(int id, String name, String description, ProjectStatus status) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Il nome del progetto e' obbligatorio");
        this.description = description != null ? description : "";
        this.status = status != null ? status : ProjectStatus.ACTIVE;
    }

    @Override public int getId() { return id; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public ProjectStatus getStatus() { return status; }

    @Override
    public String toString() {
        return name;
    }
}