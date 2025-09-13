package com.biblio.db;

import java.nio.file.*;
import java.sql.*;

public class Database {
    private static Connection conn;

    public static synchronized void init() {
        try {
            String url = "jdbc:sqlite:biblio.db";
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
            createSchema();
            seed();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    public static Connection get() {
        return conn;
    }

    public static synchronized void close() {
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }

    private static void createSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Usuario(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              nombre TEXT NOT NULL UNIQUE,
              contrasena TEXT NOT NULL,
              activo INTEGER NOT NULL DEFAULT 1
            )""");
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Estudiante(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              numControl TEXT NOT NULL UNIQUE,
              nombre TEXT NOT NULL,
              genero TEXT,
              carrera TEXT
            )""");
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Pc(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              etiqueta TEXT NOT NULL UNIQUE,
              activo INTEGER NOT NULL DEFAULT 1
            )""");
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS RegistroVisita(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              fecha TEXT NOT NULL,
              turno TEXT NOT NULL,
              estudiante_id INTEGER NOT NULL,
              serv_pc INTEGER NOT NULL DEFAULT 0,
              serv_consulta_sala INTEGER NOT NULL DEFAULT 0,
              serv_lectura_sala INTEGER NOT NULL DEFAULT 0,
              serv_trabajo_personal INTEGER NOT NULL DEFAULT 0,
              serv_trabajo_equipo INTEGER NOT NULL DEFAULT 0,
              grupo_tamano INTEGER,
              pc_id INTEGER,
              hora_entrada TEXT NOT NULL,
              hora_salida TEXT,
              observacion TEXT,
              FOREIGN KEY(estudiante_id) REFERENCES Estudiante(id),
              FOREIGN KEY(pc_id) REFERENCES Pc(id)
            )""");
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS SesionPc(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              registro_id INTEGER NOT NULL,
              pc_id INTEGER NOT NULL,
              inicio TEXT NOT NULL,
              duracion_min INTEGER NOT NULL,
              fin_previsto TEXT NOT NULL,
              fin_real TEXT,
              estado TEXT NOT NULL,
              avisado INTEGER NOT NULL DEFAULT 0,
              FOREIGN KEY(registro_id) REFERENCES RegistroVisita(id),
              FOREIGN KEY(pc_id) REFERENCES Pc(id)
            )""");
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Configuracion(
              clave TEXT PRIMARY KEY,
              valor TEXT
            )""");
            st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS DestinatarioReporte(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              email TEXT NOT NULL UNIQUE
            )""");
        }
    }

    private static void seed() throws SQLException {
        // Usuario admin por defecto
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Usuario");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO Usuario(nombre, contrasena, activo) VALUES(?,?,1)")) {
                    ins.setString(1, "admin");
                    ins.setString(2, "admin"); // para demo; se puede encriptar más adelante
                    ins.executeUpdate();
                }
            }
        }
        // PCs demo si no hay
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Pc");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO Pc(etiqueta, activo) VALUES(?,1)")) {
                    for (int i = 1; i <= 12; i++) {
                        ins.setString(1, "PC-" + i);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }
        }
        // Config por defecto
        upsertConfig("pc.tiempo_minutos", "60");
        upsertConfig("pc.aviso_minutos", "5");
        upsertConfig("smtp.host", "");
        upsertConfig("smtp.port", "587");
        upsertConfig("smtp.user", "");
        upsertConfig("smtp.pass", "");
        upsertConfig("smtp.from", "");
    }

    private static void upsertConfig(String clave, String valor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO Configuracion(clave, valor) VALUES(?,?)
            ON CONFLICT(clave) DO UPDATE SET valor=excluded.valor""")) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        }
    }
}
