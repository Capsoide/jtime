package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.function.Consumer;

public class ViewFactory {

    private static final String VIEW_PATH = "/it/unicam/cs/mpgc/jtime122631/controller/";
    private static final String PROJECTS_VIEW = VIEW_PATH + "ProjectsView.fxml";
    private static final String PLANNING_VIEW = VIEW_PATH + "PlanningView.fxml";
    private static final String REPORTS_VIEW  = VIEW_PATH + "ReportView.fxml";
    private static final String DETAILS_VIEW  = VIEW_PATH + "ProjectDetailsView.fxml";

    private final ProjectService projectService;
    private final TaskService taskService;
    private final StackPane contentArea;

    public ViewFactory(ProjectService projectService, TaskService taskService, StackPane contentArea) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.contentArea = contentArea;
    }

    public void showProjects(MainController mainController) {
        loadView(PROJECTS_VIEW, (ProjectsController controller) -> {
            controller.setProjectService(projectService);
            controller.setMainController(mainController);
        });
    }

    public void showPlanning() {
        loadView(PLANNING_VIEW, (PlanningController controller) -> {
            controller.setTaskService(taskService);
        });
    }

    public void showReports() {
        loadView(REPORTS_VIEW, (ReportController controller) -> {
            controller.setServices(projectService, taskService);
        });
    }

    public void showProjectDetails(InfoProject project, MainController mainController) {
        loadView(DETAILS_VIEW, (ProjectDetailsController controller) -> {
            controller.initData(project, taskService, mainController);
        });
    }

    private <T> void loadView(String fxmlPath, Consumer<T> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            T controller = loader.getController();
            controllerInitializer.accept(controller);
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Errore Critico", "Impossibile caricare la vista: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}