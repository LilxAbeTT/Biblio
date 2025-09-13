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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RegistrarVisitaController {

    @FXML private TextField numControlField, nombreField, generoField, carreraField, obsField;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private ComboBox<String> pcCombo;
    @FXML private ComboBox<Integer> tamEquipoCombo;
    @FXML private VBox integrantesBox;
    @FXML private Label altaMsg, regMsg;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();
    private final List<Estudiante> integrantes = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            pcDAO.findAllActivas().forEach(pc -> pcCombo.getItems().add(pc.getEtiqueta()));
        } catch (SQLException e) {
            regMsg.setText("Error cargando PCs: " + e.getMessage());
        }
        for (int i=2;i<=10;i++) tamEquipoCombo.getItems().add(i);
        tamEquipoCombo.setOnAction(e -> generarCamposIntegrantes());
        trabajoEquipoCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            tamEquipoCombo.setVisible(newV);
            tamEquipoCombo.setManaged(newV);
            integrantesBox.setVisible(newV);
            integrantesBox.setManaged(newV);
            if (!newV) {
                tamEquipoCombo.getSelectionModel().clearSelection();
                integrantesBox.getChildren().clear();
                integrantes.clear();
            }
        });
    }

    @FXML
    public void altaRapida() {
        try {
            String nc = numControlField.getText().trim();
            String nom = nombreField.getText().trim();
            Estudiante e = estudianteDAO.upsertRapido(nc, nom,
                    generoField.getText().trim(), carreraField.getText().trim());
            altaMsg.setText("OK: " + e.getNombre() + " [" + e.getNumControl() + "]");
            if (trabajoEquipoCheck.isSelected()) {
                integrantes.removeIf(est -> est.getNumControl().equals(e.getNumControl()));
                integrantes.add(e);
            }
        } catch (Exception e) {
            altaMsg.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generarCamposIntegrantes() {
        integrantesBox.getChildren().clear();
        Integer tam = tamEquipoCombo.getValue();
        if (tam == null) return;
        integrantes.removeIf(est -> !est.getNumControl().equals(numControlField.getText().trim()));
        for (int i=1; i<tam; i++) {
            HBox row = new HBox(5);
            TextField tf = new TextField();
            tf.setPromptText("Num. control");
            Label msg = new Label();
            row.getChildren().addAll(tf, msg);
            integrantesBox.getChildren().add(row);
            tf.setOnAction(ev -> buscarIntegrante(tf));
        }
    }

    private void buscarIntegrante(TextField tf) {
        String nc = tf.getText().trim();
        HBox row = (HBox) tf.getParent();
        Label msg = (Label) row.getChildren().get(1);
        try {
            Estudiante e = estudianteDAO.findByNumControl(nc);
            if (e != null) {
                if (integrantes.stream().noneMatch(est -> est.getNumControl().equals(nc))) {
                    integrantes.add(e);
                }
                msg.setText("OK");
            } else {
                TextField nom = new TextField(); nom.setPromptText("Nombre");
                TextField gen = new TextField(); gen.setPromptText("Género");
                TextField carr = new TextField(); carr.setPromptText("Carrera");
                Button guardar = new Button("Guardar");
                guardar.setOnAction(ev -> {
                    try {
                        Estudiante ne = estudianteDAO.upsertRapido(nc, nom.getText().trim(),
                                gen.getText().trim(), carr.getText().trim());
                        integrantes.add(ne);
                        msg.setText("Registrado");
                        row.getChildren().setAll(tf, msg);
                    } catch (Exception ex) {
                        msg.setText("Error");
                    }
                });
                row.getChildren().addAll(nom, gen, carr, guardar);
                msg.setText("Nuevo");
            }
        } catch (SQLException ex) {
            msg.setText("Error: "+ex.getMessage());
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
            if (trabajoEquipoCheck.isSelected()) {
                Integer tam = tamEquipoCombo.getValue();
                if (tam == null) {
                    regMsg.setText("Selecciona el tamaño del equipo.");
                    return;
                }
                if (integrantes.stream().noneMatch(est -> est.getNumControl().equals(e.getNumControl()))) {
                    integrantes.add(e);
                }
                if (integrantes.size() != tam) {
                    regMsg.setText("Faltan integrantes (" + integrantes.size() + "/" + tam + ")");
                    return;
                }
                for (Estudiante est : integrantes) {
                    RegistroVisita r = new RegistroVisita();
                    r.setFecha(LocalDate.now());
                    r.setTurno("MATUTINO");
                    r.setEstudianteId(est.getId());
                    r.setServPc(servPcCheck.isSelected());
                    r.setServConsultaSala(consultaSalaCheck.isSelected());
                    r.setServLecturaSala(lecturaSalaCheck.isSelected());
                    r.setServTrabajoPersonal(trabajoPersonalCheck.isSelected());
                    r.setServTrabajoEquipo(true);
                    r.setGrupoTamano(tam);
                    r.setPcId(servPcCheck.isSelected() && pcCombo.getValue()!=null
                            ? pcDAO.findByEtiqueta(pcCombo.getValue()).getId() : null);
                    r.setHoraEntrada(LocalTime.now());
                    r.setObservacion(obsField.getText());
                    int idVisita = visitaDAO.crear(r);
                    if (r.isServPc() && r.getPcId()!=null) {
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
                }
                regMsg.setText("Visita registrada");
                integrantes.clear();
                integrantesBox.getChildren().clear();
                tamEquipoCombo.getSelectionModel().clearSelection();
            } else {
                RegistroVisita r = new RegistroVisita();
                r.setFecha(LocalDate.now());
                r.setTurno("MATUTINO");
                r.setEstudianteId(e.getId());
                r.setServPc(servPcCheck.isSelected());
                r.setServConsultaSala(consultaSalaCheck.isSelected());
                r.setServLecturaSala(lecturaSalaCheck.isSelected());
                r.setServTrabajoPersonal(trabajoPersonalCheck.isSelected());
                r.setServTrabajoEquipo(false);
                r.setGrupoTamano(null);
                r.setPcId(servPcCheck.isSelected() && pcCombo.getValue()!=null
                        ? pcDAO.findByEtiqueta(pcCombo.getValue()).getId() : null);
                r.setHoraEntrada(LocalTime.now());
                r.setObservacion(obsField.getText());
                int idVisita = visitaDAO.crear(r);
                if (r.isServPc() && r.getPcId()!=null) {
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
            }
        } catch (Exception e) {
            regMsg.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
