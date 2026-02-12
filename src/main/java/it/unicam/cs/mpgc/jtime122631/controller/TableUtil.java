package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.service.TaskService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class TableUtil {

    public static <T> void setupLeftColumn(TableColumn<T, String> column, Callback<T, String> mapper) {
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.call(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else { setText(item); getStyleClass().add("col-left"); }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T, V> void setupBadgeColumn(TableColumn<T, V> column, Callback<T, String> valueMapper, String prefix) {
        column.setCellValueFactory(data -> (ObservableValue<V>) new SimpleStringProperty(valueMapper.call(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(V item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    String text = item.toString();
                    Label badge = new Label(text.toUpperCase());
                    badge.getStyleClass().addAll("badge", prefix + "-" + text.toLowerCase());
                    badge.setAlignment(Pos.CENTER);
                    badge.setMinWidth(95); badge.setPrefWidth(95); badge.setMaxWidth(95);
                    setGraphic(badge); setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public static <T> void setupCenterColumn(TableColumn<T, String> column, Callback<T, String> mapper) {
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.call(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else { setText(item); setAlignment(Pos.CENTER); }
            }
        });
    }

    public static Button createActionButton(String text, String styleClass, Consumer<Void> action) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("table-action-button", styleClass);
        btn.setOnAction(e -> action.accept(null));
        return btn;
    }

    public static HBox createActionContainer(Node... nodes) {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(nodes);
        return container;
    }

    public static String formatDuration(Duration d) {
        if (d == null || d.isZero()) return "0 min";
        long h = d.toHours();
        long m = d.toMinutesPart();
        return h > 0 ? String.format("%dh %dm", h, m) : m + " min";
    }

    public static void handleCompleteTaskAction(InfoTask task, TaskService service, Window owner, Runnable onRefresh) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(task.getEstimatedDuration().toMinutes()));
        dialog.setTitle("Conferma Completamento");
        dialog.setHeaderText("Attività: " + task.getTitle());
        dialog.setContentText("Minuti effettivi impiegati:");
        dialog.initOwner(owner);

        dialog.showAndWait().ifPresent(input -> {
            try {
                long min = Long.parseLong(input.trim());
                if (min < 0) throw new NumberFormatException();
                service.completeTask(task.getId(), Duration.ofMinutes(min));
                onRefresh.run();
            } catch (NumberFormatException e) {
                showSimpleWarning("Valore non valido", "Inserisci un numero intero positivo.", owner);
                handleCompleteTaskAction(task, service, owner, onRefresh);
            }
        });
    }

    public static void showTaskSelectionDialog(List<InfoTask> tasks, Window owner, Consumer<List<InfoTask>> onConfirm) {
        Dialog<List<InfoTask>> dialog = new Dialog<>();
        dialog.setTitle("Gestione Arretrati");
        dialog.initOwner(owner);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog-pane");
        String css = TableUtil.class.getResource("/it/unicam/cs/mpgc/jtime122631/controller/styles.css").toExternalForm();
        dialogPane.getStylesheets().add(css);

        dialog.setHeaderText("Attività in sospeso");

        ButtonType confirmBtnType = new ButtonType("Sposta ora", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtnType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(confirmBtnType, cancelBtnType);

        dialogPane.lookupButton(confirmBtnType).getStyleClass().add("dialog-button-confirm");
        dialogPane.lookupButton(cancelBtnType).getStyleClass().add("dialog-button-cancel");

        Map<InfoTask, SimpleBooleanProperty> selectionMap = new HashMap<>();
        ListView<InfoTask> listView = new ListView<>(FXCollections.observableArrayList(tasks));
        listView.getStyleClass().add("modern-list-view");

        listView.setCellFactory(CheckBoxListCell.forListView(
                task -> selectionMap.computeIfAbsent(task, k -> new SimpleBooleanProperty(true)),
                new StringConverter<InfoTask>() {
                    @Override
                    public String toString(InfoTask t) {
                        return t == null ? "" : t.getTitle().toUpperCase() + " (Scadenza: " + t.getScheduledDate() + ")";
                    }
                    @Override
                    public InfoTask fromString(String s) { return null; }
                }
        ));

        VBox content = new VBox(15);
        Label infoLabel = new Label("Seleziona quali attività vuoi portare alla data di oggi:");
        infoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        content.getChildren().addAll(infoLabel, listView);
        content.setPrefSize(450, 350);
        dialogPane.setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == confirmBtnType) {
                List<InfoTask> selected = new ArrayList<>();
                selectionMap.forEach((t, sel) -> { if (sel.get()) selected.add(t); });
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(onConfirm);
    }

    private static void showSimpleWarning(String header, String content, Window owner) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(header); a.setContentText(content);
        a.initOwner(owner); a.showAndWait();
    }
}