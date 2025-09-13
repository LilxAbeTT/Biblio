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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller limpio y consistente con el FXML propuesto.
 * - IDs únicos
 * - Métodos sin duplicados
 * - Tipos que empatan con el FXML
 */
public class RegistrarVisitaController {

    // --- Alta rápida / búsqueda ---
    @FXML private TextField numControlField;
    @FXML private TextField nombreField;
    @FXML private ComboBox<String> generoCombo;
    @FXML private ComboBox<String> carreraCombo;
    @FXML private VBox datosEstudianteBox;
    @FXML private Button guardarBtn;
    @FXML private Label altaMsg;

    // --- Servicios ---
    @FXML private CheckBox trabajoPersonalCheck;
    @FXML private CheckBox trabajoEquipoCheck;
    @FXML private ComboBox<Integer> tamEquipoCombo;
    @FXML private VBox integrantesBox;

    @FXML private CheckBox consultaRapidaCheck;   // "Consulta en sala (rápida)"
    @FXML private CheckBox lecturaSalaCheck;
    @FXML private CheckBox servPcCheck;
    @FXML private ComboBox<String> pcCombo;
    @FXML private TextArea obsField;

    // --- Acciones ---
    @FXML private Button registrarBtn;
    @FXML private Button limpiarTodoBtn;
    @FXML private Label regMsg;

    // --- DAOs y estado ---
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PcDAO pcDAO = new PcDAO();
    private final RegistroVisitaDAO visitaDAO = new RegistroVisitaDAO();
    private final SesionPcDAO sesionDAO = new SesionPcDAO();

    /** Integrantes del equipo (incluyendo al solicitante si procede) */
    private final List<Estudiante> integrantes = new ArrayList<>();

