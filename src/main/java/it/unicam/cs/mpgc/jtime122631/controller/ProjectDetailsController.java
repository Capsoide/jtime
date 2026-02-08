package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.model.TaskPriority;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    @FXML private TableColumn<InfoTask, TaskPriority> colPriority;

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
        colTitle.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER_LEFT);
                    setPadding(new Insets(0, 0, 0, 10));
                }
            }
        });

        if (colPriority != null) {
            colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
            colPriority.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(TaskPriority item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        HBox container = new HBox(6);
                        container.setAlignment(Pos.CENTER);

                        Label flagIcon = new Label("⚑");
                        Label priorityText = new Label(item.name());

                        String color;
                        switch (item) {
                            case URGENTE -> color = "#ef4444";
                            case ALTA    -> color = "#f59e0b";
                            case NORMALE -> color = "#3b82f6";
                            case BASSA   -> color = "#94a3b8";
                            default      -> color = "#1e293b";
                        }

                        flagIcon.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 15px; -fx-font-weight: bold;");
                        priorityText.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
                        container.getChildren().addAll(flagIcon, priorityText);
                        container.setPadding(new Insets(3, 10, 3, 10));
                        container.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 4;");
                        setGraphic(container);
                        setAlignment(Pos.CENTER);
                    }
                }
            });
        }

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        setupStatusBadge();
        setupCenterColumn(colEstimated, task -> formatDuration(task.getEstimatedDuration()));
        setupCenterColumn(colActual, task -> formatDuration(task.getActualDuration()));
        setupCenterColumn(colDate, task -> task.getScheduledDate() != null ? task.getScheduledDate().toString() : "-");
        setupTaskActions();
    }

    private String formatDuration(Duration d) {
        if (d == null) return "0 min";
        return d.toMinutes() + " min";
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

    public void setServices(InfoProject project, TaskService tService, MainController mController) {
        this.currentProject = project;
        this.taskService = tService;
        this.mainController = mController;
        projectNameLabel.setText(project.getName());
        projectDescLabel.setText(project.getDescription());
        refreshTable();
    }

    public void initData(InfoProject project, TaskService tService, MainController mController) {
        setServices(project, tService, mController);
    }

    private void refreshTable() {
        if (currentProject != null && taskService != null) {
            tasksTable.setItems(FXCollections.observableArrayList(
                    new ArrayList<>(taskService.getTasksByProject(currentProject.getId()))
            ));
        }
    }

    @FXML
    public void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova Attività");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                taskService.createTask(
                        currentProject.getId(),
                        controller.getTitle(),
                        controller.getScheduledDate(),
                        controller.getEstimatedDuration(),
                        controller.getPriority()
                );
                refreshTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEditTask(InfoTask task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifica Attività");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTaskData(task.getTitle(), task.getEstimatedDuration(), task.getScheduledDate(), task.getPriority());

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                taskService.updateTask(
                        task.getId(),
                        controller.getTitle(),
                        controller.getScheduledDate(),
                        controller.getEstimatedDuration(),
                        controller.getPriority()
                );
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
        if (mainController != null) {
            mainController.showProjects();
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

    private void setupTaskActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnComplete = new Button("COMPLETA");
            private final Button btnEdit = new Button("MODIFICA");
            private final Button btnDelete = new Button("ELIMINA");
            private final HBox pane = new HBox(8, btnComplete, btnEdit, btnDelete);

            {
                // Ripristino classi CSS originali
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
        });
    }
}