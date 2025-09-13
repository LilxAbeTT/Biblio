package com.biblio.controller;

import com.biblio.model.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private TextField passFieldText;
    @FXML private CheckBox rememberCheck;
    @FXML private CheckBox showPassCheck;
    @FXML private Label msgLabel;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    public void initialize() {
        passFieldText.textProperty().bindBidirectional(passField.textProperty());

        showPassCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passFieldText.setVisible(true);
                passFieldText.setManaged(true);
                passField.setVisible(false);
                passField.setManaged(false);
            } else {
                passFieldText.setVisible(false);
                passFieldText.setManaged(false);
                passField.setVisible(true);
                passField.setManaged(true);
            }
        });

        String savedUser = prefs.get("savedUser", "");
        if (!savedUser.isEmpty()) {
            userField.setText(savedUser);
            rememberCheck.setSelected(true);
        }
    }

    @FXML
    public void login(ActionEvent e) {
        try {
            String u = userField.getText().trim();
            String p = passField.getText().trim();
            if (rememberCheck.isSelected()) {
                prefs.put("savedUser", u);
            } else {
                prefs.remove("savedUser");
            }
            if (usuarioDAO.validar(u, p)) {
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
}
