package com.biblio.model.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegistroVisita {
    private Integer id;
    private LocalDate fecha;
    private String turno;
    private Integer estudianteId;

    private boolean servPc;
    private boolean servConsultaSala;
    private boolean servLecturaSala;
    private boolean servTrabajoPersonal;
    private boolean servTrabajoEquipo;

    private Integer grupoTamano;
    private Integer pcId;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String observacion;

    // getters/setters
    // ...
    // (por brevedad, agrega aquí todos los getters y setters estándares)
    public Integer getId(){return id;}
    public void setId(Integer id){this.id=id;}
    public LocalDate getFecha(){return fecha;}
    public void setFecha(LocalDate fecha){this.fecha=fecha;}
    public String getTurno(){return turno;}
    public void setTurno(String turno){this.turno=turno;}
    public Integer getEstudianteId(){return estudianteId;}
    public void setEstudianteId(Integer estudianteId){this.estudianteId=estudianteId;}
    public boolean isServPc(){return servPc;}
    public void setServPc(boolean servPc){this.servPc=servPc;}
    public boolean isServConsultaSala(){return servConsultaSala;}
    public void setServConsultaSala(boolean v){this.servConsultaSala=v;}
    public boolean isServLecturaSala(){return servLecturaSala;}
    public void setServLecturaSala(boolean v){this.servLecturaSala=v;}
    public boolean isServTrabajoPersonal(){return servTrabajoPersonal;}
    public void setServTrabajoPersonal(boolean v){this.servTrabajoPersonal=v;}
    public boolean isServTrabajoEquipo(){return servTrabajoEquipo;}
    public void setServTrabajoEquipo(boolean v){this.servTrabajoEquipo=v;}
    public Integer getGrupoTamano(){return grupoTamano;}
    public void setGrupoTamano(Integer grupoTamano){this.grupoTamano=grupoTamano;}
    public Integer getPcId(){return pcId;}
    public void setPcId(Integer pcId){this.pcId=pcId;}
    public LocalTime getHoraEntrada(){return horaEntrada;}
    public void setHoraEntrada(LocalTime horaEntrada){this.horaEntrada=horaEntrada;}
    public LocalTime getHoraSalida(){return horaSalida;}
    public void setHoraSalida(LocalTime horaSalida){this.horaSalida=horaSalida;}
    public String getObservacion(){return observacion;}
    public void setObservacion(String observacion){this.observacion=observacion;}
}
