package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class PlanningController {

    @FXML private DatePicker datePicker;
    @FXML private Label totalTimeLabel;

    @FXML private TableView<InfoTask> planningTable;
    @FXML private TableColumn<InfoTask, String> colTitle;
    @FXML private TableColumn<InfoTask, String> colStatus;
    @FXML private TableColumn<InfoTask, String> colEstimated;
    @FXML private TableColumn<InfoTask, String> colActual;
    @FXML private TableColumn<InfoTask, Void> colActions;

    private TaskService taskService;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        setupStatusBadge();
        colEstimated.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstimatedDuration().toMinutes() + " min"));
        colActual.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getActualDuration().toMinutes() + " min"));
        colTitle.getStyleClass().add("col-left");
        colStatus.getStyleClass().add("col-center");
        colEstimated.getStyleClass().add("col-center");
        colActual.getStyleClass().add("col-center");
        colActions.getStyleClass().add("col-center");

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

        var tasks = taskService.getDailyPlan(date);
        planningTable.setItems(FXCollections.observableArrayList(new ArrayList<>(tasks)));

        long remainingMinutes = tasks.stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus().toString()))
                .mapToLong(t -> t.getEstimatedDuration().toMinutes())
                .sum();

        long hours = remainingMinutes / 60;
        long minutes = remainingMinutes % 60;

        if (remainingMinutes == 0 && !tasks.isEmpty()) {
            totalTimeLabel.setText("Task completate!");
            totalTimeLabel.setStyle("-fx-text-fill: #15803d; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            totalTimeLabel.setText(String.format("Da completare: %dh %dm", hours, minutes));

            if (hours < 1) {
                totalTimeLabel.setStyle("-fx-text-fill: #15803d; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else if (hours >= 8) {
                totalTimeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else {
                totalTimeLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
        }
    }

    @FXML private void handlePrevDay() {
        datePicker.setValue(datePicker.getValue().minusDays(1));
    }

    @FXML private void handleNextDay() {
        datePicker.setValue(datePicker.getValue().plusDays(1));
    }

    @FXML private void handleToday() {
        datePicker.setValue(LocalDate.now());
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
                btnComplete.setOnAction(e -> handleComplete(getTableView().getItems().get(getIndex())));

                btnMove.getStyleClass().addAll("table-action-button", "open-button");
                btnMove.setOnAction(e -> handleMove(getTableView().getItems().get(getIndex())));

                btnEdit.getStyleClass().addAll("table-action-button", "edit-button");
                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                btnDelete.getStyleClass().addAll("table-action-button", "delete-button");
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InfoTask t = getTableView().getItems().get(getIndex());
                    if (t != null && "PENDING".equals(t.getStatus().toString())) {
                        pane.getChildren().setAll(btnComplete, btnMove, btnEdit, btnDelete);
                        setGraphic(pane);
                        setAlignment(Pos.CENTER);
                    } else {
                        if (t != null) {
                            pane.getChildren().setAll(btnDelete);
                            setGraphic(pane);
                            setAlignment(Pos.CENTER);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            }
        });
    }

    private void setupStatusBadge() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String style = "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 12;";
                    if ("COMPLETED".equals(item)) style += "-fx-text-fill: #15803d; -fx-background-color: #dcfce7;";
                    else style += "-fx-text-fill: #b45309; -fx-background-color: #fef3c7;";
                    badge.setStyle(style);
                    badge.setAlignment(Pos.CENTER);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void handleComplete(InfoTask task) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(task.getEstimatedDuration().toMinutes()));
        dialog.setTitle("Completa");
        dialog.setHeaderText("Minuti effettivi?");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(minStr -> {
            try {
                taskService.completeTask(task.getId(), Duration.ofMinutes(Long.parseLong(minStr)));
                loadDailyTasks(datePicker.getValue());
            } catch (Exception e) {
            }
        });
    }

    private void handleEdit(InfoTask task) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/TaskDialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifica Attività");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(planningTable.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(page));

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTaskData(task.getTitle(), task.getEstimatedDuration(), task.getScheduledDate());

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                taskService.updateTask(task.getId(), controller.getTitle(), controller.getScheduledDate(), controller.getEstimatedDuration());
                loadDailyTasks(datePicker.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(InfoTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Attività");
        alert.setHeaderText("Eliminare definitivamente '" + task.getTitle() + "'?");
        alert.setContentText("L'operazione non può essere annullata.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskService.deleteTask(task.getId());
            loadDailyTasks(datePicker.getValue());
        }
    }

    private void handleMove(InfoTask task) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Sposta Attività");
        dialog.setHeaderText("Sposta '" + task.getTitle() + "' a nuova data:");

        ButtonType saveButtonType = new ButtonType("Sposta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        DatePicker datePickerDlg = new DatePicker(task.getScheduledDate());
        VBox content = new VBox(10, new Label("Nuova data:"), datePickerDlg);
        content.setPadding(new javafx.geometry.Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return datePickerDlg.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();
        result.ifPresent(newDate -> {
            try {
                taskService.rescheduleTask(task.getId(), newDate);
                loadDailyTasks(datePicker.getValue());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Errore spostamento: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }
}