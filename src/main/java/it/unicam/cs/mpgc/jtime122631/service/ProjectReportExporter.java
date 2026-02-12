package it.unicam.cs.mpgc.jtime122631.service;

import it.unicam.cs.mpgc.jtime122631.model.InfoProject;
import it.unicam.cs.mpgc.jtime122631.model.Task;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class ProjectReportExporter {
    public static void exportToText(InfoProject project, List<Task> tasks, File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("REPORT PROGETTO: " + project.getName().toUpperCase());
            writer.println("Stato: " + project.getStatus());
            writer.println("Descrizione: " + project.getDescription());
            writer.println("------------------------------------------------------------");
            writer.println(String.format("%-30s | %-15s | %-10s ", "ATTIVITÀ", "PRIORITÀ", "STATO"));
            writer.println("------------------------------------------------------------");

            for (Task t : tasks) {
                writer.println(String.format("%-30s | %-15s | %-10s",
                        t.getTitle(), t.getPriority(), t.getStatus()));
            }
        } catch (Exception e) {
            throw new JTimeException("Errore durante la generazione del file: " + e.getMessage());
        }
    }
}