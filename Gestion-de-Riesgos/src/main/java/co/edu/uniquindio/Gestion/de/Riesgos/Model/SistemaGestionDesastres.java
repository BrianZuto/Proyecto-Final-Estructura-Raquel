// ...existing code...
package co.edu.uniquindio.Gestion.de.Riesgos.Model;


import co.edu.uniquindio.Gestion.de.Riesgos.Enums.NivelUrgencia;
import co.edu.uniquindio.Gestion.de.Riesgos.Enums.TipoRecurso;
import co.edu.uniquindio.Gestion.de.Riesgos.Enums.TipoRuta;
import co.edu.uniquindio.Gestion.de.Riesgos.Estructuras.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase central del sistema de gestión de desastres naturales.
 * Gestiona todas las entidades principales del sistema.
 */
public class SistemaGestionDesastres {
    private List<Usuario> usuarios;
    private List<Recurso> recursos;
    private List<Evacuacion> evacuaciones;
    private List<Ruta> rutas;
    private List<Zona> zonas;
    private List<EquipoRescate> equipos;
    private GrafoDirigido grafoDirigido;
    private ColaPrioridad colaPrioridad;
    private MapaRecursos mapaRecursos;
    private ArbolDistribucion arbolDistribucion;
    
    public SistemaGestionDesastres() {
        this.usuarios = new ArrayList<>();
        this.recursos = new ArrayList<>();
        this.evacuaciones = new ArrayList<>();
        this.rutas = new ArrayList<>();
        this.zonas = new ArrayList<>();
        this.equipos = new ArrayList<>();
        this.grafoDirigido = new GrafoDirigido();
        this.colaPrioridad = new ColaPrioridad();
        this.mapaRecursos = new MapaRecursos();
        this.arbolDistribucion = new ArbolDistribucion();
    }
    
    /**
     * Inicializa el sistema con datos básicos
     */
    public void inicializarSistema() {
        System.out.println("Inicializando Sistema de Gestión de Desastres...");
        
        // Inicializar estructuras de datos
        grafoDirigido = new GrafoDirigido();
        colaPrioridad = new ColaPrioridad();
        mapaRecursos = new MapaRecursos();
        arbolDistribucion = new ArbolDistribucion();
        
        System.out.println("Sistema inicializado correctamente");
    }
    
    /**
     * Agrega un usuario al sistema
     */
    public boolean agregarUsuario(Usuario usuario) {
        if (usuario != null && !usuarios.contains(usuario)) {
            usuarios.add(usuario);
            return true;
        }
        return false;
    }
    
