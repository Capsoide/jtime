package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.ProjectStatus;
import it.unicam.cs.mpgc.jtime122631.service.JTimeException;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Optional;

public class ProjectsController {

    @FXML private TableView<InfoProject> projectsTable;
    @FXML private TableColumn<InfoProject, String> colName;
    @FXML private TableColumn<InfoProject, String> colDescription;
    @FXML private TableColumn<InfoProject, String> colStatus;
    @FXML private TableColumn<InfoProject, Void> colActions;

    private ProjectService projectService;
    private MainController mainController;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colName.getStyleClass().add("col-center");
        colDescription.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        colActions.getStyleClass().add("col-center");
        setupStatusBadge();
        setupActionButtons();

        projectsTable.setRowFactory(tv -> {
            TableRow<InfoProject> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openProject(row.getItem());
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
        Callback<TableColumn<InfoProject, Void>, TableCell<InfoProject, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnComplete = new Button("COMPLETA");
            private final Button btnOpen = new Button("APRI");
            private final Button btnEdit = new Button("MODIFICA");
            private final Button btnDelete = new Button("ELIMINA");
            private final HBox pane = new HBox(8, btnComplete, btnOpen, btnEdit, btnDelete);

            {
                btnComplete.getStyleClass().addAll("table-action-button", "complete-button");
                btnOpen.getStyleClass().addAll("table-action-button", "open-button");
                btnEdit.getStyleClass().addAll("table-action-button", "edit-button");
                btnDelete.getStyleClass().addAll("table-action-button", "delete-button");
                btnComplete.setTooltip(new Tooltip("Segna progetto come completato"));
                pane.setAlignment(Pos.CENTER);
                btnComplete.setOnAction(e -> handleCloseProject(getTableView().getItems().get(getIndex())));
                btnOpen.setOnAction(e -> openProject(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> handleEditProject(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDeleteProject(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InfoProject p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        if (p.getStatus() == ProjectStatus.COMPLETED) {
                            pane.getChildren().setAll(btnOpen, btnDelete);
                        } else {
                            pane.getChildren().setAll(btnComplete, btnOpen, btnEdit, btnDelete);
                        }
                        setGraphic(pane);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        };

        colActions.setCellFactory(cellFactory);
    }

    private void handleCloseProject(InfoProject project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chiudi Progetto");
        alert.setHeaderText("Vuoi segnare '" + project.getName() + "' come COMPLETATO?");
        alert.setContentText("Potrai farlo solo se tutte le attività sono terminate.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                projectService.closeProject(project.getId());
                refreshTable();
            } catch (JTimeException e) {
                Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                errorAlert.setTitle("Impossibile Chiudere");
                errorAlert.setHeaderText("Attività in corso");
                errorAlert.setContentText(e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void handleEditProject(InfoProject project) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/ProjectDialog.fxml"));
            javafx.scene.layout.VBox page = loader.load();
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Modifica Progetto");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(projectsTable.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(page));
            ProjectDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProjectData(project.getName(), project.getDescription());
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                projectService.updateProject(project.getId(), controller.getName(), controller.getDescription());
                refreshTable();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteProject(InfoProject project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Eliminazione");
        alert.setHeaderText("Eliminare il progetto '" + project.getName() + "'?");
        alert.setContentText("Attenzione: verranno eliminati anche tutti i task associati.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            projectService.deleteProject(project.getId());
            refreshTable();
        }
    }

    private void openProject(InfoProject project) {
        if (mainController != null) {
            mainController.showProjectDetails(project);
        }
    }

    private void setupStatusBadge() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String style = "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 12;";
                    if (item.equals("ACTIVE")) {
                        style += "-fx-text-fill: #15803d; -fx-background-color: #dcfce7;";
                    } else {
                        style += "-fx-text-fill: #1e293b; -fx-background-color: #e2e8f0;";
                    }
                    badge.setStyle(style);

                    badge.setAlignment(Pos.CENTER);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void refreshTable() {
        if (projectService != null) {
            projectsTable.setItems(FXCollections.observableArrayList(new ArrayList<>(projectService.getAllProjects())));
        }
    }

    @FXML
    public void handleAddProject() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/ProjectDialog.fxml"));
            javafx.scene.layout.VBox page = loader.load();
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Nuovo Progetto");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(projectsTable.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(page));
            ProjectDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.isSaveClicked()) {
                projectService.createProject(controller.getName(), controller.getDescription());
                refreshTable();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}