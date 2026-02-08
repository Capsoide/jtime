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
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
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
                        container.setAlignment(Pos.CENTER_LEFT);

                        Label flagIcon = new Label("⚑");
                        Label priorityText = new Label(item.name());

                        String color;
                        switch (item) {
                            case URGENTE -> color = "#ef4444"; // Rosso
                            case ALTA    -> color = "#f59e0b"; // Giallo
                            case NORMALE -> color = "#3b82f6"; // Blu
                            case BASSA   -> color = "#94a3b8"; // Grigio
                            default      -> color = "#1e293b";
                        }

                        flagIcon.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                        priorityText.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-transform: uppercase;");

                        container.getChildren().addAll(flagIcon, priorityText);
                        container.setPadding(new Insets(2, 8, 2, 8));
                        container.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 4;");

                        setGraphic(container);
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            });
        }

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
            if (hours < 1) totalTimeLabel.setStyle("-fx-text-fill: #15803d; -fx-font-size: 14px; -fx-font-weight: bold;");
            else if (hours >= 8) totalTimeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-font-weight: bold;");
            else totalTimeLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 14px; -fx-font-weight: bold;");
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
            private final Button btnComplete = new Button("✔");
            private final Button btnMove = new Button("➜");
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("✖");

            private final HBox pane = new HBox(5, btnComplete, btnMove, btnEdit, btnDelete);

            {
                String baseStyle = "-fx-background-color: transparent; -fx-border-radius: 3; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;";
                btnComplete.setStyle(baseStyle + "-fx-text-fill: green;");
                btnMove.setStyle(baseStyle + "-fx-text-fill: #6366f1;");
                btnEdit.setStyle(baseStyle + "-fx-text-fill: #3b82f6;");
                btnDelete.setStyle(baseStyle + "-fx-text-fill: red;");

                btnComplete.setOnAction(e -> handleComplete(getTableView().getItems().get(getIndex())));
                btnMove.setOnAction(e -> handleMove(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
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
                    if (t != null) {
                        if ("PENDING".equals(t.getStatus().toString())) {
                            pane.getChildren().setAll(btnComplete, btnMove, btnEdit, btnDelete);
                        } else {
                            pane.getChildren().setAll(btnDelete);
                        }
                        setGraphic(pane);
                        setAlignment(Pos.CENTER);
                    } else {
                        setGraphic(null);
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
                    setText(null);
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
            } catch (Exception e) {
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
                taskService.updateTask(
                        task.getId(),
                        controller.getTitle(),
                        controller.getScheduledDate(),
                        controller.getEstimatedDuration(),
                        controller.getPriority()
                );
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
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) return datePickerDlg.getValue();
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