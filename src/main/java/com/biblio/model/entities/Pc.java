package com.biblio.model.entities;

public class Pc {
    private Integer id;
    private String etiqueta;
    private boolean activo;

    public Pc() {}
    public Pc(Integer id, String etiqueta, boolean activo) {
        this.id = id; this.etiqueta = etiqueta; this.activo = activo;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
