package com.biblio.controller;

import com.biblio.model.dao.EstudianteDAO;
import com.biblio.model.dao.PcDAO;
import com.biblio.model.dao.RegistroVisitaDAO;
import com.biblio.model.dao.SesionPcDAO;
import com.biblio.model.entities.Estudiante;
import com.biblio.model.entities.RegistroVisita;
import com.biblio.model.entities.SesionPc;
import com.biblio.model.enums.EstadoSesionPc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RegistrarVisitaController {

    @FXML private TextField numControlField, nombreField, grupoField, obsField;
    @FXML private ComboBox<String> generoCombo, carreraCombo, pcCombo;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private ComboBox<String> generoCombo, carreraCombo, pcCombo;
    @FXML private VBox datosEstudianteBox;
    @FXML private Button guardarBtn;
    @FXML private Label altaMsg, regMsg;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();

    @FXML
    public void initialize() {
        generoCombo.getItems().setAll("Hombre", "Mujer", "Otro");
        carreraCombo.getItems().setAll(
                "Ing. Administración", "Gastronomía", "Contador Público",
                "Ing. Civil", "Arquitectura", "Lic. En Turismo",
                "Ing. Electromecánica", "Ing. En Sistemas Computacionales");
        try {
            pcDAO.findAllActivas().forEach(pc -> pcCombo.getItems().add(pc.getEtiqueta()));
        } catch (SQLException e) {
            regMsg.setText("Error cargando PCs: " + e.getMessage());
        }
        numControlField.setOnAction(e -> buscarEstudiante());
        generoCombo.getItems().addAll("M", "F");
        carreraCombo.setEditable(true);
    }

    @FXML
    public void buscarEstudiante() {
        try {
            Estudiante e = estudianteDAO.findByNumControl(numControlField.getText().trim());
            if (e != null) {
                nombreField.setText(e.getNombre());
                generoCombo.setValue(e.getGenero());
                carreraCombo.setValue(e.getCarrera());
                generoCombo.setDisable(true);
                carreraCombo.setDisable(true);
            } else {
                nombreField.clear();
                generoCombo.getSelectionModel().clearSelection();
                carreraCombo.getSelectionModel().clearSelection();
                generoCombo.setDisable(false);
                carreraCombo.setDisable(false);
            }
        } catch (SQLException ex) {
            altaMsg.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    public void guardarEstudiante() {
        try {
            String nc = numControlField.getText().trim();
            String nom = nombreField.getText().trim();
            Estudiante e = estudianteDAO.upsertRapido(nc, nom,
                    generoCombo.getValue(), carreraCombo.getValue());
            altaMsg.setText("OK: " + e.getNombre() + " [" + e.getNumControl() + "]");
            generoCombo.setDisable(true);
            carreraCombo.setDisable(true);
        String nc = numControlField.getText().trim();
        if (nc.isBlank()) {
            altaMsg.setText("Ingrese número de control.");
            return;
        }
        try {
            Estudiante e = estudianteDAO.findByNumControl(nc);
            datosEstudianteBox.setVisible(true);
            datosEstudianteBox.setManaged(true);
            if (e != null) {
                nombreField.setText(e.getNombre());
                generoCombo.setValue(e.getGenero());
                carreraCombo.setValue(e.getCarrera());
                nombreField.setDisable(true);
                generoCombo.setDisable(true);
                carreraCombo.setDisable(true);
                numControlField.setDisable(true);
                guardarBtn.setVisible(false);
                altaMsg.setText("");
            } else {
                nombreField.clear();
                generoCombo.getSelectionModel().clearSelection();
                carreraCombo.getSelectionModel().clearSelection();
                nombreField.setDisable(false);
                generoCombo.setDisable(false);
                carreraCombo.setDisable(false);
                numControlField.setDisable(false);
                guardarBtn.setVisible(true);
                altaMsg.setText("Alumno no existe. Completa datos para dar de alta.");
            }
        } catch (SQLException e) {
            altaMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void guardarEstudiante() {
        try {
            Estudiante e = estudianteDAO.upsertRapido(
                    numControlField.getText().trim(),
                    nombreField.getText().trim(),
                    generoCombo.getValue(),
                    carreraCombo.getValue());
            altaMsg.setText("OK: " + e.getNombre() + " [" + e.getNumControl() + "]");
            nombreField.setDisable(true);
            generoCombo.setDisable(true);
            carreraCombo.setDisable(true);
            numControlField.setDisable(true);
            guardarBtn.setVisible(false);
        } catch (Exception e) {
            altaMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void limpiarBusqueda() {
        numControlField.clear();
        nombreField.clear();
        generoCombo.getSelectionModel().clearSelection();
        carreraCombo.getSelectionModel().clearSelection();
        nombreField.setDisable(false);
        generoCombo.setDisable(false);
        carreraCombo.setDisable(false);
        numControlField.setDisable(false);
        guardarBtn.setVisible(true);
        altaMsg.setText("");
        datosEstudianteBox.setVisible(false);
        datosEstudianteBox.setManaged(false);
    }

    @FXML
    public void registrarVisita() {
        try {
            Estudiante e = estudianteDAO.findByNumControl(numControlField.getText().trim());
            if (e == null) {
                regMsg.setText("Primero da de alta al estudiante.");
                return;
            }
            RegistroVisita r = new RegistroVisita();
            r.setFecha(LocalDate.now());
            r.setTurno("MATUTINO"); // simple: podrías derivarlo por hora
            r.setEstudianteId(e.getId());
            r.setServPc(servPcCheck.isSelected());
            r.setServConsultaSala(consultaSalaCheck.isSelected());
            r.setServLecturaSala(lecturaSalaCheck.isSelected());
            r.setServTrabajoPersonal(trabajoPersonalCheck.isSelected());
            r.setServTrabajoEquipo(trabajoEquipoCheck.isSelected());
            r.setGrupoTamano(trabajoEquipoCheck.isSelected() && !grupoField.getText().isBlank()
                    ? Integer.parseInt(grupoField.getText().trim()) : null);
            r.setPcId(servPcCheck.isSelected() && pcCombo.getValue()!=null
                    ? pcDAO.findByEtiqueta(pcCombo.getValue()).getId() : null);
            r.setHoraEntrada(LocalTime.now());
            r.setObservacion(obsField.getText());

            int idVisita = visitaDAO.crear(r);

            if (r.isServPc() && r.getPcId()!=null) {
                // Crear sesión con duración por defecto (60 mins)
                int dur = 60;
                LocalDateTime inicio = LocalDateTime.now();
                SesionPc s = new SesionPc();
                s.setRegistroId(idVisita);
                s.setPcId(r.getPcId());
                s.setInicio(inicio);
                s.setDuracionMin(dur);
                s.setFinPrevisto(inicio.plusMinutes(dur));
                s.setEstado(EstadoSesionPc.ACTIVA);
                s.setAvisado(false);
                sesionDAO.crear(s);
            }
            regMsg.setText("Visita registrada (#" + idVisita + ")");
        } catch (Exception e) {
            regMsg.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
