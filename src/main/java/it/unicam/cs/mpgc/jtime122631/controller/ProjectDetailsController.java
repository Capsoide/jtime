package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

public class ProjectDetailsController {

    @FXML private Label projectNameLabel, projectDescLabel;
    @FXML private TableView<InfoTask> tasksTable;
    @FXML private TableColumn<InfoTask, String> colTitle, colEstimated, colActual, colDate;
    @FXML private TableColumn<InfoTask, Object> colStatus, colPriority; // Cambiati in Object per TableUtil
    @FXML private TableColumn<InfoTask, Void> colActions;

    private TaskService taskService;
    private MainController mainController;
    private InfoProject currentProject;

    @FXML
    public void initialize() {
        TableUtil.setupLeftColumn(colTitle, InfoTask::getTitle);
        TableUtil.setupBadgeColumn(colPriority, t -> t.getPriority().name(), "priority");
        TableUtil.setupBadgeColumn(colStatus, t -> t.getStatus().name(), "status");
        TableUtil.setupCenterColumn(colEstimated, t -> TableUtil.formatDuration(t.getEstimatedDuration()));
        TableUtil.setupCenterColumn(colActual, t -> TableUtil.formatDuration(t.getActualDuration()));
        TableUtil.setupCenterColumn(colDate, t -> t.getScheduledDate() != null ? t.getScheduledDate().toString() : "-");
        setupTaskActions();
    }

    public void initData(InfoProject project, TaskService tService, MainController mController) {
        this.currentProject = project; this.taskService = tService; this.mainController = mController;
        projectNameLabel.setText(project.getName()); projectDescLabel.setText(project.getDescription());
        refreshTable();
    }

    private void refreshTable() {
        if (currentProject != null && taskService != null) {
            tasksTable.setItems(FXCollections.observableArrayList(new ArrayList<>(taskService.getTasksByProject(currentProject.getId()))));
        }
    }

    private void setupTaskActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    InfoTask t = getTableView().getItems().get(getIndex());
                    Button bDel = TableUtil.createActionButton("ELIMINA", "delete-button", v -> handleDelete(t));
                    if ("PENDING".equals(t.getStatus().name())) {
                        Button bComp = TableUtil.createActionButton("COMPLETA", "complete-button", v -> handleComplete(t));
                        Button bEdit = TableUtil.createActionButton("MODIFICA", "edit-button", v -> openTaskDialog(t));
                        setGraphic(TableUtil.createActionContainer(bComp, bEdit, bDel));
                    } else { setGraphic(TableUtil.createActionContainer(bDel)); }
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void handleComplete(InfoTask task) {
        TableUtil.handleCompleteTaskAction(task, taskService, tasksTable.getScene().getWindow(), this::refreshTable);
    }

    @FXML public void handleAddTask() { openTaskDialog(null); }
    private void openTaskDialog(InfoTask task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            VBox page = loader.load();
            Stage stage = new Stage();
            stage.setTitle(task == null ? "Nuova Attività" : "Modifica Attività");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tasksTable.getScene().getWindow());
            stage.setScene(new Scene(page));
            TaskDialogController ctrl = loader.getController();
            ctrl.setDialogStage(stage);
            if (task != null) ctrl.setTaskData(task.getTitle(), task.getEstimatedDuration(), task.getScheduledDate(), task.getPriority());
            stage.showAndWait();
            if (ctrl.isSaveClicked()) {
                if (task == null) taskService.createTask(currentProject.getId(), ctrl.getTitle(), ctrl.getScheduledDate(), ctrl.getEstimatedDuration(), ctrl.getPriority());
                else taskService.updateTask(task.getId(), ctrl.getTitle(), ctrl.getScheduledDate(), ctrl.getEstimatedDuration(), ctrl.getPriority());
                refreshTable();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void handleDelete(InfoTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Eliminare '" + task.getTitle() + "'?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.OK) { taskService.deleteTask(task.getId()); refreshTable(); } });
    }
    @FXML public void handleBack() { if (mainController != null) mainController.showProjects(); }
}