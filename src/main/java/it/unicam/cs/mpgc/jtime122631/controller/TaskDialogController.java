package it.unicam.cs.mpgc.jtime122631.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDate;

public class TaskDialogController {

    @FXML private TextField titleField;
    @FXML private TextField estimatedField;
    @FXML private DatePicker datePicker;

    private Stage dialogStage;
    private boolean saveClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTaskData(String title, Duration estimated, LocalDate scheduledDate) {
        this.titleField.setText(title);
        if (estimated != null) {
            this.estimatedField.setText(String.valueOf(estimated.toMinutes()));
        }
        this.datePicker.setValue(scheduledDate);
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
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Campi non validi");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}