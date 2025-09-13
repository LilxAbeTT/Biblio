package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.RegistroVisita;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class RegistroVisitaDAO {

    public int crear(RegistroVisita r) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("""
            INSERT INTO RegistroVisita(
              fecha, turno, estudiante_id, serv_pc, serv_consulta_sala, serv_lectura_sala,
              serv_trabajo_personal, serv_trabajo_equipo, grupo_tamano, pc_id,
              hora_entrada, hora_salida, observacion
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)""", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getFecha().toString());
            ps.setString(2, r.getTurno());
            ps.setInt(3, r.getEstudianteId());
            ps.setInt(4, r.isServPc()?1:0);
            ps.setInt(5, r.isServConsultaSala()?1:0);
            ps.setInt(6, r.isServLecturaSala()?1:0);
            ps.setInt(7, r.isServTrabajoPersonal()?1:0);
            ps.setInt(8, r.isServTrabajoEquipo()?1:0);
            if (r.getGrupoTamano()==null) ps.setNull(9, Types.INTEGER); else ps.setInt(9, r.getGrupoTamano());
            if (r.getPcId()==null) ps.setNull(10, Types.INTEGER); else ps.setInt(10, r.getPcId());
            ps.setString(11, r.getHoraEntrada().toString());
            ps.setString(12, r.getHoraSalida()==null? null : r.getHoraSalida().toString());
            ps.setString(13, r.getObservacion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    Database.get().commit();
                    return id;
                }
            }
        }
        return -1;
    }

    public void cerrarVisita(int id, LocalTime horaSalida) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "UPDATE RegistroVisita SET hora_salida=? WHERE id=?")) {
            ps.setString(1, horaSalida.toString());
            ps.setInt(2, id);
            ps.executeUpdate();
            Database.get().commit();
        }
    }

    public int contarHoyPorServicio(LocalDate fecha, String columnaServicio) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT COUNT(*) FROM RegistroVisita WHERE fecha=? AND "+columnaServicio+"=1")) {
            ps.setString(1, fecha.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public String[][] totalesPorServicio(java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
        String sql = """
        SELECT 
          SUM(serv_pc) AS uso_pc,
          SUM(serv_consulta_sala) AS consulta,
          SUM(serv_lectura_sala) AS lectura,
          SUM(serv_trabajo_personal) AS personal,
          SUM(serv_trabajo_equipo) AS equipo
        FROM RegistroVisita
        WHERE fecha BETWEEN ? AND ?
    """;
        try (PreparedStatement ps = com.biblio.db.Database.get().prepareStatement(sql)) {
            ps.setString(1, desde.toString());
            ps.setString(2, hasta.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new String[0][];
                return new String[][]{
                        {"Servicio","Cantidad"},
                        {"Uso PC", String.valueOf(rs.getInt("uso_pc"))},
                        {"Consulta en sala", String.valueOf(rs.getInt("consulta"))},
                        {"Lectura en sala", String.valueOf(rs.getInt("lectura"))},
                        {"Trabajo personal", String.valueOf(rs.getInt("personal"))},
                        {"Trabajo en equipo", String.valueOf(rs.getInt("equipo"))}
                };
            }
        }
    }

    /** Promedio de estancia por servicio (min), solo registros con hora_salida no nula */
    public String[][] promediosPorServicio(java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
        String base = """
      SELECT 
        AVG((julianday(hora_salida) - julianday(hora_entrada)) * 24 * 60.0) AS avg_min
      FROM RegistroVisita
      WHERE fecha BETWEEN ? AND ? AND %s=1 AND hora_salida IS NOT NULL
    """;
        String[][] out = new String[][]{
                {"Servicio","Promedio (min)"},
                {"Uso PC", "0"},
                {"Consulta en sala", "0"},
                {"Lectura en sala", "0"},
                {"Trabajo personal", "0"},
                {"Trabajo en equipo", "0"}
        };
        String[] servicios = {"serv_pc","serv_consulta_sala","serv_lectura_sala","serv_trabajo_personal","serv_trabajo_equipo"};
        for (int i=0;i<servicios.length;i++){
            try (PreparedStatement ps = com.biblio.db.Database.get().prepareStatement(base.formatted(servicios[i]))) {
                ps.setString(1, desde.toString());
                ps.setString(2, hasta.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    double v = rs.next() ? rs.getDouble(1) : 0;
                    if (rs.wasNull()) v = 0;
                    out[i+1][1] = String.format(java.util.Locale.US,"%.0f", v);
                }
            }
        }
        return out;
    }
}
