package it.unicam.cs.mpgc.jtime122631;

import it.unicam.cs.mpgc.jtime122631.controller.MainController;
import it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager;
import it.unicam.cs.mpgc.jtime122631.repository.H2ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.H2TaskRepository;
import it.unicam.cs.mpgc.jtime122631.repository.ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.TaskRepository;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.ProjectServiceImpl;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import it.unicam.cs.mpgc.jtime122631.service.TaskServiceImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.initialize();
        ProjectRepository projectRepo = new H2ProjectRepository();
        TaskRepository taskRepo = new H2TaskRepository();

        ProjectService projectService = new ProjectServiceImpl(projectRepo, taskRepo);
        TaskService taskService = new TaskServiceImpl(taskRepo, projectRepo);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicam/cs/mpgc/jtime122631/controller/MainView.fxml"));
        BorderPane root = loader.load();

        MainController mainController = loader.getController();
        mainController.setServices(projectService, taskService);

        primaryStage.setTitle("JTime Manager");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}