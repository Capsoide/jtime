package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.TaskPriority;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDate;

public class TaskDialogController {

    @FXML private TextField titleField;
    @FXML private TextField estimatedField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TaskPriority> priorityCombo;

    private Stage dialogStage;
    private boolean saveClicked = false;

    @FXML
    private void initialize() {
        if(priorityCombo != null){
            priorityCombo.setItems(FXCollections.observableArrayList(TaskPriority.values()));
            priorityCombo.setValue(TaskPriority.NORMALE); //imposto di defaul normale
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTaskData(String title, Duration estimated, LocalDate scheduledDate, TaskPriority priority) {
        this.titleField.setText(title);
        if (estimated != null) {
            this.estimatedField.setText(String.valueOf(estimated.toMinutes()));
        }
        this.datePicker.setValue(scheduledDate);
        if (priority != null) {
            this.priorityCombo.setValue(priority);
        }
    }

    public void setTaskData(String title, Duration estimated, LocalDate scheduledDate) {
        setTaskData(title, estimated, scheduledDate, TaskPriority.NORMALE);
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public String getTitle() {
        return titleField.getText();
    }

    public Duration getEstimatedDuration() {
        try {
            long minutes = Long.parseLong(estimatedField.getText());
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }

    public LocalDate getScheduledDate() {
        return datePicker.getValue();
    }

    public TaskPriority getPriority() {
        return priorityCombo.getValue();
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage += "Titolo non valido!\n";
        }

        if (estimatedField.getText() != null && !estimatedField.getText().isEmpty()) {
            try {
                long min = Long.parseLong(estimatedField.getText());
                if (min < 0) errorMessage += "La stima deve essere positiva!\n";
            } catch (NumberFormatException e) {
                errorMessage += "Inserisci un numero valido per i minuti (es. 60)\n";
            }
        }

        if (priorityCombo.getValue() == null) {
            errorMessage += "Seleziona una priorità!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Campi non validi");
            alert.setHeaderText("Per favore, correggi gli errori:");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}