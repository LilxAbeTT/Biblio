package com.biblio.controller;

import com.biblio.model.dao.SesionPcDAO;
import com.biblio.model.entities.SesionPc;
import com.biblio.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class SesionesController {

    @FXML private TableView<SesionPc> tablaSesiones;
    @FXML private TableColumn<SesionPc, String> colPc, colInicio, colFinPrev, colEstado, colAvisado;
    @FXML private Label sesionMsg;

    private final SesionPcDAO dao = new SesionPcDAO();
    private final SessionManager manager = new SessionManager();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        colPc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("PC-"+c.getValue().getPcId()));
        colInicio.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getInicio().format(fmt)));
        colFinPrev.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFinPrevisto().format(fmt)));
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado().name()));
        colAvisado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isAvisado()?"Sí":"No"));
        recargar();
    }

    private void recargar() {
        try {
            tablaSesiones.getItems().setAll(dao.activas());
        } catch (SQLException e) {
            sesionMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void extender15(){ extender(15); }
    @FXML
    public void extender30(){ extender(30); }

    private void extender(int min) {
        SesionPc s = tablaSesiones.getSelectionModel().getSelectedItem();
        if (s==null) { sesionMsg.setText("Seleccione una sesión."); return; }
        try {
            manager.extender(s, min);
            sesionMsg.setText("Extendida +" + min + " min.");
            recargar();
        } catch (SQLException e) {
            sesionMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void cerrarSeleccionada() {
        SesionPc s = tablaSesiones.getSelectionModel().getSelectedItem();
        if (s==null){ sesionMsg.setText("Seleccione una sesión."); return; }
        try {
            manager.cerrar(s);
            sesionMsg.setText("Sesión cerrada.");
            recargar();
        } catch (SQLException e) {
            sesionMsg.setText("Error: " + e.getMessage());
        }
    }
}
