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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RegistrarVisitaController {

    @FXML private TextField numControlField, nombreField, grupoField, obsField;
    @FXML private ComboBox<String> generoCombo, carreraCombo, pcCombo;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private Label altaMsg, regMsg;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();

    @FXML
    public void initialize() {
        try {
            pcDAO.findAllActivas().forEach(pc -> pcCombo.getItems().add(pc.getEtiqueta()));
        } catch (SQLException e) {
            regMsg.setText("Error cargando PCs: " + e.getMessage());
        }
    }

    @FXML
    public void buscarEstudiante() {
        try {
            Estudiante e = estudianteDAO.findByNumControl(numControlField.getText().trim());
            if (e != null) {
                nombreField.setText(e.getNombre());
                generoCombo.setValue(e.getGenero());
                carreraCombo.setValue(e.getCarrera());
                nombreField.setDisable(true);
                generoCombo.setDisable(true);
                carreraCombo.setDisable(true);
                altaMsg.setText("Estudiante encontrado.");
            } else {
                nombreField.clear();
                generoCombo.getSelectionModel().clearSelection();
                carreraCombo.getSelectionModel().clearSelection();
                nombreField.setDisable(false);
                generoCombo.setDisable(false);
                carreraCombo.setDisable(false);
                altaMsg.setText("Nuevo estudiante.");
            }
        } catch (Exception ex) {
            altaMsg.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void guardarEstudiante() {
        try {
            String nc = numControlField.getText().trim();
            String nombre = nombreField.getText().trim();
            String genero = generoCombo.getValue();
            String carrera = carreraCombo.getValue();

            if (nombre.isEmpty() || genero == null || carrera == null) {
                altaMsg.setText("Capture nombre, género y carrera.");
                return;
            }

            if (estudianteDAO.findByNumControl(nc) != null) {
                altaMsg.setText("Ya existe un estudiante con ese número de control.");
                return;
            }

            Estudiante e = estudianteDAO.upsertRapido(nc, nombre, genero, carrera);
            altaMsg.setText("OK: " + e.getNombre() + " [" + e.getNumControl() + "]");
            buscarEstudiante();
        } catch (Exception e) {
            altaMsg.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
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
