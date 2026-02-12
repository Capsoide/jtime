package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import java.awt.Desktop;
import java.net.URI;

public class MainController {

    @FXML private StackPane contentArea;
    private TaskService taskService;
    private ViewFactory viewFactory;

    public void setServices(ProjectService projectService, TaskService taskService) {
        this.taskService = taskService;
        this.viewFactory = new ViewFactory(projectService, taskService, contentArea);

        showReports();
        Platform.runLater(this::checkOverdueTasks);
    }

    private void checkOverdueTasks() {
        if (taskService == null) return;
        var overdue = taskService.getOverdueTasks();
        if (overdue.isEmpty()) return;

        if (contentArea.getScene() == null || contentArea.getScene().getWindow() == null) {
            Platform.runLater(this::checkOverdueTasks);
            return;
        }

        TableUtil.showTaskSelectionDialog(
                overdue,
                contentArea.getScene().getWindow(),
                selected -> {
                    if (selected != null && !selected.isEmpty()) {
                        taskService.rescheduleAllToToday(selected);
                        showReports();
                    }
                }
        );
    }

    @FXML
    public void showProjects() {
        viewFactory.showProjects(this);
    }

    @FXML
    public void showPlanning() {
        viewFactory.showPlanning();
    }

    @FXML
    public void showReports() {
        viewFactory.showReports();
    }

    public void showProjectDetails(InfoProject project) {
        viewFactory.showProjectDetails(project, this);
    }

    @FXML
    public void handleVisitWebsite() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/Capsoide"));
        } catch (Exception e) {
            System.err.println("Impossibile aprire il browser: " + e.getMessage());
        }
    }
}