package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.Configuracion;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionDAO {

    public Map<String,String> getAll() throws SQLException {
        Map<String,String> map = new HashMap<>();
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT clave,valor FROM Configuracion");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("clave"), rs.getString("valor"));
        }
        return map;
    }

    public void set(String clave, String valor) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("""
            INSERT INTO Configuracion(clave, valor) VALUES(?,?)
            ON CONFLICT(clave) DO UPDATE SET valor=excluded.valor""")) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
            Database.get().commit();
        }
    }

    public String get(String clave, String def) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT valor FROM Configuracion WHERE clave=?")) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()? rs.getString(1) : def;
            }
        }
    }
}
