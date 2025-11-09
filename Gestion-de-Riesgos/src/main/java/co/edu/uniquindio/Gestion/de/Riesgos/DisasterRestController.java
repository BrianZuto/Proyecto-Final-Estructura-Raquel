// Archivo: src/main/java/co/edu/uniquindio/Gestion/de/Riesgos/Controller/DisasterRestController.java
package co.edu.uniquindio.Gestion.de.Riesgos;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.Gestion.de.Riesgos.Enums.NivelUrgencia;
import co.edu.uniquindio.Gestion.de.Riesgos.Enums.TipoRecurso;
import co.edu.uniquindio.Gestion.de.Riesgos.Enums.TipoRuta;
import co.edu.uniquindio.Gestion.de.Riesgos.Estructuras.Nodo;
import co.edu.uniquindio.Gestion.de.Riesgos.Estructuras.Ruta;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.Administrador;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.EquipoRescate;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.Evacuacion;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.OperadorEmergencia;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.Recurso;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.SistemaGestionDesastres;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.Usuario;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.Zona;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DisasterRestController {

    private static SistemaGestionDesastres sistema;

    static {
        sistema = new SistemaGestionDesastres();
        sistema.inicializarSistema();
        inicializarDatosPrueba();
    }

    // ============ ENDPOINTS DE HEALTH CHECK ============

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "API funcionando correctamente",
                "timestamp", System.currentTimeMillis()
        ));
    }

    // ============ ENDPOINTS DE AUTENTICACIÓN ============

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Usuario usuario = sistema.getUsuarios().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (usuario != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.obtenerNombreCompleto(),
                    "rol", usuario.getRol().getDescripcion(),
                    "email", usuario.getEmail()
            ));
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Credenciales inválidas"));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userData) {
        try {
            // Validar que el username no exista
            boolean usernameExists = sistema.getUsuarios().stream()
                    .anyMatch(u -> u.getUsername().equals(userData.get("username")));

            if (usernameExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "El nombre de usuario ya existe"));
            }

            // Validar que el email no exista
            boolean emailExists = sistema.getUsuarios().stream()
                    .anyMatch(u -> u.getEmail().equals(userData.get("email")));

            if (emailExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "El correo electrónico ya está registrado"));
            }

            String id = "U" + String.format("%03d", sistema.getUsuarios().size() + 1);
            String rol = userData.getOrDefault("rol", "OPERADOR_EMERGENCIA");

            Usuario nuevoUsuario;

            if ("ADMINISTRADOR".equals(rol)) {
                nuevoUsuario = new Administrador(
                        id,
                        userData.get("nombre"),
                        userData.get("apellido"),
                        userData.get("email"),
                        userData.get("username"),
                        userData.get("password"),
                        userData.getOrDefault("departamento", "General")
                );
            } else {
                nuevoUsuario = new OperadorEmergencia(
                        id,
                        userData.get("nombre"),
                        userData.get("apellido"),
                        userData.get("email"),
                        userData.get("username"),
                        userData.get("password"),
                        userData.getOrDefault("especialidad", "Emergencias"),
                        userData.getOrDefault("ubicacion", "Sin asignar")
                );
            }

            sistema.agregarUsuario(nuevoUsuario);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuario registrado exitosamente",
                    "userId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Error al registrar usuario: " + e.getMessage()));
        }
    }

    // ============ ENDPOINTS DE ZONAS ============

    @GetMapping("/zonas")
    public ResponseEntity<List<Map<String, Object>>> obtenerZonas() {
        List<Map<String, Object>> zonasData = sistema.getZonas().stream()
                .map(zona -> {
                    Map<String, Object> zonaMap = new HashMap<>();
                    zonaMap.put("id", zona.getId());
                    zonaMap.put("nombre", zona.getNombre());
                    zonaMap.put("descripcion", zona.getDescripcion());
                    zonaMap.put("coordenadaX", zona.getCoordenadaX());
                    zonaMap.put("coordenadaY", zona.getCoordenadaY());
                    zonaMap.put("poblacionAfectada", zona.getPoblacionAfectada());
                    zonaMap.put("nivelUrgencia", zona.getNivelUrgencia().getDescripcion());
                    zonaMap.put("nivelUrgenciaValor", zona.getNivelUrgencia().getValor());
                    zonaMap.put("color", zona.getNivelUrgencia().getColor());
                    zonaMap.put("activa", zona.isActiva());
                    return zonaMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(zonasData);
    }

    @GetMapping("/zonas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerZonaPorId(@PathVariable String id) {
        Zona zona = sistema.buscarZona(id);
        if (zona != null) {
            Map<String, Object> zonaMap = new HashMap<>();
            zonaMap.put("id", zona.getId());
            zonaMap.put("nombre", zona.getNombre());
            zonaMap.put("descripcion", zona.getDescripcion());
            zonaMap.put("coordenadaX", zona.getCoordenadaX());
            zonaMap.put("coordenadaY", zona.getCoordenadaY());
            zonaMap.put("poblacionAfectada", zona.getPoblacionAfectada());
            zonaMap.put("nivelUrgencia", zona.getNivelUrgencia().getDescripcion());
            return ResponseEntity.ok(zonaMap);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/zonas")
    public ResponseEntity<Map<String, Object>> crearZona(@RequestBody Map<String, Object> zonaData) {
        try {
            Zona zona = new Zona(
                    (String) zonaData.get("id"),
                    (String) zonaData.get("nombre"),
                    NivelUrgencia.valueOf((String) zonaData.get("nivelUrgencia"))
            );
            zona.setDescripcion((String) zonaData.get("descripcion"));
            zona.setCoordenadaX(((Number) zonaData.get("coordenadaX")).doubleValue());
            zona.setCoordenadaY(((Number) zonaData.get("coordenadaY")).doubleValue());
            zona.setPoblacionAfectada(((Number) zonaData.get("poblacionAfectada")).intValue());

            sistema.agregarZona(zona);

            return ResponseEntity.ok(Map.of("success", true, "message", "Zona creada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ============ ENDPOINTS DE RECURSOS ============

    @GetMapping("/recursos")
    public ResponseEntity<List<Map<String, Object>>> obtenerRecursos() {
        List<Map<String, Object>> recursosData = sistema.getRecursos().stream()
                .map(recurso -> {
                    Map<String, Object> recursoMap = new HashMap<>();
                    recursoMap.put("id", recurso.getId());
                    recursoMap.put("nombre", recurso.getNombre());
                    recursoMap.put("tipo", recurso.getTipo().getDescripcion());
                    recursoMap.put("cantidad", recurso.getCantidad());
                    recursoMap.put("cantidadDisponible", recurso.getCantidadDisponible());
                    recursoMap.put("unidadMedida", recurso.getUnidadMedida());
                    recursoMap.put("ubicacionId", recurso.getUbicacionId());
                    recursoMap.put("estado", recurso.getEstado().getDescripcion());
                    recursoMap.put("porcentajeDisponible", recurso.calcularPorcentajeDisponible());
                    return recursoMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(recursosData);
    }

    @GetMapping("/recursos/ubicacion/{ubicacionId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRecursosPorUbicacion(@PathVariable String ubicacionId) {
        List<Map<String, Object>> recursosData = sistema.getRecursos().stream()
                .filter(r -> ubicacionId.equals(r.getUbicacionId()))
                .map(recurso -> {
                    Map<String, Object> recursoMap = new HashMap<>();
                    recursoMap.put("id", recurso.getId());
                    recursoMap.put("nombre", recurso.getNombre());
                    recursoMap.put("tipo", recurso.getTipo().getDescripcion());
                    recursoMap.put("cantidad", recurso.getCantidad());
                    recursoMap.put("cantidadDisponible", recurso.getCantidadDisponible());
                    recursoMap.put("unidadMedida", recurso.getUnidadMedida());
                    return recursoMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(recursosData);
    }

    @PostMapping("/recursos")
    public ResponseEntity<Map<String, Object>> crearRecurso(@RequestBody Map<String, Object> recursoData) {
        try {
            Recurso recurso = new Recurso(
                    (String) recursoData.get("id"),
                    (String) recursoData.get("nombre"),
                    TipoRecurso.valueOf((String) recursoData.get("tipo")),
                    ((Number) recursoData.get("cantidad")).intValue(),
                    (String) recursoData.get("unidadMedida"),
                    (String) recursoData.get("ubicacionId")
            );

            sistema.agregarRecurso(recurso);

            return ResponseEntity.ok(Map.of("success", true, "message", "Recurso creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ============ ENDPOINTS DE EQUIPOS ============

    @GetMapping("/equipos")
    public ResponseEntity<List<Map<String, Object>>> obtenerEquipos() {
        List<Map<String, Object>> equiposData = sistema.getEquipos().stream()
                .map(equipo -> {
                    Map<String, Object> equipoMap = new HashMap<>();
                    equipoMap.put("id", equipo.getId());
                    equipoMap.put("nombre", equipo.getNombre());
                    equipoMap.put("tipo", equipo.getTipo().getDescripcion());
                    equipoMap.put("ubicacionActual", equipo.getUbicacionActual());
                    equipoMap.put("estado", equipo.getEstado().getDescripcion());
                    equipoMap.put("capacidadMaxima", equipo.getCapacidadMaxima());
                    equipoMap.put("personalAsignado", equipo.getPersonalAsignado());
                    equipoMap.put("liderEquipo", equipo.getLiderEquipo());
                    equipoMap.put("disponible", equipo.isDisponible());
                    equipoMap.put("experienciaAnos", equipo.getExperienciaAnos());
                    equipoMap.put("eficiencia", equipo.calcularEficiencia() * 100);
                    return equipoMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(equiposData);
    }

    @PostMapping("/equipos")
    public ResponseEntity<Map<String, Object>> crearEquipo(@RequestBody Map<String, Object> equipoData) {
        try {
            EquipoRescate equipo = new EquipoRescate(
                    (String) equipoData.get("id"),
                    (String) equipoData.get("nombre"),
                    EquipoRescate.TipoEquipo.valueOf((String) equipoData.get("tipo")),
                    (String) equipoData.get("ubicacionActual"),
                    ((Number) equipoData.get("capacidadMaxima")).intValue(),
                    (String) equipoData.get("liderEquipo")
            );

            if (equipoData.containsKey("personalAsignado")) {
                equipo.setPersonalAsignado(((Number) equipoData.get("personalAsignado")).intValue());
            }
            if (equipoData.containsKey("experienciaAnos")) {
                equipo.setExperienciaAnos(((Number) equipoData.get("experienciaAnos")).intValue());
            }

            sistema.agregarEquipo(equipo);

            return ResponseEntity.ok(Map.of("success", true, "message", "Equipo creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ============ ENDPOINTS DE EVACUACIONES ============

    @GetMapping("/evacuaciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerEvacuaciones() {
        List<Map<String, Object>> evacuacionesData = sistema.getEvacuaciones().stream()
                .map(evacuacion -> {
                    Map<String, Object> evacuacionMap = new HashMap<>();
                    evacuacionMap.put("id", evacuacion.getId());
                    evacuacionMap.put("nombre", evacuacion.getNombre());
                    evacuacionMap.put("nivelUrgencia", evacuacion.getNivelUrgencia().getDescripcion());
                    evacuacionMap.put("personasAEvacuar", evacuacion.getPersonasAEvacuar());
                    evacuacionMap.put("personasEvacuadas", evacuacion.getPersonasEvacuadas());
                    evacuacionMap.put("estado", evacuacion.getEstado().getDescripcion());
                    evacuacionMap.put("responsable", evacuacion.getResponsable());
                    evacuacionMap.put("porcentajeCompletado", evacuacion.calcularPorcentajeCompletado());
                    evacuacionMap.put("prioridad", evacuacion.calcularPrioridad());
                    return evacuacionMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(evacuacionesData);
    }

    @PostMapping("/evacuaciones")
    public ResponseEntity<Map<String, Object>> planificarEvacuacion(@RequestBody Map<String, Object> evacuacionData) {
        try {
            Evacuacion evacuacion = sistema.planificarEvacuacionEntreZonas(
                    (String) evacuacionData.get("id"),
                    (String) evacuacionData.get("zonaOrigen"),
                    (String) evacuacionData.get("zonaDestino"),
                    ((Number) evacuacionData.get("personasAEvacuar")).intValue(),
                    NivelUrgencia.valueOf((String) evacuacionData.get("nivelUrgencia")),
                    (String) evacuacionData.get("responsable")
            );

            if (evacuacion != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Evacuación planificada exitosamente"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "No se pudo planificar la evacuación"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ============ ENDPOINTS DE RUTAS ============

    @GetMapping("/rutas")
    public ResponseEntity<List<Map<String, Object>>> obtenerRutas() {
        List<Map<String, Object>> rutasData = sistema.getRutas().stream()
                .map(ruta -> {
                    Map<String, Object> rutaMap = new HashMap<>();
                    rutaMap.put("id", ruta.getId());
                    rutaMap.put("origen", Map.of(
                            "id", ruta.getOrigen().getId(),
                            "nombre", ruta.getOrigen().getNombre()
                    ));
                    rutaMap.put("destino", Map.of(
                            "id", ruta.getDestino().getId(),
                            "nombre", ruta.getDestino().getNombre()
                    ));
                    rutaMap.put("distancia", ruta.getDistancia());
                    rutaMap.put("tiempoEstimado", ruta.getTiempoEstimado());
                    rutaMap.put("tipo", ruta.getTipo().getDescripcion());
                    rutaMap.put("activa", ruta.isActiva());
                    rutaMap.put("nivelRiesgo", ruta.getNivelRiesgo());
                    rutaMap.put("capacidadMaxima", ruta.getCapacidadMaxima());
                    rutaMap.put("capacidadActual", ruta.getCapacidadActual());
                    return rutaMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(rutasData);
    }

    @PostMapping("/rutas")
    public ResponseEntity<Map<String, Object>> crearRuta(@RequestBody Map<String, Object> rutaData) {
        try {
            Ruta ruta = sistema.conectarZonas(
                    (String) rutaData.get("id"),
                    (String) rutaData.get("origenId"),
                    (String) rutaData.get("destinoId"),
                    ((Number) rutaData.get("distancia")).doubleValue(),
                    ((Number) rutaData.get("tiempoEstimado")).doubleValue(),
                    TipoRuta.valueOf((String) rutaData.get("tipo"))
            );

            if (ruta != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Ruta creada exitosamente"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "No se pudo crear la ruta"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // MapaRecursos: recursos por ruta
    @GetMapping("/mapa/recursos/ruta/{rutaId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRecursosPorRuta(@PathVariable String rutaId) {
        Ruta ruta = sistema.buscarRuta(rutaId);
        if (ruta == null) return ResponseEntity.notFound().build();

        List<Recurso> recursos = sistema.obtenerRecursosPorRuta(ruta);
        if (recursos == null) recursos = Collections.emptyList();

        List<Map<String, Object>> data = recursos.stream().map(rec -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rec.getId());
            m.put("nombre", rec.getNombre());
            m.put("tipo", rec.getTipo() != null ? rec.getTipo().getDescripcion() : null);
            m.put("cantidadDisponible", rec.getCantidadDisponible());
            m.put("unidadMedida", rec.getUnidadMedida());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(data);
    }

    // MapaRecursos: rutas por recurso
    @GetMapping("/mapa/rutas/recurso/{recursoId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRutasPorRecurso(@PathVariable String recursoId) {
        Recurso recurso = sistema.buscarRecurso(recursoId);
        if (recurso == null) return ResponseEntity.notFound().build();

        List<Ruta> rutas = sistema.obtenerRutasPorRecurso(recurso);
        if (rutas == null) rutas = Collections.emptyList();

        List<Map<String, Object>> data = rutas.stream().map(rt -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rt.getId());
            m.put("origenId", rt.getOrigen() != null ? rt.getOrigen().getId() : null);
            m.put("destinoId", rt.getDestino() != null ? rt.getDestino().getId() : null);
            m.put("distancia", rt.getDistancia());
            m.put("tiempoEstimado", rt.getTiempoEstimado());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(data);
    }

    // Grafo: obtener rutas desde un nodo
    @GetMapping("/grafo/rutas/desde/{idOrigen}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRutasDesdeGrafo(@PathVariable String idOrigen) {
        List<Ruta> rutas = sistema.obtenerRutasDesdeGrafo(idOrigen);
        if (rutas == null) rutas = Collections.emptyList();

        List<Map<String, Object>> data = rutas.stream().map(rt -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rt.getId());
            m.put("origenId", rt.getOrigen() != null ? rt.getOrigen().getId() : null);
            m.put("destinoId", rt.getDestino() != null ? rt.getDestino().getId() : null);
            m.put("distancia", rt.getDistancia());
            m.put("tiempoEstimado", rt.getTiempoEstimado());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(data);
    }

    // Grafo: obtener rutas hacia un nodo
    @GetMapping("/grafo/rutas/hasta/{idDestino}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRutasHaciaGrafo(@PathVariable String idDestino) {
        List<Ruta> rutas = sistema.obtenerRutasHaciaGrafo(idDestino);
        if (rutas == null) rutas = Collections.emptyList();

        List<Map<String, Object>> data = rutas.stream().map(rt -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rt.getId());
            m.put("origenId", rt.getOrigen() != null ? rt.getOrigen().getId() : null);
            m.put("destinoId", rt.getDestino() != null ? rt.getDestino().getId() : null);
            m.put("distancia", rt.getDistancia());
            m.put("tiempoEstimado", rt.getTiempoEstimado());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(data);
    }

    // Grafo: obtener nodo por id (retorna representación mínima)
    @GetMapping("/grafo/nodo/{id}")
    public ResponseEntity<Map<String, Object>> obtenerNodoGrafo(@PathVariable String id) {
        Nodo nodo = sistema.obtenerNodoGrafo(id);
        if (nodo == null) return ResponseEntity.notFound().build();
        Map<String, Object> nodoMap = new HashMap<>();
        nodoMap.put("id", nodo.getId());
        nodoMap.put("nombre", nodo.getNombre());
        nodoMap.put("coordenadaX", nodo.getCoordenadaX());
        nodoMap.put("coordenadaY", nodo.getCoordenadaY());
        nodoMap.put("nivelUrgencia", nodo.getNivelUrgencia() != null ? nodo.getNivelUrgencia().getDescripcion() : null);
        nodoMap.put("activo", nodo.isActivo());
        return ResponseEntity.ok(nodoMap);
    }

    // ColaPrioridad: ver / procesar / priorizar
    @GetMapping("/cola/verSiguiente")
    public ResponseEntity<Map<String, Object>> verSiguienteEvacuacion() {
        Evacuacion ev = sistema.verSiguienteEvacuacionCola();
        if (ev == null) return ResponseEntity.ok(Map.of("siguiente", null));
        return ResponseEntity.ok(Map.of(
                "id", ev.getId(),
                "nombre", ev.getNombre(),
                "nivelUrgencia", ev.getNivelUrgencia().getDescripcion(),
                "personasAEvacuar", ev.getPersonasAEvacuar(),
                "prioridad", ev.calcularPrioridad()
        ));
    }

    @PostMapping("/cola/priorizar")
    public ResponseEntity<Map<String, Object>> priorizarCola() {
        sistema.priorizarCola();
        return ResponseEntity.ok(Map.of("success", true, "message", "Cola priorizada"));
    }

    @PostMapping("/cola/procesar")
    public ResponseEntity<Map<String, Object>> procesarSiguienteEvacuacion() {
        Evacuacion ev = sistema.procesarSiguienteEvacuacion();
        if (ev == null) return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("success", false, "message", "No hay evacuaciones"));
        return ResponseEntity.ok(Map.of("success", true, "id", ev.getId(), "estado", ev.getEstado().name()));
    }

    // ArbolDistribucion: crear raiz y agregar nodo (usa recurso existente)
    @PostMapping("/arbol/crearRaiz")
    public ResponseEntity<Map<String, Object>> crearRaizArbol(@RequestBody Map<String, Object> body) {
        String recursoId = (String) body.get("recursoId");
        int cantidad = ((Number) body.getOrDefault("cantidad", 0)).intValue();
        Recurso recurso = sistema.buscarRecurso(recursoId);
        if (recurso == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Recurso no encontrado"));
        sistema.crearNodoRaizArbol(recurso, cantidad);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/arbol/agregarNodo")
    public ResponseEntity<Map<String, Object>> agregarNodoArbol(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        String recursoId = (String) body.get("recursoId");
        int cantidad = ((Number) body.getOrDefault("cantidad", 0)).intValue();
        String idPadre = (String) body.get("idPadre");
        Recurso recurso = sistema.buscarRecurso(recursoId);
        if (recurso == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Recurso no encontrado"));
        sistema.agregarNodoArbol(id, recurso, cantidad, idPadre);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/arbol/total")
    public ResponseEntity<Map<String, Object>> obtenerCantidadTotalArbol() {
        int total = sistema.calcularCantidadTotalEnArbol();
        return ResponseEntity.ok(Map.of("total", total));
    }

    // ============ ENDPOINTS DE ESTADÍSTICAS ============

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalZonas", sistema.getZonas().size());
        estadisticas.put("totalRecursos", sistema.getRecursos().size());
        estadisticas.put("totalEquipos", sistema.getEquipos().size());
        estadisticas.put("totalEvacuaciones", sistema.getEvacuaciones().size());
        estadisticas.put("totalRutas", sistema.getRutas().size());

        // Zonas por nivel de urgencia
        Map<String, Long> zonasPorUrgencia = sistema.getZonas().stream()
                .collect(Collectors.groupingBy(
                        zona -> zona.getNivelUrgencia().getDescripcion(),
                        Collectors.counting()
                ));
        estadisticas.put("zonasPorUrgencia", zonasPorUrgencia);

        // Recursos por tipo
        Map<String, Integer> recursosPorTipo = sistema.resumenRecursosPorTipo().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getDescripcion(),
                        Map.Entry::getValue
                ));
        estadisticas.put("recursosPorTipo", recursosPorTipo);

        // Equipos por estado
        Map<String, Long> equiposPorEstado = sistema.getEquipos().stream()
                .collect(Collectors.groupingBy(
                        equipo -> equipo.getEstado().getDescripcion(),
                        Collectors.counting()
                ));
        estadisticas.put("equiposPorEstado", equiposPorEstado);

        // Zonas críticas
        List<Map<String, Object>> zonasCriticas = sistema.topZonasCriticas(5).stream()
                .map(zona -> Map.of(
                        "id", (Object)zona.getId(),
                        "nombre", zona.getNombre(),
                        "poblacionAfectada", zona.getPoblacionAfectada(),
                        "nivelUrgencia", zona.getNivelUrgencia().getDescripcion()
                ))
                .collect(Collectors.toList());
        estadisticas.put("zonasCriticas", zonasCriticas);

        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Object>> obtenerReporteGeneral() {
        String reporte = sistema.generarReporteGeneral();
        return ResponseEntity.ok(Map.of("reporte", reporte));
    }

    // ============ INICIALIZACIÓN DE DATOS DE PRUEBA ============

    private static void inicializarDatosPrueba() {
        // Crear zonas
        Zona zona1 = new Zona("Z001", "Ciudad Central", NivelUrgencia.ALTA);
        zona1.setCoordenadaX(4.5389);
        zona1.setCoordenadaY(-75.6678);
        zona1.setPoblacionAfectada(15000);
        zona1.setDescripcion("Zona urbana principal afectada por terremoto");
        sistema.agregarZona(zona1);

        Zona zona2 = new Zona("Z002", "Refugio Norte", NivelUrgencia.MEDIA);
        zona2.setCoordenadaX(4.5489);
        zona2.setCoordenadaY(-75.6578);
        zona2.setPoblacionAfectada(2000);
        zona2.setDescripcion("Refugio temporal para evacuados");
        sistema.agregarZona(zona2);

        Zona zona3 = new Zona("Z003", "Hospital Principal", NivelUrgencia.CRITICA);
        zona3.setCoordenadaX(4.5289);
        zona3.setCoordenadaY(-75.6778);
        zona3.setPoblacionAfectada(500);
        zona3.setDescripcion("Hospital con heridos críticos");
        sistema.agregarZona(zona3);

        // Crear rutas
        sistema.conectarZonas("R001", "Z001", "Z002", 15.5, 0.8, TipoRuta.TERRESTRE);
        sistema.conectarZonas("R002", "Z001", "Z003", 8.2, 0.5, TipoRuta.TERRESTRE);
        sistema.conectarZonas("R003", "Z002", "Z003", 12.3, 0.7, TipoRuta.TERRESTRE);

        // Crear recursos
        Recurso recurso1 = new Recurso("R001", "Agua Potable", TipoRecurso.ALIMENTOS, 1000, "litros", "Z001");
        sistema.agregarRecurso(recurso1);

        Recurso recurso2 = new Recurso("R002", "Medicinas Básicas", TipoRecurso.MEDICINAS, 500, "unidades", "Z003");
        sistema.agregarRecurso(recurso2);

        Recurso recurso3 = new Recurso("R003", "Alimentos No Perecederos", TipoRecurso.ALIMENTOS, 2000, "kg", "Z002");
        sistema.agregarRecurso(recurso3);

        // Crear equipos
        EquipoRescate equipo1 = new EquipoRescate("EQ001", "Equipo Médico Alpha", EquipoRescate.TipoEquipo.MEDICO, "Z003", 8, "Dr. García");
        equipo1.setPersonalAsignado(6);
        equipo1.setExperienciaAnos(8);
        sistema.agregarEquipo(equipo1);

        EquipoRescate equipo2 = new EquipoRescate("EQ002", "Bomberos Bravo", EquipoRescate.TipoEquipo.BOMBEROS, "Z001", 12, "Cpt. Rodríguez");
        equipo2.setPersonalAsignado(10);
        equipo2.setExperienciaAnos(12);
        sistema.agregarEquipo(equipo2);

        // Crear usuario administrador de prueba
        Administrador admin = new Administrador("U001", "Admin", "Sistema", "admin@sistema.com", "admin", "admin123", "Coordinación");
        sistema.agregarUsuario(admin);

        System.out.println("✅ Datos de prueba inicializados correctamente");
    }
}