package com.biblio.controller;

import com.biblio.model.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    private TextField passText;
    @FXML private CheckBox showPassChk;
    @FXML private Label msgLabel;
    @FXML private ImageView logoView;
    @FXML private Button btnSalir;

    @FXML
    private void initialize() {
        // Carga robusta del logo: intenta con getResource("/...") y con el context classloader
        URL url = getClass().getResource("/images/logo.png");
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource("images/logo.png");
        }

        if (url != null) {
            logoView.setImage(new Image(url.toExternalForm()));
            System.out.println("Logo cargado desde: " + url);
        } else {
            System.err.println("No se encontró /images/logo.png en el classpath");
        }

        // Campo alterno para mostrar texto plano de la contraseña
        passText = new TextField();
        passText.setManaged(false);
        passText.setVisible(false);
        passText.textProperty().bindBidirectional(passField.textProperty());
        ((GridPane) passField.getParent()).add(passText, 1, 1);

        // Conmutar visibilidad de los campos según el checkbox
        showPassChk.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                passText.setManaged(true);
                passText.setVisible(true);
                passField.setManaged(false);
                passField.setVisible(false);
            } else {
                passText.setManaged(false);
                passText.setVisible(false);
                passField.setManaged(true);
                passField.setVisible(true);
            }
        });

        // Mensaje inicial opcional
        // msgLabel.setText("");
    }

    @FXML
    private void login() {
        try {
            String u = userField.getText();
            String p = passField.getText();

            boolean ok = UsuarioDAO.validar(u, p); // tu lógica
            if (ok) {
                Stage st = (Stage) userField.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                st.setScene(new Scene(loader.load(), 1200, 750));
                st.setMaximized(true);
                st.setTitle("Biblio - Panel");
            } else {
                msgLabel.setText("Usuario/contraseña incorrectos o inactivo.");
            }
        } catch (Exception ex) {
            msgLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void salir() {
        ((Stage) btnSalir.getScene().getWindow()).close();
    }
}
