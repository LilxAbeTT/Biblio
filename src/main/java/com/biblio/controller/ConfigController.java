package com.biblio.controller;

import com.biblio.model.dao.ConfiguracionDAO;
import com.biblio.model.dao.DestinatarioReporteDAO;
import com.biblio.model.entities.DestinatarioReporte;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ConfigController {

    @FXML private TextField duracionField, avisoField;
    @FXML private Label msgTiempos;

    @FXML private TextField smtpHost, smtpPort, smtpUser, smtpFrom;
    @FXML private PasswordField smtpPass;
    @FXML private Label msgSMTP;

    @FXML private TextField emailField;
    @FXML private TableView<DestinatarioReporte> tablaEmails;
    @FXML private TableColumn<DestinatarioReporte, Number> colId;
    @FXML private TableColumn<DestinatarioReporte, String> colEmail;
    @FXML private Label msgEmails;

    private final ConfiguracionDAO configDao = new ConfiguracionDAO();
    private final DestinatarioReporteDAO destDao = new DestinatarioReporteDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        cargarConfig();
        recargarEmails();
    }

    private void cargarConfig() {
        try {
            Map<String,String> m = configDao.getAll();
            duracionField.setText(m.getOrDefault("pc.tiempo_minutos","60"));
            avisoField.setText(m.getOrDefault("pc.aviso_minutos","5"));

            smtpHost.setText(m.getOrDefault("smtp.host",""));
            smtpPort.setText(m.getOrDefault("smtp.port","587"));
            smtpUser.setText(m.getOrDefault("smtp.user",""));
            smtpPass.setText(m.getOrDefault("smtp.pass",""));
            smtpFrom.setText(m.getOrDefault("smtp.from", m.getOrDefault("smtp.user","")));
        } catch (SQLException e) {
            msgSMTP.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void guardarTiempos() {
        try {
            configDao.set("pc.tiempo_minutos", duracionField.getText().trim());
            configDao.set("pc.aviso_minutos", avisoField.getText().trim());
            msgTiempos.setText("Guardado.");
        } catch (SQLException e) {
            msgTiempos.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void guardarSMTP() {
        try {
            configDao.set("smtp.host", smtpHost.getText().trim());
            configDao.set("smtp.port", smtpPort.getText().trim());
            configDao.set("smtp.user", smtpUser.getText().trim());
            configDao.set("smtp.pass", smtpPass.getText().trim());
            configDao.set("smtp.from", smtpFrom.getText().trim());
            msgSMTP.setText("Guardado.");
        } catch (SQLException e) {
            msgSMTP.setText("Error: " + e.getMessage());
        }
    }

    private void recargarEmails() {
        try {
            List<DestinatarioReporte> list = destDao.list();
            tablaEmails.setItems(FXCollections.observableArrayList(list));
            msgEmails.setText(list.size()+" destinatario(s).");
        } catch (SQLException e) {
            msgEmails.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void agregarEmail() {
        try {
            String em = emailField.getText().trim();
            if (em.isEmpty()) { msgEmails.setText("Escribe un email."); return; }
            destDao.add(em);
            emailField.clear();
            recargarEmails();
            msgEmails.setText("Agregado.");
        } catch (Exception e) {
            msgEmails.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarEmail() {
        try {
            DestinatarioReporte sel = tablaEmails.getSelectionModel().getSelectedItem();
            if (sel == null) { msgEmails.setText("Selecciona un email."); return; }
            destDao.remove(sel.getId());
            recargarEmails();
            msgEmails.setText("Eliminado.");
        } catch (Exception e) {
            msgEmails.setText("Error: " + e.getMessage());
        }
    }
}
