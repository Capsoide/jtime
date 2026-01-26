package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.Project;
import java.util.List;

public interface ProjectService {
    void createProject(String name, String description);
    void updateProject(int id, String name, String description);
    void deleteProject(int projectId);
    void closeProject(int projectId);

    InfoProject getProject(int id);
    List<Project> getAllProjects();

    long countTotalProjects();
    long countActiveProjects();
    long countCompletedProjects();
}