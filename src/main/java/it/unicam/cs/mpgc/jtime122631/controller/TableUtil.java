package it.unicam.cs.mpgc.jtime122631.controller;

import it.unicam.cs.mpgc.jtime122631.model.InfoTask;
import it.unicam.cs.mpgc.jtime122631.model.TaskPriority;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import java.time.Duration;

public class TableUtil {

    public static void setupTitleColumn(TableColumn<InfoTask, String> column) {
        column.setCellValueFactory(new PropertyValueFactory<>("title"));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });
    }

    public static void setupPriorityColumn(TableColumn<InfoTask, TaskPriority> column) {
        column.setCellValueFactory(new PropertyValueFactory<>("priority"));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(TaskPriority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createBadge(item.name(), "priority"));
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public static void setupStatusColumn(TableColumn<InfoTask, String> column) {
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createBadge(item, "status"));
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public static void setupCenterColumn(TableColumn<InfoTask, String> column, Callback<InfoTask, String> mapper) {
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.call(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public static String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) return "0 min";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }
        return minutes + " min";
    }

    /**
     * Crea un badge stilizzato (look a pillola).
     * Modificato per impedire lo stretching orizzontale e verticale.
     */
    private static Label createBadge(String text, String prefix) {
        Label badge = new Label(text.toUpperCase());
        badge.getStyleClass().add("badge");
        badge.getStyleClass().add(prefix + "-" + text.toLowerCase());

        // CORREZIONE: Impedisce al badge di allargarsi a tutta la cella
        // Impostiamo la dimensione massima a quella preferita dal testo + padding
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setMaxWidth(Region.USE_PREF_SIZE);

        // Opzionale: fissa l'altezza per evitare variazioni tra badge diversi
        badge.setMinHeight(Region.USE_PREF_SIZE);
        badge.setMaxHeight(Region.USE_PREF_SIZE);

        badge.setAlignment(Pos.CENTER);
        return badge;
    }
}