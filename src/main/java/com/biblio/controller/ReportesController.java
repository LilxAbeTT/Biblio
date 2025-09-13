package com.biblio.controller;

import com.biblio.model.dao.DestinatarioReporteDAO;
import com.biblio.model.dao.RegistroVisitaDAO;
import com.biblio.model.dao.SesionPcDAO;
import com.biblio.service.EmailService;
import com.biblio.service.ReportService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportesController {
    @FXML private DatePicker desdePicker, hastaPicker;
    @FXML private TableView<List<String>> tablaReporte;
    @FXML private Label repMsg;

    private final ReportService reportService = new ReportService();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();
    private final DestinatarioReporteDAO destDAO = new DestinatarioReporteDAO();
    private final EmailService emailService = new EmailService();

    private List<String[]> currentRows = new ArrayList<>();

    @FXML
    public void initialize() {
        desdePicker.setValue(LocalDate.now().minusDays(6));
        hastaPicker.setValue(LocalDate.now());
        tablaReporte.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private LocalDate d(){ return desdePicker.getValue(); }
    private LocalDate h(){ return hastaPicker.getValue(); }

    @FXML
    public void generarTotales() {
        try {
            currentRows = Arrays.asList(visitaDAO.totalesPorServicio(d(), h()));
            pintarTabla(currentRows);
            repMsg.setText("Totales generados.");
        } catch (Exception e) {
            repMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void generarOcupacion() {
        try {
            currentRows = Arrays.asList(sesionDAO.ocupacionPC(d(), h()));
            pintarTabla(currentRows);
            repMsg.setText("Ocupación generada.");
        } catch (Exception e) {
            repMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void generarPromedios() {
        try {
            currentRows = Arrays.asList(visitaDAO.promediosPorServicio(d(), h()));
            pintarTabla(currentRows);
            repMsg.setText("Promedios generados.");
        } catch (Exception e) {
            repMsg.setText("Error: " + e.getMessage());
        }
    }

    private void pintarTabla(List<String[]> rows) {
        tablaReporte.getColumns().clear();
        if (rows.isEmpty()) return;
        String[] headers = rows.get(0);
        for (int i=0;i<headers.length;i++){
            final int idx = i;
            TableColumn<List<String>, String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().get(idx)));
            tablaReporte.getColumns().add(col);
        }
        List<List<String>> data = new ArrayList<>();
        for (int r=1;r<rows.size();r++){
            data.add(List.of(rows.get(r)));
        }
        tablaReporte.setItems(FXCollections.observableArrayList(data));
    }

    @FXML
    public void exportarCsv() {
        try {
            Path p = reportService.exportCSV("reporte.csv", currentRows);
            repMsg.setText("CSV: " + p.toAbsolutePath());
        } catch (Exception e) {
            repMsg.setText("Error CSV: " + e.getMessage());
        }
    }

    @FXML
    public void exportarExcel() {
        try {
            Path p = reportService.exportExcel("reporte.xlsx", currentRows);
            repMsg.setText("Excel: " + p.toAbsolutePath());
        } catch (Exception e) {
            repMsg.setText("Error Excel: " + e.getMessage());
        }
    }

    @FXML
    public void exportarPdf() {
        try {
            Path p = reportService.exportPDF("reporte.pdf", "Reporte Biblio", currentRows);
            repMsg.setText("PDF: " + p.toAbsolutePath());
        } catch (Exception e) {
            repMsg.setText("Error PDF: " + e.getMessage());
        }
    }

    @FXML
    public void enviarCorreo() {
        try {
            var dests = destDAO.list().stream().map(d -> d.getEmail()).toArray(String[]::new);
            if (dests.length == 0) { repMsg.setText("No hay destinatarios configurados."); return; }
            StringBuilder cuerpo = new StringBuilder("Reporte Biblio (")
                    .append(d()).append(" a ").append(h()).append(")\n\n");
            for (String[] row : currentRows) {
                cuerpo.append(String.join(" | ", row)).append("\n");
            }
            emailService.enviar("Reporte Biblio", cuerpo.toString(), dests);
            repMsg.setText("Enviado a " + dests.length + " destinatario(s).");
        } catch (Exception e) {
            repMsg.setText("Error envío: " + e.getMessage());
        }
    }
}
