package com.biblio.controller;

import com.biblio.model.dao.PcDAO;
import com.biblio.model.entities.Pc;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class PCsController {

    @FXML private TextField etiquetaField;
    @FXML private TableView<Pc> tabla;
    @FXML private TableColumn<Pc, Number> colId;
    @FXML private TableColumn<Pc, String> colEtiqueta;
    @FXML private TableColumn<Pc, String> colActivo;
    @FXML private Label msgLabel;

    private final PcDAO dao = new PcDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colEtiqueta.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEtiqueta()));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isActivo() ? "Sí" : "No"));
        recargar();
    }

    private void recargar() {
        try {
            var cn = com.biblio.db.Database.get();
            try (var ps = cn.prepareStatement("SELECT id, etiqueta, activo FROM Pc ORDER BY etiqueta");
                 var rs = ps.executeQuery()) {
                List<Pc> list = new java.util.ArrayList<>();
                while (rs.next()) list.add(new Pc(rs.getInt(1), rs.getString(2), rs.getInt(3)==1));
                tabla.setItems(FXCollections.observableArrayList(list));
            }
        } catch (SQLException e) {
            msgLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void agregar() {
        try {
            String et = etiquetaField.getText().trim();
            if (et.isEmpty()) { msgLabel.setText("Etiqueta requerida."); return; }
            var cn = com.biblio.db.Database.get();
            try (var ps = cn.prepareStatement("INSERT INTO Pc(etiqueta, activo) VALUES(?,1)")) {
                ps.setString(1, et);
                ps.executeUpdate();
                cn.commit();
            }
            etiquetaField.clear();
            msgLabel.setText("PC agregada.");
            recargar();
        } catch (Exception e) {
            msgLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void activar() { setActivo(true); }

    @FXML
    public void desactivar() { setActivo(false); }

    private void setActivo(boolean activo) {
        try {
            Pc sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { msgLabel.setText("Seleccione una PC."); return; }
            var cn = com.biblio.db.Database.get();
            try (var ps = cn.prepareStatement("UPDATE Pc SET activo=? WHERE id=?")) {
                ps.setInt(1, activo ? 1 : 0);
                ps.setInt(2, sel.getId());
                ps.executeUpdate();
                cn.commit();
            }
            msgLabel.setText(activo ? "PC activada." : "PC desactivada.");
            recargar();
        } catch (Exception e) {
            msgLabel.setText("Error: " + e.getMessage());
        }
    }
}
