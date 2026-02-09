package it.unicam.cs.mpgc.jtime122631.controller;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class PlanningController {

    @FXML private DatePicker datePicker;
    @FXML private Label totalTimeLabel;
    @FXML private TableView<InfoTask> planningTable;
    @FXML private TableColumn<InfoTask, String> colTitle;
    @FXML private TableColumn<InfoTask, String> colStatus;
    @FXML private TableColumn<InfoTask, TaskPriority> colPriority;
    @FXML private TableColumn<InfoTask, String> colEstimated;
    @FXML private TableColumn<InfoTask, String> colActual;
    @FXML private TableColumn<InfoTask, Void> colActions;

    private TaskService taskService;

    @FXML
    public void initialize() {
        // 1. Allineamento Titolo (Sinistra con Padding)
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item);
                    setAlignment(Pos.CENTER_LEFT);
                    setPadding(new Insets(0, 0, 0, 10));
                }
            }
        });

        // 2. Priorità (Badge Centrato)
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
                        container.setAlignment(Pos.CENTER); // Centra contenuto nel badge

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
                        setAlignment(Pos.CENTER); // Centra il badge nella cella
                    }
                }
            });
        }

        // 3. Stato (Badge Centrato)
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        setupStatusBadge();

        // 4. Colonne Dati (Centrate)
        setupCenterColumn(colEstimated, task -> task.getEstimatedDuration().toMinutes() + " min");
        setupCenterColumn(colActual, task -> task.getActualDuration().toMinutes() + " min");

        // 5. Azioni (Bottoni Testuali e Centrati)
        setupActions();

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) loadDailyTasks(newDate);
        });
    }

    private void setupCenterColumn(TableColumn<InfoTask, String> column, Callback<InfoTask, String> mapper) {
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.call(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
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
        updateSummaryLabel(tasks);
    }

    private void updateSummaryLabel(ArrayList<InfoTask> tasks) {
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
            totalTimeLabel.setStyle("-fx-text-fill: " + (hours >= 8 ? "#ef4444" : "#d97706") + "; -fx-font-size: 14px; -fx-font-weight: bold;");
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
                btnMove.getStyleClass().addAll("table-action-button", "open-button"); // Usiamo open-button per coerenza colore
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
                        if ("PENDING".equals(t.getStatus().toString())) {
                            pane.getChildren().setAll(btnComplete, btnMove, btnEdit, btnDelete);
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
        dialog.setHeaderText("Minuti effettivi impiegati?");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(minStr -> {
            try {
                taskService.completeTask(task.getId(), Duration.ofMinutes(Long.parseLong(minStr)));
                loadDailyTasks(datePicker.getValue());
            } catch (Exception e) {}
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDelete(InfoTask task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Eliminare definitivamente '" + task.getTitle() + "'?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                taskService.deleteTask(task.getId());
                loadDailyTasks(datePicker.getValue());
            }
        });
    }

    private void handleMove(InfoTask task) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Sposta");
        dialog.setHeaderText("Sposta '" + task.getTitle() + "' a:");
        ButtonType saveBtn = new ButtonType("Sposta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        DatePicker dp = new DatePicker(task.getScheduledDate());
        dialog.getDialogPane().setContent(new VBox(10, new Label("Nuova data:"), dp));
        dialog.setResultConverter(btn -> btn == saveBtn ? dp.getValue() : null);
        dialog.showAndWait().ifPresent(newDate -> {
            taskService.rescheduleTask(task.getId(), newDate);
            loadDailyTasks(datePicker.getValue());
        });
    }

    @FXML private void handlePrevDay() { datePicker.setValue(datePicker.getValue().minusDays(1)); }
    @FXML private void handleNextDay() { datePicker.setValue(datePicker.getValue().plusDays(1)); }
    @FXML private void handleToday() { datePicker.setValue(LocalDate.now()); }
}