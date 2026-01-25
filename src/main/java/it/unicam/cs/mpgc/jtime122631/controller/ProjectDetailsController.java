package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.geometry.Pos;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

public class ProjectDetailsController {

    @FXML private Label projectNameLabel;
    @FXML private Label projectDescLabel;

    @FXML private TableView<InfoTask> tasksTable;
    @FXML private TableColumn<InfoTask, String> colTitle;
    @FXML private TableColumn<InfoTask, String> colStatus;
    @FXML private TableColumn<InfoTask, String> colEstimated;
    @FXML private TableColumn<InfoTask, String> colActual;
    @FXML private TableColumn<InfoTask, String> colDate;
    @FXML private TableColumn<InfoTask, Void> colActions;

    private TaskService taskService;
    private MainController mainController;
    private InfoProject currentProject;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        colEstimated.getStyleClass().add("col-center");
        colActual.getStyleClass().add("col-center");
        colDate.getStyleClass().add("col-center");
        colActions.getStyleClass().add("col-center");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        setupStatusBadge();
        setupCenterColumn(colEstimated, task -> task.getEstimatedDuration().toMinutes() + " min");
        setupCenterColumn(colActual, task -> task.getActualDuration().toMinutes() + " min");
        setupCenterColumn(colDate, task -> task.getScheduledDate() != null ? task.getScheduledDate().toString() : "-");

        setupTaskActions();
    }

    private void setupCenterColumn(TableColumn<InfoTask, String> column, Callback<InfoTask, String> mapper) {
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.call(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public void initData(InfoProject project, TaskService tService, MainController mController) {
        this.currentProject = project;
        this.taskService = tService;
        this.mainController = mController;
        projectNameLabel.setText(project.getName());
        projectDescLabel.setText(project.getDescription());
        refreshTable();
    }

    private void refreshTable() {
        if (currentProject != null && taskService != null) {
            tasksTable.setItems(FXCollections.observableArrayList(
                    new ArrayList<>(taskService.getTasksByProject(currentProject.getId()))
            ));
        }
    }

    private void setupTaskActions() {
        Callback<TableColumn<InfoTask, Void>, TableCell<InfoTask, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnComplete = new Button("COMPLETA");
            private final Button btnEdit = new Button("MODIFICA");
            private final Button btnDelete = new Button("ELIMINA");
            private final HBox pane = new HBox(8, btnComplete, btnEdit, btnDelete);

            {
                btnComplete.getStyleClass().addAll("table-action-button", "complete-button");
                btnEdit.getStyleClass().addAll("table-action-button", "edit-button");
                btnDelete.getStyleClass().addAll("table-action-button", "delete-button");
                pane.setAlignment(Pos.CENTER);
                btnComplete.setOnAction(e -> handleCompleteTask(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> handleEditTask(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDeleteTask(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InfoTask task = getTableView().getItems().get(getIndex());
                    if (task != null) {
                        if ("PENDING".equals(task.getStatus().toString())) {
                            pane.getChildren().setAll(btnComplete, btnEdit, btnDelete);
                        } else {
                            pane.getChildren().setAll(btnDelete);
                        }
                        setGraphic(pane);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void handleEditTask(InfoTask task) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            javafx.scene.layout.VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifica Attività");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksTable.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(page));

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTaskData(task.getTitle(), task.getEstimatedDuration(), task.getScheduledDate());

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                taskService.updateTask(task.getId(), controller.getTitle(), controller.getScheduledDate(), controller.getEstimatedDuration());
                refreshTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteTask(InfoTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Attività");
        alert.setHeaderText("Eliminare l'attività '" + task.getTitle() + "'?");
        alert.setContentText("Questa azione è irreversibile.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskService.deleteTask(task.getId());
            refreshTable();
        }
    }

    private void handleCompleteTask(InfoTask task) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(task.getEstimatedDuration().toMinutes()));
        dialog.setTitle("Completa Attività");
        dialog.setHeaderText("Hai completato: " + task.getTitle());
        dialog.setContentText("Minuti effettivi impiegati:");
        dialog.initOwner(tasksTable.getScene().getWindow());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(minutesString -> {
            try {
                long actualMinutes = Long.parseLong(minutesString);
                taskService.completeTask(task.getId(), Duration.ofMinutes(actualMinutes));
                refreshTable();
            } catch (NumberFormatException e) {
            }
        });
    }

    @FXML
    public void handleBack() {
        mainController.showProjects();
    }

    @FXML
    public void handleAddTask() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            javafx.scene.layout.VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova Attività");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksTable.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(page));

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                taskService.createTask(currentProject.getId(), controller.getTitle(), controller.getScheduledDate(), controller.getEstimatedDuration());
                refreshTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                    if (item.equals("COMPLETED")) style += "-fx-text-fill: #15803d; -fx-background-color: #dcfce7;";
                    else style += "-fx-text-fill: #b45309; -fx-background-color: #fef3c7;";
                    badge.setStyle(style);
                    badge.setAlignment(Pos.CENTER);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }
}