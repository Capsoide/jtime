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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static void main(String[] args) {
        DatabaseManager.initialize();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            //setup dependency injection

            //repository per accesso ai dati
            ProjectRepository projectRepo = new H2ProjectRepository();
            TaskRepository taskRepo = new H2TaskRepository();

            //service per logica di business
            ProjectService projectService = new ProjectServiceImpl(projectRepo, taskRepo);
            TaskService taskService = new TaskServiceImpl(taskRepo, projectRepo);

            //setup UI

            //caricamento fxml
            String fxmlPath = "/it/unicam/cs/mpgc/jtime122631/controller/MainView.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);

            //controllo se il file esista davvero
            if (fxmlUrl == null) {
                throw new IOException("Impossibile trovare il file FXML al percorso: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            //config controller
            MainController controller = loader.getController();
            controller.setServices(projectService, taskService);

            //creazione scena
            Scene scene = new Scene(root);

            //configurazione stage principale
            primaryStage.setTitle("JTime Project Manager");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setMaximized(true); //avvio progetto a schermo intero

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERRORE CRITICO ALL'AVVIO: " + e.getMessage());
        }
    }
}