package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.DestinatarioReporte;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinatarioReporteDAO {

    public void add(String email) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "INSERT INTO DestinatarioReporte(email) VALUES(?)")) {
            ps.setString(1, email);
            ps.executeUpdate();
            Database.get().commit();
        }
    }

    public void remove(int id) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "DELETE FROM DestinatarioReporte WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            Database.get().commit();
        }
    }

    public List<DestinatarioReporte> list() throws SQLException {
        List<DestinatarioReporte> list = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT id,email FROM DestinatarioReporte ORDER BY email")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DestinatarioReporte d = new DestinatarioReporte();
                    d.setId(rs.getInt("id"));
                    d.setEmail(rs.getString("email"));
                    list.add(d);
                }
            }
        }
        return list;
    }
}
