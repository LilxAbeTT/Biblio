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
import javafx.scene.layout.VBox;

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
    @FXML private TextField numControlField, nombreField, generoField, carreraField;
    @FXML private TextArea obsField;
    @FXML private CheckBox servPcCheck, consultaRapidaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private ComboBox<String> pcCombo;
    @FXML private ComboBox<Integer> tamEquipoCombo;
    @FXML private VBox integrantesBox;

    @FXML private TextField numControlField, nombreField, grupoField, obsField;
    @FXML private ComboBox<String> generoCombo, carreraCombo, pcCombo;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private CheckBox servPcCheck, consultaSalaCheck, lecturaSalaCheck, trabajoPersonalCheck, trabajoEquipoCheck;
    @FXML private ComboBox<String> generoCombo, carreraCombo, pcCombo;
    @FXML private VBox datosEstudianteBox;
    @FXML private Button guardarBtn;
    @FXML private Label altaMsg, regMsg;
    @FXML private Button registrarBtn, limpiarTodoBtn;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();
    private final List<Estudiante> integrantes = new ArrayList<>();

    @FXML
    public void initialize() {
        generoCombo.getItems().setAll("Hombre", "Mujer", "Otro");
        carreraCombo.getItems().setAll(
                "Ing. Administración", "Gastronomía", "Contador Público",
                "Ing. Civil", "Arquitectura", "Lic. En Turismo",
                "Ing. Electromecánica", "Ing. En Sistemas Computacionales");
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
            if (trabajoEquipoCheck.isSelected()) {
                integrantes.removeIf(est -> est.getNumControl().equals(e.getNumControl()));
                integrantes.add(e);
            }
            nombreField.setDisable(true);
            generoCombo.setDisable(true);
            carreraCombo.setDisable(true);
            numControlField.setDisable(true);
            guardarBtn.setVisible(false);
        } catch (Exception e) {
            altaMsg.setText("Error: " + e.getMessage());
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
