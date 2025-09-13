package com.biblio.service;

import com.biblio.model.dao.ConfiguracionDAO;
import com.biblio.model.dao.SesionPcDAO;
import com.biblio.model.entities.SesionPc;
import com.biblio.model.enums.EstadoSesionPc;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SessionManager {
    private final SesionPcDAO sesionDao = new SesionPcDAO();
    private final ConfiguracionDAO configDao = new ConfiguracionDAO();

    public void tick() {
        try {
            int avisoMin = Integer.parseInt(configDao.get("pc.aviso_minutos", "5"));
            List<SesionPc> activas = sesionDao.activas();
            LocalDateTime now = LocalDateTime.now();
            for (SesionPc s : activas) {
                if (s.getEstado()==EstadoSesionPc.CERRADA) continue;
                long minutesLeft = Duration.between(now, s.getFinPrevisto()).toMinutes();
                if (minutesLeft <= 0 && s.getEstado()!=EstadoSesionPc.VENCIDA) {
                    s.setEstado(EstadoSesionPc.VENCIDA);
                    sesionDao.actualizar(s);
                } else if (minutesLeft <= avisoMin && s.getEstado()==EstadoSesionPc.ACTIVA) {
                    s.setEstado(EstadoSesionPc.POR_VENCER);
                    sesionDao.actualizar(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void extender(SesionPc s, int minutos) throws SQLException {
        s.setFinPrevisto(s.getFinPrevisto().plusMinutes(minutos));
        s.setEstado(EstadoSesionPc.ACTIVA);
        sesionDao.actualizar(s);
    }

    public void cerrar(SesionPc s) throws SQLException {
        s.setFinReal(LocalDateTime.now());
        s.setEstado(EstadoSesionPc.CERRADA);
        sesionDao.actualizar(s);
    }
}
