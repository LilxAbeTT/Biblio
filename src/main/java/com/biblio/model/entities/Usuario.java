package com.biblio.model.entities;

public class Usuario {
    private Integer id;
    private String nombre;
    private String contrasena;
    private boolean activo;

    public Usuario() {}
    public Usuario(Integer id, String nombre, String contrasena, boolean activo) {
        this.id = id; this.nombre = nombre; this.contrasena = contrasena; this.activo = activo;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
