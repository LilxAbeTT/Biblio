package com.biblio.controller;

import com.biblio.model.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label msgLabel;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void login(ActionEvent e) {
        try {
            String u = userField.getText().trim();
            String p = passField.getText().trim();
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
