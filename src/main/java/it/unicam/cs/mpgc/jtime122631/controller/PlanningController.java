package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

public class PlanningController {
    @FXML private DatePicker datePicker;
    @FXML private Label totalTimeLabel;
    @FXML private TableView<InfoTask> planningTable;
    @FXML private TableColumn<InfoTask, String> colTitle, colEstimated, colActual;
    @FXML private TableColumn<InfoTask, Object> colPriority, colStatus;
    @FXML private TableColumn<InfoTask, Void> colActions;

    private TaskService taskService;

    @FXML
    public void initialize() {
        TableUtil.setupLeftColumn(colTitle, InfoTask::getTitle);
        TableUtil.setupBadgeColumn(colPriority, t -> t.getPriority().name(), "priority");
        TableUtil.setupBadgeColumn(colStatus, t -> t.getStatus().name(), "status");
        TableUtil.setupCenterColumn(colEstimated, t -> TableUtil.formatDuration(t.getEstimatedDuration()));
        TableUtil.setupCenterColumn(colActual, t -> TableUtil.formatDuration(t.getActualDuration()));
        setupActions();
        datePicker.valueProperty().addListener((obs, old, newD) -> { if (newD != null) loadDailyTasks(newD); });
    }

    public void setTaskService(TaskService service) { this.taskService = service; datePicker.setValue(LocalDate.now()); }

    private void loadDailyTasks(LocalDate date) {
        if (taskService == null) return;
        var tasks = new ArrayList<>(taskService.getDailyPlan(date));
        tasks.sort(Comparator.comparing(InfoTask::getPriority));
        planningTable.setItems(FXCollections.observableArrayList(tasks));
        updateSummaryLabel(taskService.getRemainingMinutesForDate(date), tasks.isEmpty());
    }

    private void updateSummaryLabel(Duration remaining, boolean isEmpty) {
        totalTimeLabel.getStyleClass().removeAll("summary-label-ok", "summary-label-warning", "summary-label-danger");
        if (remaining.isZero() && !isEmpty) {
            totalTimeLabel.setText("Task completate!");
            totalTimeLabel.getStyleClass().add("summary-label-ok");
        } else {
            totalTimeLabel.setText("Da completare: " + TableUtil.formatDuration(remaining));
            totalTimeLabel.getStyleClass().add(remaining.toHours() >= 8 ? "summary-label-danger" : "summary-label-warning");
        }
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    InfoTask t = getTableView().getItems().get(getIndex());
                    Button bDel = TableUtil.createActionButton("ELIMINA", "delete-button", v -> handleDelete(t));
                    if ("COMPLETED".equals(t.getStatus().name())) {
                        setGraphic(TableUtil.createActionContainer(bDel));
                    } else {
                        Button bComp = TableUtil.createActionButton("COMPLETA", "complete-button", v -> handleComplete(t));
                        Button bMove = TableUtil.createActionButton("SPOSTA", "open-button", v -> handleMove(t));
                        Button bEdit = TableUtil.createActionButton("MODIFICA", "edit-button", v -> handleEdit(t)); // Metodo omesso per brevità
                        setGraphic(TableUtil.createActionContainer(bComp, bMove, bEdit, bDel));
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void handleComplete(InfoTask task) {
        TableUtil.handleCompleteTaskAction(task, taskService, planningTable.getScene().getWindow(), () -> loadDailyTasks(datePicker.getValue()));
    }

    private void handleDelete(InfoTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Eliminare '" + task.getTitle() + "'?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.OK) { taskService.deleteTask(task.getId()); loadDailyTasks(datePicker.getValue()); } });
    }
    private void handleMove(InfoTask task) {
        DatePicker dp = new DatePicker(task.getScheduledDate());
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.initOwner(planningTable.getScene().getWindow());
        dialog.setTitle("Sposta Task");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(new VBox(10, new Label("Nuova data:"), dp));
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? dp.getValue() : null);
        dialog.showAndWait().ifPresent(d -> { taskService.rescheduleTask(task.getId(), d); loadDailyTasks(datePicker.getValue()); });
    }
    @FXML private void handlePrevDay() { datePicker.setValue(datePicker.getValue().minusDays(1)); }
    @FXML private void handleNextDay() { datePicker.setValue(datePicker.getValue().plusDays(1)); }
    @FXML private void handleToday() { datePicker.setValue(LocalDate.now()); }
    private void handleEdit(InfoTask task) {}
}