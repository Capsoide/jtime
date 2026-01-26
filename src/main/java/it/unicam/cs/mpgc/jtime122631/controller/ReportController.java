package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.util.List;

public class ReportController {
    @FXML private VBox cardOverdue;
    @FXML private Label lblOverdue;
    @FXML private Label lblPendingTasks;
    @FXML private Label lblCompletedTasks;
    @FXML private Label lblTotalActual;
    @FXML private PieChart projectPieChart;
    @FXML private BarChart<String, Number> taskBarChart;

    private ProjectService projectService;
    private TaskService taskService;

    public void setServices(ProjectService pService, TaskService tService) {
        this.projectService = pService;
        this.taskService = tService;
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (projectService == null || taskService == null) return;

        long activeProj = projectService.countActiveProjects();
        long completedProj = projectService.countCompletedProjects();
        long totalProj = activeProj + completedProj;
        long completedTasks = taskService.countCompletedTasks();
        long totalTasks = taskService.countTotalTasks();
        long pendingTasks = totalTasks - completedTasks;

        Duration totalAct = taskService.getTotalActualTime();

        List<InfoTask> overdueList = taskService.getOverdueTasks();
        int overdueCount = overdueList.size();

        lblOverdue.setText(String.valueOf(overdueCount));
        cardOverdue.getStyleClass().remove("card-alarm");

        if (overdueCount > 0) {
            cardOverdue.getStyleClass().add("card-alarm");
            lblOverdue.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 900;");
        } else {
            lblOverdue.setStyle("-fx-text-fill: #1e293b;");
        }

        lblPendingTasks.setText(String.valueOf(pendingTasks));
        lblCompletedTasks.setText(String.valueOf(completedTasks));
        lblTotalActual.setText(formatDuration(totalAct));

        String labelActive = String.format("Attivi (%d%%)", totalProj > 0 ? (activeProj * 100 / totalProj) : 0);
        String labelCompleted = String.format("Completati (%d%%)", totalProj > 0 ? (completedProj * 100 / totalProj) : 0);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data(labelActive, activeProj),
                new PieChart.Data(labelCompleted, completedProj)
        );
        projectPieChart.setData(pieData);
        projectPieChart.setLegendVisible(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks");

        XYChart.Data<String, Number> dataPending = new XYChart.Data<>("Da Fare", pendingTasks);
        XYChart.Data<String, Number> dataCompleted = new XYChart.Data<>("Completati", completedTasks);

        dataPending.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) newNode.setStyle("-fx-bar-fill: #dc2626;");
        });

        dataCompleted.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) newNode.setStyle("-fx-bar-fill: #10b981;");
        });

        series.getData().addAll(dataPending, dataCompleted);

        taskBarChart.getData().clear();
        taskBarChart.getData().add(series);
        taskBarChart.setLegendVisible(false);
        taskBarChart.setCategoryGap(50);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "0h";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }
}