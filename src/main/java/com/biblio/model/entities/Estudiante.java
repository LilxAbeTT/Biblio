package com.biblio.model.entities;

public class Estudiante {
    private Integer id;
    private String numControl;
    private String nombre;
    private String genero;
    private String carrera;

    public Estudiante() {}
    public Estudiante(Integer id, String numControl, String nombre, String genero, String carrera) {
        this.id = id; this.numControl = numControl; this.nombre = nombre; this.genero = genero; this.carrera = carrera;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumControl() { return numControl; }
    public void setNumControl(String numControl) { this.numControl = numControl; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
}
