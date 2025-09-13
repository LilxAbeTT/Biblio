package com.biblio.model.dao;

import com.biblio.db.Database;
import com.biblio.model.entities.SesionPc;
import com.biblio.model.enums.EstadoSesionPc;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SesionPcDAO {

    private SesionPc map(ResultSet rs) throws SQLException {
        SesionPc s = new SesionPc();
        s.setId(rs.getInt("id"));
        s.setRegistroId(rs.getInt("registro_id"));
        s.setPcId(rs.getInt("pc_id"));
        s.setInicio(LocalDateTime.parse(rs.getString("inicio")));
        s.setDuracionMin(rs.getInt("duracion_min"));
        s.setFinPrevisto(LocalDateTime.parse(rs.getString("fin_previsto")));
        String finReal = rs.getString("fin_real");
        s.setFinReal(finReal==null? null : LocalDateTime.parse(finReal));
        s.setEstado(EstadoSesionPc.valueOf(rs.getString("estado")));
        s.setAvisado(rs.getInt("avisado")==1);
        return s;
    }

    public int crear(SesionPc s) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("""
            INSERT INTO SesionPc(registro_id, pc_id, inicio, duracion_min, fin_previsto, fin_real, estado, avisado)
            VALUES(?,?,?,?,?,?,?,?)""", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getRegistroId());
            ps.setInt(2, s.getPcId());
            ps.setString(3, s.getInicio().toString());
            ps.setInt(4, s.getDuracionMin());
            ps.setString(5, s.getFinPrevisto().toString());
            ps.setString(6, s.getFinReal()==null? null : s.getFinReal().toString());
            ps.setString(7, s.getEstado().name());
            ps.setInt(8, s.isAvisado()?1:0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Database.get().commit();
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void actualizar(SesionPc s) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("""
            UPDATE SesionPc SET fin_real=?, estado=?, avisado=? WHERE id=?""")) {
            ps.setString(1, s.getFinReal()==null? null : s.getFinReal().toString());
            ps.setString(2, s.getEstado().name());
            ps.setInt(3, s.isAvisado()?1:0);
            ps.setInt(4, s.getId());
            ps.executeUpdate();
            Database.get().commit();
        }
    }

    public List<SesionPc> activas() throws SQLException {
        List<SesionPc> list = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(
                "SELECT * FROM SesionPc WHERE estado != 'CERRADA' ORDER BY fin_previsto")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public String[][] ocupacionPC(java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
        String sql = """
      SELECT pc_id,
             SUM( (julianday(COALESCE(fin_real, fin_previsto)) - julianday(inicio)) * 24 * 60.0 ) AS min_tot
      FROM SesionPc
      WHERE date(inicio) BETWEEN ? AND ?
      GROUP BY pc_id
      ORDER BY pc_id
    """;
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        rows.add(new String[]{"PC","Tiempo (min)","%Uso"});
        try (PreparedStatement ps = com.biblio.db.Database.get().prepareStatement(sql)) {
            ps.setString(1, desde.toString());
            ps.setString(2, hasta.toString());
            try (ResultSet rs = ps.executeQuery()) {
                // Ventana total en minutos por día*horas_aprox (8h por día como referencia simple)
                long dias = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1;
                double ventana = dias * 8 * 60.0; // 8 horas/día de referencia
                while (rs.next()) {
                    int pcId = rs.getInt("pc_id");
                    double min = rs.getDouble("min_tot");
                    String pct = ventana > 0 ? String.format(java.util.Locale.US, "%.0f%%", (min/ventana)*100.0) : "0%";
                    rows.add(new String[]{"PC-"+pcId, String.format(java.util.Locale.US,"%.0f", min), pct});
                }
            }
        }
        return rows.toArray(String[][]::new);
    }
}
