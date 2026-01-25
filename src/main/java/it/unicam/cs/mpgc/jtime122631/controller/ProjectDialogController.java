package it.unicam.cs.mpgc.jtime122631.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProjectDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;

    private Stage dialogStage;
    private boolean saveClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProjectData(String name, String description) {
        this.nameField.setText(name);
        this.descriptionArea.setText(description);
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public String getName() {
        return nameField.getText();
    }

    public String getDescription() {
        return descriptionArea.getText();
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
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Nome progetto non valido!\n";
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