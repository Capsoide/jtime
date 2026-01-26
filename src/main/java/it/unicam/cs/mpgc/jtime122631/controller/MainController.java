package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;


public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnProjects;
    @FXML private Button btnPlanning;
    @FXML private Button btnReports;

    private ProjectService projectService;
    private TaskService taskService;

    public void setServices(ProjectService pService, TaskService tService) {
        this.projectService = pService;
        this.taskService = tService;
        showReports();
        javafx.application.Platform.runLater(this::checkOverdueTasks);
    }

    private void setActiveButton(Button activeButton) {
        btnProjects.getStyleClass().remove("menu-button-active");
        btnPlanning.getStyleClass().remove("menu-button-active");
        btnReports.getStyleClass().remove("menu-button-active");
        activeButton.getStyleClass().add("menu-button-active");
    }

    @FXML
    public void showProjects() {
        setActiveButton(btnProjects);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/ProjectsView.fxml"));
            Parent view = loader.load();
            ProjectsController controller = loader.getController();

            controller.setProjectService(projectService);
            controller.setMainController(this);
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore caricamento vista Progetti");
        }
    }

    public void showProjectDetails(InfoProject project) {
        setActiveButton(btnProjects);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/ProjectDetailsView.fxml"));
            Parent view = loader.load();

            ProjectDetailsController controller = loader.getController();
            controller.initData(project, taskService, this);

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore caricamento Dettaglio Progetto: " + e.getMessage());
        }
    }

    @FXML
    public void showPlanning() {
        setActiveButton(btnPlanning);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/PlanningView.fxml"));
            Parent view = loader.load();

            PlanningController controller = loader.getController();
            controller.setTaskService(taskService);

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore caricamento Pianificazione: " + e.getMessage());
        }
    }

    @FXML
    public void showReports() {
        setActiveButton(btnReports);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/ReportView.fxml"));
            Parent view = loader.load();

            ReportController controller = loader.getController();
            controller.setServices(projectService, taskService);

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore caricamento Report: " + e.getMessage());
        }
    }

    private void showPlaceholder(String text) {
        contentArea.getChildren().clear();
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #333;");
        contentArea.getChildren().add(label);
    }

    private void showError(String msg) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("ERRORE: " + msg));
    }

    private void checkOverdueTasks() {
        if (taskService == null) return;

        java.util.List<it.unicam.cs.mpgc.jtime122631.model.InfoTask> overdueTasks = taskService.getOverdueTasks();

        if (!overdueTasks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (contentArea.getScene() != null && contentArea.getScene().getWindow() != null) {
                alert.initOwner(contentArea.getScene().getWindow());
            }
            alert.setTitle("Pianificazione");
            alert.setHeaderText("Hai " + overdueTasks.size() + " attività lasciate indietro!");
            alert.setContentText("Vuoi spostarle tutte alla pianificazione di OGGI?");

            ButtonType btnYes = new ButtonType("Sì, sposta a Oggi");
            ButtonType btnNo = new ButtonType("No, lasciale lì");
            alert.getButtonTypes().setAll(btnYes, btnNo);

            java.util.Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnYes) {
                taskService.rescheduleAllToToday(overdueTasks);
                showReports();  //aggiorna i contatori

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                if (contentArea.getScene() != null && contentArea.getScene().getWindow() != null) {
                    success.initOwner(contentArea.getScene().getWindow());
                }
                success.setHeaderText("Aggiornamento completato");
                success.setContentText("Le attività sono ora nella lista di oggi.");
                success.show();
            }
        }
    }

    @FXML
    private void handleVisitWebsite() {
        try {
            String url = "https://github.com/Capsoide";
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}