package com.biblio.controller;

import com.biblio.service.SessionManager;
import com.biblio.service.TimerService;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainController {
    @FXML private TabPane tabPane;

    private final SessionManager sessionManager = new SessionManager();
    private final TimerService timer = new TimerService();

    @FXML
    public void initialize() {
        timer.addListener(n -> sessionManager.tick()); // ~60fps, pero lógica liviana
        timer.start();
    }

    @FXML
    public void logout() {
        timer.stop();
        Stage st = (Stage) tabPane.getScene().getWindow();
        st.close();
    }
}
