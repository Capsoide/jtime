package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnProjects;
    @FXML private Button btnPlanning;
    @FXML private Button btnReports;

    private ProjectService projectService;
    private TaskService taskService;
    private ViewFactory viewFactory;

    public void setServices(ProjectService pService, TaskService tService) {
        this.projectService = pService;
        this.taskService = tService;
        this.viewFactory = new ViewFactory(pService, tService, contentArea);

        showReports();
        Platform.runLater(this::checkOverdueTasks);
    }

    @FXML
    public void showProjects() {
        setActiveButton(btnProjects);
        if (viewFactory != null) {
            viewFactory.showProjects(this);
        }
    }

    @FXML
    public void showPlanning() {
        setActiveButton(btnPlanning);
        if (viewFactory != null) {
            viewFactory.showPlanning();
        }
    }

    @FXML
    public void showReports() {
        setActiveButton(btnReports);
        if (viewFactory != null) {
            viewFactory.showReports();
        }
    }

    public void showProjectDetails(InfoProject project) {
        setActiveButton(btnProjects);
        if (viewFactory != null) {
            viewFactory.showProjectDetails(project, this);
        }
    }


    private void setActiveButton(Button activeButton) {
        btnProjects.getStyleClass().remove("menu-button-active");
        btnPlanning.getStyleClass().remove("menu-button-active");
        btnReports.getStyleClass().remove("menu-button-active");

        activeButton.getStyleClass().add("menu-button-active");
    }

    @FXML
    private void handleVisitWebsite() {
        try {
            String url = "https://github.com/Capsoide";
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkOverdueTasks() {
        if (taskService == null) return;

        List<InfoTask> overdueTasks = taskService.getOverdueTasks();

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

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnYes) {
                taskService.rescheduleAllToToday(overdueTasks);
                showReports();

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
}