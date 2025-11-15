package co.edu.uniquindio.Gestion.de.Riesgos.Model;

import co.edu.uniquindio.Gestion.de.Riesgos.Enums.NivelUrgencia;

import java.util.Objects;

/**
 * Clase que representa una zona en el sistema de gestión de desastres
 * Una zona es una área geográfica que puede estar afectada por desastres
 */
public class Zona {
    private String id;
    private NivelUrgencia nivelRiesgo;
    private String nombre;
    private String descripcion;
    private double coordenadaX;
    private double coordenadaY;
    private int poblacionAfectada;
    private boolean activa;
    private int radio; // Radio de cobertura en metros
    
    public Zona() {
        this.activa = true;
        this.poblacionAfectada = 0;
        this.nivelRiesgo = NivelUrgencia.BAJA;
        this.radio = 500; // Radio por defecto: 500 metros
    }
    
    public Zona(String id, String nombre, NivelUrgencia nivelRiesgo) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.nivelRiesgo = nivelRiesgo;
    }
    
    /**
     * Verifica si la zona está en estado crítico
     */
    public boolean estaEnEstadoCritico() {
        return nivelRiesgo == NivelUrgencia.CRITICA || 
               (nivelRiesgo == NivelUrgencia.ALTA && poblacionAfectada > 1000);
    }
    
    /**
     * Actualiza el nivel de riesgo basado en la población afectada
     */
    public void actualizarNivelRiesgo() {
        if (poblacionAfectada > 5000) {
            this.nivelRiesgo = NivelUrgencia.CRITICA;
        } else if (poblacionAfectada > 2000) {
            this.nivelRiesgo = NivelUrgencia.ALTA;
        } else if (poblacionAfectada > 500) {
            this.nivelRiesgo = NivelUrgencia.MEDIA;
        } else {
            this.nivelRiesgo = NivelUrgencia.BAJA;
        }
    }
    
    /**
     * Calcula la prioridad de evacuación de la zona
     */
    public int calcularPrioridadEvacuacion() {
        int prioridad = nivelRiesgo.getValor();
        
        if (poblacionAfectada > 10000) prioridad += 3;
        else if (poblacionAfectada > 5000) prioridad += 2;
        else if (poblacionAfectada > 1000) prioridad += 1;
        
        return prioridad;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public NivelUrgencia getNivelRiesgo() {
        return nivelRiesgo;
    }
    
    public void setNivelRiesgo(NivelUrgencia nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }
    
    /**
     * Alias para getNivelRiesgo() para compatibilidad con ColaPrioridad
     */
    public NivelUrgencia getNivelUrgencia() {
        return nivelRiesgo;
    }
    
    /**
     * Alias para setNivelRiesgo() para compatibilidad con ColaPrioridad
     */
    public void setNivelUrgencia(NivelUrgencia nivelUrgencia) {
        this.nivelRiesgo = nivelUrgencia;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public double getCoordenadaX() {
        return coordenadaX;
    }
    
    public void setCoordenadaX(double coordenadaX) {
        this.coordenadaX = coordenadaX;
    }
    
    public double getCoordenadaY() {
        return coordenadaY;
    }
    
    public void setCoordenadaY(double coordenadaY) {
        this.coordenadaY = coordenadaY;
    }
    
    public int getPoblacionAfectada() {
        return poblacionAfectada;
    }
    
    public void setPoblacionAfectada(int poblacionAfectada) {
        this.poblacionAfectada = Math.max(0, poblacionAfectada);
        // NO actualizar automáticamente el nivel de riesgo
        // para respetar el nivel establecido por el usuario
    }
    
    public boolean isActiva() {
        return activa;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    public int getRadio() {
        return radio;
    }
    
    public void setRadio(int radio) {
        this.radio = Math.max(100, Math.min(10000, radio)); // Entre 100m y 10km
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zona zona = (Zona) o;
        return Objects.equals(id, zona.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Zona{id='%s', nombre='%s', nivelRiesgo=%s, poblacionAfectada=%d, radio=%dm}", 
            id, nombre, nivelRiesgo.getDescripcion(), poblacionAfectada, radio);
    }
}