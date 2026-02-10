package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.model.TaskPriority;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class PlanningController {

    @FXML private DatePicker datePicker;
    @FXML private Label totalTimeLabel;
    @FXML private TableView<InfoTask> planningTable;
    @FXML private TableColumn<InfoTask, String> colTitle, colStatus, colEstimated, colActual;
    @FXML private TableColumn<InfoTask, TaskPriority> colPriority;
    @FXML private TableColumn<InfoTask, Void> colActions;

    private TaskService taskService;

    @FXML
    public void initialize() {
        TableUtil.setupTitleColumn(colTitle);
        TableUtil.setupPriorityColumn(colPriority);
        TableUtil.setupStatusColumn(colStatus);
        TableUtil.setupCenterColumn(colEstimated, task -> TableUtil.formatDuration(task.getEstimatedDuration()));
        TableUtil.setupCenterColumn(colActual, task -> TableUtil.formatDuration(task.getActualDuration()));
        setupActions();

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) loadDailyTasks(newDate);
        });
    }

    public void setTaskService(TaskService service) {
        this.taskService = service;
        datePicker.setValue(LocalDate.now());
    }

    private void loadDailyTasks(LocalDate date) {
        if (taskService == null) return;

        var tasks = new ArrayList<>(taskService.getDailyPlan(date));
        tasks.sort(Comparator.comparing(InfoTask::getPriority));
        planningTable.setItems(FXCollections.observableArrayList(tasks));
        Duration remaining = taskService.getRemainingMinutesForDate(date);
        updateSummaryLabel(remaining, tasks.isEmpty());
    }

    private void updateSummaryLabel(Duration remaining, boolean isEmpty) {
        if (remaining.isZero() && !isEmpty) {
            totalTimeLabel.setText("Task completate!");
            totalTimeLabel.setStyle("-fx-text-fill: #15803d; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            String timeStr = TableUtil.formatDuration(remaining);
            totalTimeLabel.setText("Da completare: " + timeStr);

            boolean isOverloaded = remaining.toHours() >= 8;
            totalTimeLabel.setStyle("-fx-text-fill: " + (isOverloaded ? "#dc2626" : "#d97706") + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnComplete = new Button("COMPLETA");
            private final Button btnMove = new Button("SPOSTA");
            private final Button btnEdit = new Button("MODIFICA");
            private final Button btnDelete = new Button("ELIMINA");
            private final HBox pane = new HBox(8, btnComplete, btnMove, btnEdit, btnDelete);

            {
                btnComplete.getStyleClass().addAll("table-action-button", "complete-button");
                btnMove.getStyleClass().addAll("table-action-button", "open-button");
                btnEdit.getStyleClass().addAll("table-action-button", "edit-button");
                btnDelete.getStyleClass().addAll("table-action-button", "delete-button");

                pane.setAlignment(Pos.CENTER);

                btnComplete.setOnAction(e -> handleComplete(getTableView().getItems().get(getIndex())));
                btnMove.setOnAction(e -> handleMove(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InfoTask t = getTableView().getItems().get(getIndex());
                    if (t != null) {
                        if ("COMPLETED".equals(t.getStatus().name())) {
                            pane.getChildren().setAll(btnDelete);
                        } else {
                            pane.getChildren().setAll(btnComplete, btnMove, btnEdit, btnDelete);
                        }
                        setGraphic(pane);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });
    }

    private void handleComplete(InfoTask task) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(task.getEstimatedDuration().toMinutes()));
        dialog.setTitle("Completa");
        dialog.setHeaderText("Hai completato: " + task.getTitle());
        dialog.setContentText("Minuti effettivi impiegati:");
        dialog.initOwner(planningTable.getScene().getWindow());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(minStr -> {
            try {
                long actualMinutes = Long.parseLong(minStr);
                taskService.completeTask(task.getId(), Duration.ofMinutes(actualMinutes));
                loadDailyTasks(datePicker.getValue());
            } catch (NumberFormatException e) {
                showWarning("Valore non valido", "Inserisci un numero intero per i minuti.");
            }
        });
    }

    private void handleEdit(InfoTask task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            VBox page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifica Attività");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(planningTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTaskData(task.getTitle(), task.getEstimatedDuration(), task.getScheduledDate(), task.getPriority());

            dialogStage.showAndWait();
            if (controller.isSaveClicked()) {
                taskService.updateTask(task.getId(), controller.getTitle(), controller.getScheduledDate(), controller.getEstimatedDuration(), controller.getPriority());
                loadDailyTasks(datePicker.getValue());
            }
        } catch (IOException e) {
            showError("Errore", "Impossibile caricare il dialogo di modifica.");
        }
    }

    private void handleDelete(InfoTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(planningTable.getScene().getWindow());
        alert.setTitle("Elimina");
        alert.setHeaderText("Eliminare l'attività '" + task.getTitle() + "'?");
        alert.setContentText("L'azione è irreversibile.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                taskService.deleteTask(task.getId());
                loadDailyTasks(datePicker.getValue());
            }
        });
    }

    private void handleMove(InfoTask task) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.initOwner(planningTable.getScene().getWindow());
        dialog.setTitle("Sposta");
        dialog.setHeaderText("Sposta '" + task.getTitle() + "' a una nuova data:");

        ButtonType saveBtnType = new ButtonType("Sposta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        DatePicker dp = new DatePicker(task.getScheduledDate());
        VBox content = new VBox(10, new Label("Nuova data pianificata:"), dp);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == saveBtnType ? dp.getValue() : null);
        dialog.showAndWait().ifPresent(newDate -> {
            taskService.rescheduleTask(task.getId(), newDate);
            loadDailyTasks(datePicker.getValue());
        });
    }

    @FXML private void handlePrevDay() { datePicker.setValue(datePicker.getValue().minusDays(1)); }
    @FXML private void handleNextDay() { datePicker.setValue(datePicker.getValue().plusDays(1)); }
    @FXML private void handleToday() { datePicker.setValue(LocalDate.now()); }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(planningTable.getScene().getWindow());
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(planningTable.getScene().getWindow());
        alert.setTitle("Attenzione");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}