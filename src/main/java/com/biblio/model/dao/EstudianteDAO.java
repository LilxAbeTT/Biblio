package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public Estudiante upsertRapido(String numControl, String nombre, String genero, String carrera) throws SQLException {
        Estudiante found = findByNumControl(numControl);
        if (found != null) {
            // actualizar nombre/genero/carrera si llegan no nulos
            try (PreparedStatement ps = Database.get().prepareStatement(
                    "UPDATE Estudiante SET nombre=?, genero=?, carrera=? WHERE id=?")) {
                ps.setString(1, nombre);
                ps.setString(2, genero);
                ps.setString(3, carrera);
                ps.setInt(4, found.getId());
                ps.executeUpdate();
            }
            Database.get().commit();
            found.setNombre(nombre); found.setGenero(genero); found.setCarrera(carrera);
            return found;
        } else {
            try (PreparedStatement ps = Database.get().prepareStatement(
                    "INSERT INTO Estudiante(numControl,nombre,genero,carrera) VALUES(?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, numControl);
                ps.setString(2, nombre);
                ps.setString(3, genero);
                ps.setString(4, carrera);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        Estudiante e = new Estudiante(keys.getInt(1), numControl, nombre, genero, carrera);
                        Database.get().commit();
                        return e;
                    }
                }
            }
        }
        return null;
    }

    public Estudiante findByNumControl(String numControl) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT id,numControl,nombre,genero,carrera FROM Estudiante WHERE numControl=?")) {
            ps.setString(1, numControl);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Estudiante(
                            rs.getInt("id"),
                            rs.getString("numControl"),
                            rs.getString("nombre"),
                            rs.getString("genero"),
                            rs.getString("carrera"));
                }
                return null;
            }
        }
    }

    public List<Estudiante> search(String term) throws SQLException {
        List<Estudiante> list = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT id,numControl,nombre,genero,carrera FROM Estudiante " +
                        "WHERE numControl LIKE ? OR nombre LIKE ? ORDER BY nombre LIMIT 100")) {
            String like = "%" + term + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Estudiante(
                            rs.getInt("id"), rs.getString("numControl"), rs.getString("nombre"),
                            rs.getString("genero"), rs.getString("carrera")));
                }
            }
        }
        return list;
    }
}
