package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.Project;
import it.unicam.cs.mpgc.jtime122631.model.ProjectStatus;
import it.unicam.cs.mpgc.jtime122631.model.TaskStatus;
import it.unicam.cs.mpgc.jtime122631.repository.ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.TaskRepository;

import java.util.List;

public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectServiceImpl(ProjectRepository projectRepo, TaskRepository taskRepo) {
        this.projectRepository = projectRepo;
        this.taskRepository = taskRepo;
    }

    @Override
    public void createProject(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new JTimeException("Il nome del progetto non può essere vuoto.");
        }
        projectRepository.save(new Project(name, description));
    }

    @Override
    public void updateProject(int id, String name, String description) {
        Project existing = projectRepository.findById(id);
        if (existing == null) throw new JTimeException("Progetto non trovato.");

        projectRepository.save(new Project(existing.getId(), name, description, existing.getStatus()));
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public InfoProject getProject(int id) {
        Project p = projectRepository.findById(id);
        if (p == null) throw new JTimeException("Progetto non trovato.");
        return p;
    }

    @Override
    public void closeProject(int projectId) {
        Project project = projectRepository.findById(projectId);
        if (project == null) throw new JTimeException("Progetto non trovato.");

        boolean hasPendingTasks = taskRepository.findByProject(projectId).stream()
                .anyMatch(t -> t.getStatus() != TaskStatus.COMPLETED);

        if (hasPendingTasks) {
            throw new JTimeException("Impossibile chiudere il progetto: completare prima tutte le attività.");
        }

        projectRepository.save(new Project(project.getId(), project.getName(), project.getDescription(), ProjectStatus.COMPLETED));
    }

    @Override
    public void deleteProject(int projectId) {
        projectRepository.deleteById(projectId);
    }

    @Override
    public long countActiveProjects() {
        return projectRepository.findAll().stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
    }

    @Override
    public long countCompletedProjects() {
        return projectRepository.findAll().stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
    }

    @Override
    public double getCompletionPercentage() {
        List<Project> all = projectRepository.findAll();
        if (all.isEmpty()) return 0.0;
        long completed = all.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        return (double) completed * 100 / all.size();
    }

    @Override
    public void generateReport(int projectId, java.io.File file) {
        InfoProject project = getProject(projectId);
        var tasks = taskRepository.findByProject(projectId);

        ProjectReportExporter.exportToText(project, tasks, file);
    }
}