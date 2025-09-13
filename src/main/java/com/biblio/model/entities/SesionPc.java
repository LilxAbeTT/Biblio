package com.biblio.model.entities;

import com.biblio.model.enums.EstadoSesionPc;
import java.time.LocalDateTime;

public class SesionPc {
    private Integer id;
    private Integer registroId;
    private Integer pcId;
    private LocalDateTime inicio;
    private int duracionMin;
    private LocalDateTime finPrevisto;
    private LocalDateTime finReal;
    private EstadoSesionPc estado;
    private boolean avisado;

    public Integer getId(){return id;}
    public void setId(Integer id){this.id=id;}
    public Integer getRegistroId(){return registroId;}
    public void setRegistroId(Integer registroId){this.registroId=registroId;}
    public Integer getPcId(){return pcId;}
    public void setPcId(Integer pcId){this.pcId=pcId;}
    public LocalDateTime getInicio(){return inicio;}
    public void setInicio(LocalDateTime inicio){this.inicio=inicio;}
    public int getDuracionMin(){return duracionMin;}
    public void setDuracionMin(int duracionMin){this.duracionMin=duracionMin;}
    public LocalDateTime getFinPrevisto(){return finPrevisto;}
    public void setFinPrevisto(LocalDateTime finPrevisto){this.finPrevisto=finPrevisto;}
    public LocalDateTime getFinReal(){return finReal;}
    public void setFinReal(LocalDateTime finReal){this.finReal=finReal;}
    public EstadoSesionPc getEstado(){return estado;}
    public void setEstado(EstadoSesionPc estado){this.estado=estado;}
    public boolean isAvisado(){return avisado;}
    public void setAvisado(boolean avisado){this.avisado=avisado;}
}
