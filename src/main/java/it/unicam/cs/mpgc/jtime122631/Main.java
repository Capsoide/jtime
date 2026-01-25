package it.unicam.cs.mpgc.jtime122631;

import it.unicam.cs.mpgc.jtime122631.infrastructure.DatabaseManager;
import it.unicam.cs.mpgc.jtime122631.repository.H2ProjectRepository;
import it.unicam.cs.mpgc.jtime122631.repository.H2TaskRepository;
import it.unicam.cs.mpgc.jtime122631.service.ProjectService;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import it.unicam.cs.mpgc.jtime122631.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        DatabaseManager.initialize();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            //setup repo e service
            H2ProjectRepository projectRepo = new H2ProjectRepository();
            H2TaskRepository taskRepo = new H2TaskRepository();

            ProjectService projectService = new ProjectService(projectRepo, taskRepo);
            TaskService taskService = new TaskService(taskRepo, projectRepo);

            //caricamento fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("controller/MainView.fxml"));
            Parent root = loader.load();

            //setup controller
            MainController controller = loader.getController();
            controller.setServices(projectService, taskService);

            //setup scena
            Scene scene = new Scene(root);
            primaryStage.setTitle("JTime Project Manager");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento della View.");
        }
    }
}