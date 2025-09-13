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

    @FXML private TextField numControlField, nombreField, generoField, carreraField;
    @FXML private TextArea obsField;
    @FXML private CheckBox servPcCheck, consultaRapidaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private ComboBox<String> pcCombo;
    @FXML private ComboBox<Integer> tamEquipoCombo;
    @FXML private VBox integrantesBox;
    @FXML private Label altaMsg, regMsg;
    @FXML private Button registrarBtn, limpiarTodoBtn;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();

    @FXML
    public void initialize() {
        try {
            pcDAO.findAllActivas().forEach(pc -> pcCombo.getItems().add(pc.getEtiqueta()));
            tamEquipoCombo.getItems().addAll(2,3,4,5,6);

            // show team size and integrantes only when working in team
            tamEquipoCombo.visibleProperty().bind(trabajoEquipoCheck.selectedProperty());
            tamEquipoCombo.managedProperty().bind(trabajoEquipoCheck.selectedProperty());
            integrantesBox.visibleProperty().bind(trabajoEquipoCheck.selectedProperty());
            integrantesBox.managedProperty().bind(trabajoEquipoCheck.selectedProperty());

            // disable pc selection when working in team
            servPcCheck.disableProperty().bind(trabajoEquipoCheck.selectedProperty());
            pcCombo.disableProperty().bind(trabajoEquipoCheck.selectedProperty().or(servPcCheck.selectedProperty().not()));
        } catch (SQLException e) {
            regMsg.setText("Error cargando PCs: " + e.getMessage());
        }
    }

    @FXML
    public void altaRapida() {
        try {
            String nc = numControlField.getText().trim();
            String nom = nombreField.getText().trim();
            Estudiante e = estudianteDAO.upsertRapido(nc, nom,
                    generoField.getText().trim(), carreraField.getText().trim());
            altaMsg.setText("OK: " + e.getNombre() + " [" + e.getNumControl() + "]");
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
            r.setServConsultaSala(consultaRapidaCheck.isSelected());
            r.setServLecturaSala(lecturaSalaCheck.isSelected());
            r.setServTrabajoPersonal(trabajoPersonalCheck.isSelected());
            r.setServTrabajoEquipo(trabajoEquipoCheck.isSelected());
            r.setGrupoTamano(trabajoEquipoCheck.isSelected() && tamEquipoCombo.getValue() != null
                    ? tamEquipoCombo.getValue() : null);
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

    @FXML
    public void limpiarFormulario() {
        numControlField.clear();
        nombreField.clear();
        generoField.clear();
        carreraField.clear();
        servPcCheck.setSelected(false);
        consultaRapidaCheck.setSelected(false);
        lecturaSalaCheck.setSelected(false);
        trabajoPersonalCheck.setSelected(false);
        trabajoEquipoCheck.setSelected(false);
        tamEquipoCombo.getSelectionModel().clearSelection();
        pcCombo.getSelectionModel().clearSelection();
        obsField.clear();
        altaMsg.setText("");
        regMsg.setText("");
        integrantesBox.getChildren().clear();
    }
}
