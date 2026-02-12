package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.TaskPriority;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Map;

public class ReportController {
    @FXML private VBox cardOverdue;
    @FXML private Label lblOverdue, lblPendingTasks, lblCompletedTasks, lblTotalActual;
    @FXML private PieChart projectPieChart;
    @FXML private BarChart<String, Number> priorityBarChart;
    @FXML private NumberAxis priorityYAxis;

    private ProjectService projectService;
    private TaskService taskService;

    public void setServices(ProjectService pService, TaskService tService) {
        this.projectService = pService;
        this.taskService = tService;
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (projectService == null || taskService == null) return;

        lblOverdue.setText(String.valueOf(taskService.getOverdueTasks().size()));
        lblPendingTasks.setText(String.valueOf(taskService.countTotalTasks() - taskService.countCompletedTasks()));
        lblCompletedTasks.setText(String.valueOf(taskService.countCompletedTasks()));
        lblTotalActual.setText(TableUtil.formatDuration(taskService.getTotalActualTime()));

        cardOverdue.getStyleClass().remove("card-alarm");
        if (taskService.getOverdueTasks().size() > 0) cardOverdue.getStyleClass().add("card-alarm");

        double compPerc = projectService.getCompletionPercentage();
        projectPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data(String.format("Completati (%.0f%%)", compPerc), projectService.countCompletedProjects()),
                new PieChart.Data(String.format("Attivi (%.0f%%)", 100 - compPerc), projectService.countActiveProjects())
        ));

        setupIntegerAxis();

        Map<TaskPriority, Long> stats = taskService.getPendingTasksByPriority();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Priorità");

        addPriorityBar(series, stats, TaskPriority.URGENTE, "#ef4444");
        addPriorityBar(series, stats, TaskPriority.ALTA, "#f59e0b");
        addPriorityBar(series, stats, TaskPriority.NORMALE, "#3b82f6");
        addPriorityBar(series, stats, TaskPriority.BASSA, "#94a3b8");

        priorityBarChart.getData().setAll(series);
    }

    @FXML
    private void handleViewOverdueTasks() {
        if (taskService == null) return;

        var overdue = taskService.getOverdueTasks();

        if (overdue.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ottimo lavoro! Non ci sono attività arretrate al momento.");
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.initOwner(cardOverdue.getScene().getWindow());
            alert.showAndWait();
            return;
        }

        TableUtil.showTaskSelectionDialog(
                overdue,
                cardOverdue.getScene().getWindow(),
                selected -> {
                    if (selected != null && !selected.isEmpty()) {
                        taskService.rescheduleAllToToday(selected);
                        loadDashboardData();
                    }
                }
        );
    }

    private void setupIntegerAxis() {
        priorityYAxis.setTickUnit(1);
        priorityYAxis.setMinorTickVisible(false);
        priorityYAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override public String toString(Number n) { return n.doubleValue() == n.intValue() ? String.valueOf(n.intValue()) : ""; }
            @Override public Number fromString(String s) { return Integer.parseInt(s); }
        });
    }

    private void addPriorityBar(XYChart.Series<String, Number> series, Map<TaskPriority, Long> stats, TaskPriority p, String color) {
        XYChart.Data<String, Number> data = new XYChart.Data<>(p.name(), stats.getOrDefault(p, 0L));
        data.nodeProperty().addListener((obs, old, newNode) -> { if (newNode != null) newNode.setStyle("-fx-bar-fill: " + color + ";"); });
        series.getData().add(data);
    }
}