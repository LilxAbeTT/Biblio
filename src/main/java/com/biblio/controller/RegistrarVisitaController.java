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
import java.util.Set;
import java.util.stream.Collectors;

public class RegistrarVisitaController {

    @FXML private TextField numControlField, nombreField, generoField, carreraField, grupoField, obsField;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private ComboBox<String> pcCombo;
    @FXML private Label altaMsg, regMsg;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();

    @FXML
    public void initialize() {
        loadAvailablePcs();
        pcCombo.setDisable(true);
        servPcCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            pcCombo.setDisable(!newV);
            if (newV) loadAvailablePcs();
        });
    }

    private void loadAvailablePcs() {
        try {
            pcCombo.getItems().clear();
            var pcs = pcDAO.findAllActivas();
            Set<Integer> ocupadas = sesionDAO.activas().stream()
                    .filter(s -> s.getEstado() == EstadoSesionPc.ACTIVA)
                    .map(SesionPc::getPcId)
                    .collect(Collectors.toSet());
            pcs.stream()
                    .filter(pc -> !ocupadas.contains(pc.getId()))
                    .forEach(pc -> pcCombo.getItems().add(pc.getEtiqueta()));
            boolean hayLibres = !pcCombo.getItems().isEmpty();
            servPcCheck.setDisable(!hayLibres);
            if (!hayLibres) {
                servPcCheck.setSelected(false);
                regMsg.setText("No hay PCs disponibles.");
            }
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
