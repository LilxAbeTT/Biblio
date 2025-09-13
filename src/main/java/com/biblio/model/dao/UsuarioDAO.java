package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.Usuario;

import java.sql.*;

public class UsuarioDAO {
    public Usuario findByNombre(String nombre) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT id,nombre,contrasena,activo FROM Usuario WHERE nombre=?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("contrasena"),
                            rs.getInt("activo") == 1
                    );
                }
                return null;
            }
        }
    }

    public boolean validar(String nombre, String contrasena) throws SQLException {
        Usuario u = findByNombre(nombre);
        return (u != null && u.isActivo() && u.getContrasena().equals(contrasena));
    }
}
