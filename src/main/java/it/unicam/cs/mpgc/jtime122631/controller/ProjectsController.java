package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.ProjectStatus;
import it.unicam.cs.mpgc.jtime122631.service.JTimeException;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProjectsController {

    @FXML private TableView<InfoProject> projectsTable;
    @FXML private TableColumn<InfoProject, String> colName, colDescription;
    @FXML private TableColumn<InfoProject, Object> colStatus;
    @FXML private TableColumn<InfoProject, Void> colActions;

    private ProjectService projectService;
    private MainController mainController;

    @FXML
    public void initialize() {
        TableUtil.setupLeftColumn(colName, InfoProject::getName);
        TableUtil.setupLeftColumn(colDescription, InfoProject::getDescription);
        TableUtil.setupBadgeColumn(colStatus, p -> p.getStatus().name(), "status");

        setupActionButtons();

        projectsTable.setRowFactory(tv -> {
            TableRow<InfoProject> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    if (mainController != null) {
                        mainController.showProjectDetails(row.getItem());
                    }
                }
            });
            return row;
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setProjectService(ProjectService service) {
        this.projectService = service;
        refreshTable();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InfoProject p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        Button bOpen = TableUtil.createActionButton("APRI", "open-button", v -> mainController.showProjectDetails(p));
                        Button bDel = TableUtil.createActionButton("ELIMINA", "delete-button", v -> handleDeleteProject(p));
                        Button bReport = TableUtil.createActionButton("REPORT", "report-button", v -> handleGenerateReport(p));

                        if (p.getStatus() == ProjectStatus.COMPLETED) {
                            setGraphic(TableUtil.createActionContainer(bOpen, bReport, bDel));
                        } else {
                            Button bComp = TableUtil.createActionButton("COMPLETA", "complete-button", v -> handleCloseProject(p));
                            Button bEdit = TableUtil.createActionButton("MODIFICA", "edit-button", v -> openProjectDialog(p));
                            setGraphic(TableUtil.createActionContainer(bComp, bOpen, bEdit, bReport, bDel));
                        }
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });
    }

    private void handleGenerateReport(InfoProject project) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Report Progetto");
        fileChooser.setInitialFileName("Report_" + project.getName().replace(" ", "_") + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File di testo", "*.txt"));

        File file = fileChooser.showSaveDialog(projectsTable.getScene().getWindow());
        if (file != null) {
            try {
                projectService.generateReport(project.getId(), file);
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Report generato con successo!");
                success.initOwner(projectsTable.getScene().getWindow());
                success.showAndWait();
            } catch (JTimeException e) {
                showWarning("Errore Report", e.getMessage());
            }
        }
    }

    private void handleCloseProject(InfoProject project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "L'azione è irreversibile.", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Chiudi Progetto");
        alert.setHeaderText("Segnare '" + project.getName() + "' come COMPLETATO?");
        alert.initOwner(projectsTable.getScene().getWindow());

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    projectService.closeProject(project.getId());
                    refreshTable();
                } catch (JTimeException e) {
                    showWarning("Errore", e.getMessage());
                }
            }
        });
    }

    private void openProjectDialog(InfoProject project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/ProjectDialog.fxml"));
            VBox page = loader.load();
            Stage stage = new Stage();
            stage.setTitle(project == null ? "Nuovo Progetto" : "Modifica Progetto");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(projectsTable.getScene().getWindow());
            stage.setScene(new Scene(page));

            ProjectDialogController ctrl = loader.getController();
            ctrl.setDialogStage(stage);
            if (project != null) ctrl.setProjectData(project.getName(), project.getDescription());

            stage.showAndWait();
            if (ctrl.isSaveClicked()) {
                if (project == null) projectService.createProject(ctrl.getName(), ctrl.getDescription());
                else projectService.updateProject(project.getId(), ctrl.getName(), ctrl.getDescription());
                refreshTable();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDeleteProject(InfoProject project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tutti i task verranno eliminati.", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Elimina");
        alert.setHeaderText("Eliminare il progetto '" + project.getName() + "'?");
        alert.initOwner(projectsTable.getScene().getWindow());

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                projectService.deleteProject(project.getId());
                refreshTable();
            }
        });
    }

    @FXML public void handleAddProject() { openProjectDialog(null); }

    private void refreshTable() {
        if (projectService != null) {
            projectsTable.setItems(FXCollections.observableArrayList(new ArrayList<>(projectService.getAllProjects())));
        }
    }

    private void showWarning(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING, content);
        a.setTitle("Attenzione");
        a.setHeaderText(header);
        a.initOwner(projectsTable.getScene().getWindow());
        a.showAndWait();
    }
}