    /**
     * Agrega un recurso al sistema
     * Si el recurso tiene una ubicación (zona), busca rutas asociadas y las agrega al mapa
     */
    public boolean agregarRecurso(Recurso recurso) {
        if (recurso != null && !recursos.contains(recurso)) {
            recursos.add(recurso);
            
            // Si el recurso tiene una ubicación, buscar rutas asociadas y agregarlas al mapa
            if (recurso.getUbicacionId() != null && !recurso.getUbicacionId().isEmpty()) {
                // Buscar todas las rutas que tienen esta zona como origen o destino
                List<Ruta> rutasAsociadas = rutas.stream()
                    .filter(ruta -> (ruta.getOrigen() != null && ruta.getOrigen().getId().equals(recurso.getUbicacionId())) ||
                                   (ruta.getDestino() != null && ruta.getDestino().getId().equals(recurso.getUbicacionId())))
                    .toList();
                
                // Agregar el recurso a todas las rutas asociadas
                for (Ruta ruta : rutasAsociadas) {
                    asignarRecursoARuta(recurso, ruta);
                }
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Asigna un recurso a una ruta específica
     */
    public boolean asignarRecursoARuta(Recurso recurso, Ruta ruta) {
        if (recurso != null && ruta != null) {
            mapaRecursos.agregarRecurso(recurso, ruta);
            return true;
        }
        return false;
    }
    
    /**
     * Agrega una evacuación al sistema
     */
    public boolean agregarEvacuacion(Evacuacion evacuacion) {
        if (evacuacion != null && !evacuaciones.contains(evacuacion)) {
            evacuaciones.add(evacuacion);
            colaPrioridad.agregarEvacuacion(evacuacion);
            return true;
        }
        return false;
    }
    
    /**
     * Agrega una ruta al sistema
     */
    public boolean agregarRuta(Ruta ruta) {
        if (ruta != null && !rutas.contains(ruta)) {
            rutas.add(ruta);
            grafoDirigido.agregarArista(ruta);
            return true;
        }
        return false;
    }
    
    /**
     * Agrega una zona al sistema
     */
    public boolean agregarZona(Zona zona) {
        if (zona != null && !zonas.contains(zona)) {
            zonas.add(zona);
            return true;
        }
        return false;
    }
    
    /**
     * Agrega un equipo de rescate al sistema
     */
    public boolean agregarEquipo(EquipoRescate equipo) {
        if (equipo != null && !equipos.contains(equipo)) {
            equipos.add(equipo);
            return true;
        }
        return false;
    }
    
    /**
     * Obtiene estadísticas generales del sistema
     */
    public String obtenerEstadisticasGenerales() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADÍSTICAS DEL SISTEMA ===\n");
        stats.append("Usuarios registrados: ").append(usuarios.size()).append("\n");
        stats.append("Recursos disponibles: ").append(recursos.size()).append("\n");
        stats.append("Evacuaciones activas: ").append(evacuaciones.size()).append("\n");
        stats.append("Rutas definidas: ").append(rutas.size()).append("\n");
        stats.append("Zonas monitoreadas: ").append(zonas.size()).append("\n");
        stats.append("Equipos de rescate: ").append(equipos.size()).append("\n");
        stats.append("\n").append(grafoDirigido.generarEstadisticas());
        stats.append("\n").append(colaPrioridad.generarEstadisticas());
        
        return stats.toString();
    }

    /**
     * Crea una Ruta entre dos zonas y la agrega al sistema y al grafo.
     */
    public Ruta conectarZonas(String idRuta, String idOrigen, String idDestino,
                              double distancia, double tiempo,
                              TipoRuta tipo) {
        Zona origen = zonas.stream().filter(z -> z.getId().equals(idOrigen)).findFirst().orElse(null);
        Zona destino = zonas.stream().filter(z -> z.getId().equals(idDestino)).findFirst().orElse(null);
        if (origen == null || destino == null) return null;

        Ruta r =
                new Ruta(idRuta, origen, destino, distancia, tiempo, tipo);
        if (agregarRuta(r)) {
            return r;
        }
        return null;
    }

    /**
     *  Algorítmo de DIJSKTRA
     */

    /**
     * Retorna la secuencia de Zonas que conforma la ruta más corta (por distancia).
     */
    public java.util.List<Zona> calcularRutaMasCorta(String idOrigen, String idDestino) {
        Zona o = zonas.stream().filter(z -> z.getId().equals(idOrigen)).findFirst().orElse(null);
        Zona d = zonas.stream().filter(z -> z.getId().equals(idDestino)).findFirst().orElse(null);
        if (o == null || d == null) return java.util.List.of();
        return grafoDirigido.calcularRutaMasCorta(o, d);
    }

    /**
     * Retorna la mejor ruta por tiempo estimado entre dos zonas (si existe).
     */
    public Ruta calcularRutaMasRapida(String idOrigen, String idDestino) {
        Zona o = zonas.stream().filter(z -> z.getId().equals(idOrigen)).findFirst().orElse(null);
        Zona d = zonas.stream().filter(z -> z.getId().equals(idDestino)).findFirst().orElse(null);
        if (o == null || d == null) return null;
        return grafoDirigido.calcularRutaMasRapida(o, d);
    }


    /**
     * Simula el funcionamiento del sistema
     */
    public void simularSistema() {
        System.out.println("=== SIMULACIÓN DEL SISTEMA ===");
        
        // Simular rutas
        grafoDirigido.simularRutas();
        
        // Mostrar estadísticas
        System.out.println(obtenerEstadisticasGenerales());
    }

    /**
     *  Busquedas por ID
     */

    /** Busca una zona por ID (o null si no existe). */
    public Zona buscarZona(String id) {
        if (id == null) return null;
        for (Zona z : zonas) if (id.equals(z.getId())) return z;
        return null;
    }

    /** Busca un recurso por ID (o null si no existe). */
    public Recurso buscarRecurso(String id) {
        if (id == null) return null;
        for (Recurso r : recursos) if (id.equals(r.getId())) return r;
        return null;
    }

    /** Busca un equipo por ID (o null si no existe). */
    public EquipoRescate buscarEquipo(String id) {
        if (id == null) return null;
        for (EquipoRescate e : equipos) if (id.equals(e.getId())) return e;
        return null;
    }

    /** Busca una ruta por ID (o null si no existe). */
    public Ruta buscarRuta(String id) {
        if (id == null) return null;
        for (Ruta r : rutas) if (id.equals(r.getId())) return r;
        return null;
    }

    /**
     * Devuelve la ruta con menor nivel de riesgo entre dos zonas (si existe).
     */
    public Ruta calcularRutaMasSegura(String idOrigen, String idDestino) {
        Zona o = zonas.stream().filter(z -> z.getId().equals(idOrigen)).findFirst().orElse(null);
        Zona d = zonas.stream().filter(z -> z.getId().equals(idDestino)).findFirst().orElse(null);
        if (o == null || d == null) return null;
        return grafoDirigido.calcularRutaMasSegura(o, d);
    }
    /**
     * Asigna (mueve) una cantidad de un recurso a una zona.
     * Disminuye el disponible del recurso origen (reservar) y registra una copia asociada a la zona.
     *
     * @return true si la operación se realizó; false si no hay stock o datos inválidos.
     */
    public boolean asignarRecursoAZona(String recursoId, String zonaDestinoId, int cantidad) {
        Recurso r = buscarRecurso(recursoId);
        Zona z = buscarZona(zonaDestinoId);
        if (r == null || z == null || cantidad <= 0) return false;

        if (!r.reservar(cantidad)) return false;

        // Crea un “recurso en zona” para que puedas listarlo por ubicación (opcional pero útil)
        Recurso copia = new Recurso(
                r.getId() + "-Z" + z.getId(),
                r.getNombre(),
                r.getTipo(),
                cantidad,
                r.getUnidadMedida(),
                z.getId()
        );
        recursos.add(copia);
        return true;
    }
    /** Resumen total de recursos disponibles agrupado por tipo. */
    public java.util.Map<TipoRecurso, Integer> resumenRecursosPorTipo() {
        java.util.Map<TipoRecurso, Integer> tot = new java.util.HashMap<>();
        for (Recurso r : recursos) {
            tot.merge(r.getTipo(), r.getCantidadDisponible(), Integer::sum);
        }
        return tot;
    }
    /**
     * Traslada un equipo a la zona indicada y lo deja DISPONIBLE.
     */
    public boolean asignarEquipoAZona(String equipoId, String zonaId) {
        EquipoRescate eq = buscarEquipo(equipoId);
        Zona z = buscarZona(zonaId);
        if (eq == null || z == null) return false;

        eq.actualizarUbicacion(z.getId());
        eq.setEstado(EquipoRescate.EstadoEquipo.DISPONIBLE);
        return true;
    }
    /**
     * Planea una evacuación entre dos zonas:
     * - Usa la ruta más rápida si existe; si no, registra origen/destino sin ruta.
     * - Encola la evacuación en la Cola de Prioridad.
     */
    public Evacuacion planificarEvacuacionEntreZonas(
            String idEvac,
            String idZonaOrigen,
            String idZonaDestino,
            int personasAEvacuar,
            NivelUrgencia urgencia,
            String responsable) {

        Zona origen = buscarZona(idZonaOrigen);
        Zona destino = buscarZona(idZonaDestino);
        if (origen == null || destino == null || personasAEvacuar <= 0) return null;

        Ruta rutaElegida = calcularRutaMasRapida(idZonaOrigen, idZonaDestino);

        Evacuacion ev = new Evacuacion();
        ev.setId(idEvac);
        ev.setNombre("Evacuación " + idEvac);
        ev.setNivelUrgencia(urgencia != null ? urgencia : origen.getNivelUrgencia());
        ev.setPersonasAEvacuar(personasAEvacuar);
        ev.setResponsable(responsable);

        if (rutaElegida != null) {
            ev.setRuta(rutaElegida);
        } else {
            ev.setZonaOrigen(idZonaOrigen);
            ev.setZonaDestino(idZonaDestino);
        }

        if (agregarEvacuacion(ev)) {
            colaPrioridad.priorizar();
            return ev;
        }
        return null;
    }

    /**
     * Procesar y completar evacuaciones
     * @return
     */
    /** Toma la evacuación más prioritaria y la pasa a EN_PROGRESO. */
    public Evacuacion procesarSiguienteEvacuacion() {
        Evacuacion e = colaPrioridad.obtenerSiguienteEvacuacion();
        if (e != null) e.setEstado(Evacuacion.EstadoEvacuacion.EN_PROGRESO);
        return e;
    }

    /**
     * Completa una evacuación:
     * - Marca COMPLETADA y fija progreso.
     * - Ajusta población afectada en origen/destino.
     */
    public void completarEvacuacion(Evacuacion ev, int personasEvacuadas) {
        if (ev == null) return;

        // asegurar que no bajemos el progreso
        int nuevo = Math.max(ev.getPersonasEvacuadas(), personasEvacuadas);
        ev.actualizarProgreso(nuevo);
        ev.setEstado(Evacuacion.EstadoEvacuacion.COMPLETADA);

        // Determinar ids de origen/destino
        String idO = ev.getZonaOrigen() != null ? ev.getZonaOrigen()
                : (ev.getRuta() != null ? ev.getRuta().getOrigen().getId() : null);
        String idD = ev.getZonaDestino() != null ? ev.getZonaDestino()
                : (ev.getRuta() != null ? ev.getRuta().getDestino().getId() : null);

        Zona origen = buscarZona(idO);
        Zona destino = buscarZona(idD);

        int mov = ev.getPersonasEvacuadas();
        if (origen != null) origen.setPoblacionAfectada(Math.max(0, origen.getPoblacionAfectada() - mov));
        if (destino != null) destino.setPoblacionAfectada(destino.getPoblacionAfectada() + mov);
    }

    /** Limpia de la cola evacuaciones COMPLETADAS o CANCELADAS. */
    public void limpiarEvacuacionesCompletadas() {
        colaPrioridad.limpiarCompletadas();
    }
    /**
     * Devuelve las N zonas más críticas ordenadas por nivel de riesgo y población afectada.
     */
    public java.util.List<Zona> topZonasCriticas(int n) {
        return zonas.stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getNivelRiesgo().getValor(), a.getNivelRiesgo().getValor());
                    if (cmp != 0) return cmp;
                    return Integer.compare(b.getPoblacionAfectada(), a.getPoblacionAfectada());
                })
                .limit(Math.max(0, n))
                .toList();
    }

    /**
     * Reporte general
     * @return
     */
    /**
     * Reporte general integrando estructuras para mostrar en la pestaña “Estadísticas”.
     * No reemplaza a obtenerEstadisticasGenerales(); es una versión extendida.
     */
    public String generarReporteGeneral() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE GENERAL ===\n");
        sb.append("Usuarios: ").append(usuarios.size()).append("\n");
        sb.append("Zonas: ").append(zonas.size()).append("\n");
        sb.append("Rutas: ").append(rutas.size()).append("\n");
        sb.append("Recursos: ").append(recursos.size()).append("\n");
        sb.append("Equipos: ").append(equipos.size()).append("\n");
        sb.append("Evacuaciones: ").append(evacuaciones.size()).append("\n\n");

        sb.append(grafoDirigido.generarEstadisticas()).append("\n\n");
        sb.append(colaPrioridad.generarEstadisticas()).append("\n\n");
        sb.append(mapaRecursos.generarEstadisticas()).append("\n");

        return sb.toString();
    }

    // --- Delegaciones / Wrappers seguros hacia otras estructuras ---
    // MapaRecursos (delegaciones limitadas a métodos comúnmente presentes)
    public List<Recurso> obtenerRecursosPorRuta(Ruta ruta) {
        if (mapaRecursos == null || ruta == null) return new ArrayList<>();
        return mapaRecursos.obtenerRecursos(ruta);
    }

    public List<Ruta> obtenerRutasPorRecurso(Recurso recurso) {
        if (mapaRecursos == null || recurso == null) return new ArrayList<>();
        return mapaRecursos.obtenerRutas(recurso);
    }

    public Recurso obtenerRecursoMapa(String recursoId) {
        if (mapaRecursos == null || recursoId == null) return null;
        return mapaRecursos.obtenerRecurso(recursoId);
    }

    public List<Recurso> obtenerTodosLosRecursosMapa() {
        if (mapaRecursos == null) return new ArrayList<>();
        return mapaRecursos.obtenerTodosLosRecursos();
    }

    public List<Recurso> obtenerRecursosPorTipoMapa(TipoRecurso tipo) {
        if (mapaRecursos == null || tipo == null) return new ArrayList<>();
        return mapaRecursos.obtenerRecursosPorTipo(tipo);
    }

    // GrafoDirigido
    public void agregarNodoAlGrafo(Nodo nodo) {
        if (grafoDirigido != null && nodo != null) grafoDirigido.agregarNodo(nodo);
    }

    public Nodo obtenerNodoGrafo(String id) {
        if (grafoDirigido == null || id == null) return null;
        return grafoDirigido.obtenerNodo(id);
    }

    public List<Ruta> obtenerRutasDesdeGrafo(String idOrigen) {
        if (grafoDirigido == null || idOrigen == null) return new ArrayList<>();
        return grafoDirigido.obtenerRutasDesde(idOrigen);
    }

    public List<Ruta> obtenerRutasHaciaGrafo(String idDestino) {
        if (grafoDirigido == null || idDestino == null) return new ArrayList<>();
        return grafoDirigido.obtenerRutasHacia(idDestino);
    }

    public boolean existeRutaEnGrafo(String idOrigen, String idDestino) {
        if (grafoDirigido == null || idOrigen == null || idDestino == null) return false;
        return grafoDirigido.existeRuta(idOrigen, idDestino);
    }

    // ColaPrioridad
    public Evacuacion verSiguienteEvacuacionCola() {
        if (colaPrioridad == null) return null;
        return colaPrioridad.verSiguienteEvacuacion();
    }

    public boolean estaVaciaCola() {
        return colaPrioridad == null || colaPrioridad.estaVacia();
    }

    public int obtenerTamanoCola() {
        return colaPrioridad == null ? 0 : colaPrioridad.obtenerTamano();
    }

    public List<Evacuacion> obtenerTodasEvacuacionesCola() {
        if (colaPrioridad == null) return new ArrayList<>();
        return colaPrioridad.obtenerTodasLasEvacuaciones();
    }

    public List<Evacuacion> obtenerHistorialEvacuaciones() {
        if (colaPrioridad == null) return new ArrayList<>();
        return colaPrioridad.obtenerHistorial();
    }

    public void priorizarCola() {
        if (colaPrioridad != null) colaPrioridad.priorizar();
    }

    // ArbolDistribucion
    public void crearNodoRaizArbol(Recurso recurso, int cantidad) {
        if (arbolDistribucion != null) arbolDistribucion.crearNodoRaiz(recurso, cantidad);
    }

    public void agregarNodoArbol(String id, Recurso recurso, int cantidad, String idPadre) {
        if (arbolDistribucion != null) arbolDistribucion.agregarNodo(id, recurso, cantidad, idPadre);
    }

    public ArbolDistribucion.NodoDistribucion buscarNodoArbol(String id) {
        if (arbolDistribucion == null || id == null) return null;
        return arbolDistribucion.buscarNodo(id);
    }

    public boolean arbolTieneRecursosSuficientes(int cantidadRequerida) {
        if (arbolDistribucion == null) return false;
        return arbolDistribucion.tieneRecursosSuficientes(cantidadRequerida);
    }

    public int calcularCantidadTotalEnArbol() {
        if (arbolDistribucion == null) return 0;
        return arbolDistribucion.calcularCantidadTotal();
    }

    // Helpers / utilidades
    public Usuario buscarUsuarioPorId(String id) {
        if (id == null) return null;
        for (Usuario u : usuarios) if (id.equals(u.getId())) return u;
        return null;
    }

    public boolean eliminarUsuarioPorId(String id) {
        Usuario u = buscarUsuarioPorId(id);
        if (u != null) return usuarios.remove(u);
        return false;
    }

    public void iniciarEvacuacion(Evacuacion ev) {
        if (ev != null) ev.iniciarEvacuacion();
    }

    public void cancelarEvacuacion(Evacuacion ev) {
        if (ev != null) ev.cancelarEvacuacion();
    }

    public void suspenderEvacuacion(Evacuacion ev) {
        if (ev != null) ev.suspenderEvacuacion();
    }

    public void reanudarEvacuacion(Evacuacion ev) {
        if (ev != null) ev.reanudarEvacuacion();
    }

    // ...existing code...
    // Getters
    public List<Usuario> getUsuarios() {
        return new ArrayList<>(usuarios);
    }
    
    public List<Recurso> getRecursos() {
        return new ArrayList<>(recursos);
    }
    
    public List<Evacuacion> getEvacuaciones() {
        return new ArrayList<>(evacuaciones);
    }
    
    public List<Ruta> getRutas() {
        return new ArrayList<>(rutas);
    }
    
    public List<Zona> getZonas() {
        return new ArrayList<>(zonas);
    }
    
    public List<EquipoRescate> getEquipos() {
        return new ArrayList<>(equipos);
    }
    
    public GrafoDirigido getGrafoDirigido() {
        return grafoDirigido;
    }
    
    public ColaPrioridad getColaPrioridad() {
        return colaPrioridad;
    }
    
    public MapaRecursos getMapaRecursos() {
        return mapaRecursos;
    }
    
    public ArbolDistribucion getArbolDistribucion() {
        return arbolDistribucion;
    }
    
    @Override
    public String toString() {
        return String.format("SistemaGestionDesastres{usuarios=%d, recursos=%d, evacuaciones=%d, rutas=%d, zonas=%d, equipos=%d}", 
            usuarios.size(), recursos.size(), evacuaciones.size(), rutas.size(), zonas.size(), equipos.size());
    }
}
// ...existing code...