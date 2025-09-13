package com.biblio.app;

import com.biblio.db.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.init(); // Crea esquema si no existe

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("Biblio - Acceso");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        Database.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
