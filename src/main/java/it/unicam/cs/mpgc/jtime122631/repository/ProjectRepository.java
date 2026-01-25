package it.unicam.cs.mpgc.jtime122631.repository;

import it.unicam.cs.mpgc.jtime122631.model.Project;
import java.util.List;

public interface ProjectRepository {
    Project save(Project project);
    Project findById(int id);
    List<Project> findAll();
    void deleteById(int id);
    boolean existsById(int id);
}