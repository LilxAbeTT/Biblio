package com.biblio.controller;

import com.biblio.model.dao.EstudianteDAO;
import com.biblio.model.entities.Estudiante;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class EstudiantesController {

    @FXML private TextField buscarField, numControlField, nombreField, generoField, carreraField;
    @FXML private TableView<Estudiante> tabla;
    @FXML private TableColumn<Estudiante, Number> colId;
    @FXML private TableColumn<Estudiante, String> colNum, colNom, colGen, colCar;
    @FXML private Label msgLabel;

    private final EstudianteDAO dao = new EstudianteDAO();
    private Estudiante seleccionado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colNum.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumControl()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colGen.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGenero()));
        colCar.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCarrera()));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            seleccionado = n;
            if (n != null) {
                numControlField.setText(n.getNumControl());
                nombreField.setText(n.getNombre());
                generoField.setText(n.getGenero());
                carreraField.setText(n.getCarrera());
            }
        });
        buscar();
    }

    @FXML
    public void buscar() {
        try {
            String term = buscarField.getText() == null ? "" : buscarField.getText().trim();
            List<Estudiante> list = dao.search(term);
            tabla.setItems(FXCollections.observableArrayList(list));
            msgLabel.setText(list.size() + " resultado(s).");
        } catch (SQLException e) {
            msgLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void nuevo() {
        seleccionado = null;
        numControlField.clear();
        nombreField.clear();
        generoField.clear();
        carreraField.clear();
        msgLabel.setText("Nuevo registro.");
    }

    @FXML
    public void guardar() {
        try {
            Estudiante e = dao.upsertRapido(
                    numControlField.getText().trim(),
                    nombreField.getText().trim(),
                    generoField.getText().trim(),
                    carreraField.getText().trim());
            msgLabel.setText("Guardado: " + e.getNombre());
            buscar();
        } catch (Exception e) {
            msgLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void eliminar() {
        try {
            Estudiante sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { msgLabel.setText("Seleccione un estudiante."); return; }
            // eliminación simple: usar numControl único
            var cn = com.biblio.db.Database.get();
            try (var ps = cn.prepareStatement("DELETE FROM Estudiante WHERE id=?")) {
                ps.setInt(1, sel.getId());
                ps.executeUpdate();
                cn.commit();
            }
            msgLabel.setText("Eliminado.");
            buscar();
        } catch (Exception e) {
            msgLabel.setText("Error: " + e.getMessage());
        }
    }
}
