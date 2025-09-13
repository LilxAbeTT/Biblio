module com.biblio {
    requires javafx.graphics;   // base de JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Libs con módulo JPMS válido:
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires javafx.swing; // TilesFX lo necesita
    requires org.apache.fontbox;

    // SQLite y otros (usan nombres válidos)
    requires org.xerial.sqlitejdbc;
    requires org.apache.commons.csv;
    requires org.apache.poi.ooxml;
    requires org.apache.pdfbox;
    requires jakarta.mail;

    // Aperturas para FXML y propiedades JavaFX
    opens com.biblio.app to javafx.graphics;
    opens com.biblio.controller to javafx.fxml;
    opens com.biblio.model.entities to javafx.base;

    exports com.biblio.app;
}
