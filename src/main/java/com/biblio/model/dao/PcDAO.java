package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.Pc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PcDAO {
    public List<Pc> findAllActivas() throws SQLException {
        List<Pc> list = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT id, etiqueta, activo FROM Pc WHERE activo=1 ORDER BY etiqueta")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Pc(rs.getInt("id"), rs.getString("etiqueta"), rs.getInt("activo")==1));
                }
            }
        }
        return list;
    }

    public Pc findByEtiqueta(String etiqueta) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT id, etiqueta, activo FROM Pc WHERE etiqueta=?")) {
            ps.setString(1, etiqueta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Pc(rs.getInt("id"), rs.getString("etiqueta"), rs.getInt("activo")==1);
                return null;
            }
        }
    }
}