    // =====================================================================
    // Inicialización
    // =====================================================================
    @FXML
    public void initialize() {
        // Catálogos básicos
        generoCombo.getItems().setAll("Hombre", "Mujer", "Otro");
        carreraCombo.getItems().setAll(
                "Ing. Administración", "Gastronomía", "Contador Público",
                "Ing. Civil", "Arquitectura", "Lic. En Turismo",
                "Ing. Electromecánica", "Ing. En Sistemas Computacionales"
        );
        carreraCombo.setEditable(true);

        // Tamaño de equipo (2..10)
        tamEquipoCombo.getItems().clear();
        for (int i = 2; i <= 10; i++) tamEquipoCombo.getItems().add(i);

        // Mostrar/ocultar controles de equipo al marcar trabajo en equipo
        tamEquipoCombo.visibleProperty().bind(trabajoEquipoCheck.selectedProperty());
        tamEquipoCombo.managedProperty().bind(trabajoEquipoCheck.selectedProperty());
        integrantesBox.visibleProperty().bind(trabajoEquipoCheck.selectedProperty());
        integrantesBox.managedProperty().bind(trabajoEquipoCheck.selectedProperty());

        tamEquipoCombo.setOnAction(e -> generarCamposIntegrantes());
        trabajoEquipoCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                tamEquipoCombo.getSelectionModel().clearSelection();
                integrantesBox.getChildren().clear();
                integrantes.clear();
            }
        });

        // PCs disponibles: cargar y habilitar combo sólo si hay stock
        pcCombo.setDisable(true);
        loadAvailablePcs();

        servPcCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            pcCombo.setDisable(!newV);
            if (newV) loadAvailablePcs();
        });

        // Buscar al presionar Enter en el campo
        numControlField.setOnAction(e -> buscarEstudiante());

        // Estado inicial
        datosEstudianteBox.setVisible(false);
        datosEstudianteBox.setManaged(false);
        altaMsg.setText("");
        regMsg.setText("");
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
                pcCombo.getSelectionModel().clearSelection();
                regMsg.setText("No hay PCs disponibles.");
            }
        } catch (SQLException e) {
            regMsg.setText("Error cargando PCs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    // Alta rápida / Búsqueda
    // =====================================================================
    @FXML
    public void buscarEstudiante() {
        String nc = Optional.ofNullable(numControlField.getText()).orElse("").trim();
        if (nc.isBlank()) {
            altaMsg.setText("Ingrese número de control.");
            return;
        }

        try {
            Estudiante e = estudianteDAO.findByNumControl(nc);
            datosEstudianteBox.setVisible(true);
            datosEstudianteBox.setManaged(true);

            if (e != null) {
                // Encontrado
                nombreField.setText(e.getNombre());
                generoCombo.setValue(e.getGenero());
                carreraCombo.setValue(e.getCarrera());
                nombreField.setDisable(true);
                generoCombo.setDisable(true);
                carreraCombo.setDisable(true);
                numControlField.setDisable(true);
                guardarBtn.setVisible(false);
                altaMsg.setText("Estudiante encontrado.");
            } else {
                // Nuevo
                nombreField.clear();
                generoCombo.getSelectionModel().clearSelection();
                carreraCombo.getSelectionModel().clearSelection();
                nombreField.setDisable(false);
                generoCombo.setDisable(false);
                carreraCombo.setDisable(false);
                numControlField.setDisable(false);
                guardarBtn.setVisible(true);
                altaMsg.setText("Nuevo estudiante. Completa datos y guarda.");
            }
        } catch (SQLException ex) {
            altaMsg.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void guardarEstudiante() {
        String nc = Optional.ofNullable(numControlField.getText()).orElse("").trim();
        String nombre = Optional.ofNullable(nombreField.getText()).orElse("").trim();
        String genero = generoCombo.getValue();
        String carrera = carreraCombo.getValue();

        if (nc.isBlank()) {
            altaMsg.setText("Ingrese número de control.");
            return;
        }
        if (nombre.isEmpty() || genero == null || carrera == null) {
            altaMsg.setText("Capture nombre, género y carrera.");
            return;
        }

        try {
            // Evitar duplicado
            Estudiante ya = estudianteDAO.findByNumControl(nc);
            if (ya != null) {
                altaMsg.setText("Ya existe un estudiante con ese número de control.");
                return;
            }

            Estudiante e = estudianteDAO.upsertRapido(nc, nombre, genero, carrera);
            altaMsg.setText("OK: " + e.getNombre() + " [" + e.getNumControl() + "]");
            // Bloquear edición tras alta
            nombreField.setDisable(true);
            generoCombo.setDisable(true);
            carreraCombo.setDisable(true);
            numControlField.setDisable(true);
            guardarBtn.setVisible(false);

            // Si está en modo equipo, añade a integrantes
            if (trabajoEquipoCheck.isSelected()) {
                integrantes.removeIf(est -> est.getNumControl().equals(e.getNumControl()));
                integrantes.add(e);
            }
        } catch (Exception ex) {
            altaMsg.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
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

    // =====================================================================
    // Equipo
    // =====================================================================
    private void generarCamposIntegrantes() {
        integrantesBox.getChildren().clear();
        Integer tam = tamEquipoCombo.getValue();
        if (tam == null) return;

        // Mantener al solicitante si ya existe:
        String titularNc = Optional.ofNullable(numControlField.getText()).orElse("").trim();
        integrantes.removeIf(est -> !est.getNumControl().equals(titularNc));

        // Se crean (tam - 1) campos adicionales
        for (int i = 1; i < tam; i++) {
            HBox row = new HBox(6);
            TextField tf = new TextField();
            tf.setPromptText("Num. control");
            Label msg = new Label();
            row.getChildren().addAll(tf, msg);
            integrantesBox.getChildren().add(row);

            tf.setOnAction(ev -> buscarIntegrante(tf));
        }
    }

    private void buscarIntegrante(TextField tf) {
        String nc = Optional.ofNullable(tf.getText()).orElse("").trim();
        HBox row = (HBox) tf.getParent();
        Label msg = (Label) row.getChildren().get(1);

        if (nc.isBlank()) {
            msg.setText("Ingrese NC");
            return;
        }

        try {
            Estudiante e = estudianteDAO.findByNumControl(nc);
            if (e != null) {
                // Evitar duplicados
                if (integrantes.stream().noneMatch(est -> est.getNumControl().equals(nc))) {
                    integrantes.add(e);
                }
                msg.setText("OK");
            } else {
                // Alta en línea del integrante
                TextField nom = new TextField(); nom.setPromptText("Nombre");
                ComboBox<String> gen = new ComboBox<>();
                gen.getItems().setAll("Hombre", "Mujer", "Otro");
                gen.setPromptText("Género");
                TextField carr = new TextField(); carr.setPromptText("Carrera");
                Button guardar = new Button("Guardar");

                guardar.setOnAction(ev -> {
                    try {
                        String vNom = Optional.ofNullable(nom.getText()).orElse("").trim();
                        String vGen = gen.getValue();
                        String vCar = Optional.ofNullable(carr.getText()).orElse("").trim();
                        if (vNom.isEmpty() || vGen == null || vCar.isEmpty()) {
                            msg.setText("Faltan datos");
                            return;
                        }
                        Estudiante ne = estudianteDAO.upsertRapido(nc, vNom, vGen, vCar);
                        if (integrantes.stream().noneMatch(est -> est.getNumControl().equals(nc))) {
                            integrantes.add(ne);
                        }
                        msg.setText("Registrado");
                        // Dejar solo NC + estado
                        row.getChildren().setAll(tf, msg);
                    } catch (Exception ex) {
                        msg.setText("Error");
                        ex.printStackTrace();
                    }
                });

                row.getChildren().addAll(nom, gen, carr, guardar);
                msg.setText("Nuevo");
            }
        } catch (SQLException ex) {
            msg.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // =====================================================================
    // Registro de visita
    // =====================================================================
    @FXML
    public void registrarVisita() {
        try {
            String nc = Optional.ofNullable(numControlField.getText()).orElse("").trim();
            if (nc.isBlank()) {
                regMsg.setText("Ingrese número de control y busque/guarde primero.");
                return;
            }

            Estudiante titular = estudianteDAO.findByNumControl(nc);
            if (titular == null) {
                regMsg.setText("Primero da de alta al estudiante.");
                return;
            }

            // Derivar turno sencillo por la hora (opcional)
            String turno = derivarTurno(LocalTime.now());

            if (trabajoEquipoCheck.isSelected()) {
                Integer tam = tamEquipoCombo.getValue();
                if (tam == null) {
                    regMsg.setText("Selecciona el tamaño del equipo.");
                    return;
                }
                // Asegura que el titular esté dentro de la lista
                if (integrantes.stream().noneMatch(est -> est.getNumControl().equals(titular.getNumControl()))) {
                    integrantes.add(titular);
                }
                if (integrantes.size() != tam) {
                    regMsg.setText("Faltan integrantes (" + integrantes.size() + "/" + tam + ")");
                    return;
                }

                // Registrar para cada integrante
                for (Estudiante est : integrantes) {
                    int idVisita = crearVisitaYPosibleSesionPc(est, turno, true, tam);
                }
                regMsg.setText("Visita registrada (equipo).");
                // Limpiar estado de equipo
                integrantes.clear();
                integrantesBox.getChildren().clear();
                tamEquipoCombo.getSelectionModel().clearSelection();

            } else {
                int idVisita = crearVisitaYPosibleSesionPc(titular, turno, false, null);
                regMsg.setText("Visita registrada (#" + idVisita + ")");
            }

            new Alert(Alert.AlertType.INFORMATION, "Visita registrada").showAndWait();
            limpiarFormulario();

        } catch (Exception e) {
            regMsg.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int crearVisitaYPosibleSesionPc(Estudiante est, String turno, boolean esEquipo, Integer tamEquipo) throws Exception {
        RegistroVisita r = new RegistroVisita();
        r.setFecha(LocalDate.now());
        r.setTurno(turno);
        r.setEstudianteId(est.getId());
        r.setServPc(servPcCheck.isSelected());
        r.setServConsultaSala(consultaRapidaCheck.isSelected());
        r.setServLecturaSala(lecturaSalaCheck.isSelected());
        r.setServTrabajoPersonal(trabajoPersonalCheck.isSelected());
        r.setServTrabajoEquipo(esEquipo);
        r.setGrupoTamano(esEquipo ? tamEquipo : null);
        r.setPcId(servPcCheck.isSelected() && pcCombo.getValue() != null
                ? pcDAO.findByEtiqueta(pcCombo.getValue()).getId() : null);
        r.setHoraEntrada(LocalTime.now());
        r.setObservacion(obsField.getText());

        int idVisita = visitaDAO.crear(r);

        if (r.isServPc() && r.getPcId() != null) {
            // Crear sesión con duración por defecto (60 minutos)
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
        return idVisita;
    }

    private String derivarTurno(LocalTime now) {
        // Ejemplo simple: antes de 14:00 MATUTINO, si no VESPERTINO
        return now.isBefore(LocalTime.of(14, 0)) ? "MATUTINO" : "VESPERTINO";
    }

    // =====================================================================
    // Limpieza de formulario
    // =====================================================================
    @FXML
    public void limpiarFormulario() {
        // Alta / búsqueda
        limpiarBusqueda();

        // Servicios
        servPcCheck.setSelected(false);
        consultaRapidaCheck.setSelected(false);
        lecturaSalaCheck.setSelected(false);
        trabajoPersonalCheck.setSelected(false);
        trabajoEquipoCheck.setSelected(false);
        tamEquipoCombo.getSelectionModel().clearSelection();
        pcCombo.getSelectionModel().clearSelection();
        obsField.clear();

        integrantes.clear();
        integrantesBox.getChildren().clear();

        // Mensajes
        altaMsg.setText("");
        regMsg.setText("");

        // Recalcular PCs disponibles
        loadAvailablePcs();
    }
}
