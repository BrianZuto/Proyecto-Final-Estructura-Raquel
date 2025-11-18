// Archivo: src/main/java/co/edu/uniquindio/Gestion/de/Riesgos/Controller/DisasterRestController.java
package co.edu.uniquindio.Gestion.de.Riesgos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

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
import co.edu.uniquindio.Gestion.de.Riesgos.Estructuras.MapaRecursos;
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

    // Fragmento del método obtenerZonas en DisasterRestController.java

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
            
            // Devolver el nombre del enum (CRITICA, ALTA, etc)
            zonaMap.put("nivelUrgencia", zona.getNivelUrgencia().name());
            zonaMap.put("nivelUrgenciaDescripcion", zona.getNivelUrgencia().getDescripcion());
            zonaMap.put("nivelUrgenciaValor", zona.getNivelUrgencia().getValor());
            zonaMap.put("color", zona.getNivelUrgencia().getColor());
            zonaMap.put("activa", zona.isActiva());
            zonaMap.put("radio", zona.getRadio());
            
            System.out.println("📍 Zona: " + zona.getNombre() + 
                " | Nivel: " + zona.getNivelUrgencia().name() + 
                " | Color: " + zona.getNivelUrgencia().getColor() +
                " | Radio: " + zona.getRadio() + "m");
            
            return zonaMap;
        })
        .collect(Collectors.toList());

    System.out.println("✅ Total de zonas devueltas: " + zonasData.size());
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
        System.out.println("\n========================================");
        System.out.println("=== CREANDO ZONA - INICIO ===");
        System.out.println("========================================");
        System.out.println("📥 Datos recibidos completos:");
        zonaData.forEach((key, value) -> 
            System.out.println("   " + key + " = " + value + " [" + (value != null ? value.getClass().getSimpleName() : "null") + "]")
        );
        
        // Validar campos requeridos
        String id = (String) zonaData.get("id");
        String nombre = (String) zonaData.get("nombre");
        String nivelUrgenciaStr = (String) zonaData.get("nivelUrgencia");
        
        if (id == null || id.trim().isEmpty()) {
            System.out.println("❌ ERROR: ID faltante o vacío");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "El campo 'id' es requerido"));
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("❌ ERROR: Nombre faltante o vacío");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "El campo 'nombre' es requerido"));
        }
        
        if (nivelUrgenciaStr == null || nivelUrgenciaStr.trim().isEmpty()) {
            System.out.println("❌ ERROR: Nivel de urgencia faltante");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "El campo 'nivelUrgencia' es requerido"));
        }
        
        // Parsear nivel de urgencia
        NivelUrgencia nivelUrgencia;
        try {
            nivelUrgencia = NivelUrgencia.valueOf(nivelUrgenciaStr.toUpperCase());
            System.out.println("✅ Nivel de urgencia parseado: " + nivelUrgencia + " (valor: " + nivelUrgencia.getValor() + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ ERROR: Nivel de urgencia inválido: " + nivelUrgenciaStr);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Nivel de urgencia inválido: " + nivelUrgenciaStr + ". Valores permitidos: BAJA, MEDIA, ALTA, CRITICA"));
        }
        
        // Crear zona con el nivel correcto
        Zona zona = new Zona(id, nombre, nivelUrgencia);
        System.out.println("✅ Zona instanciada:");
        System.out.println("   ID: " + zona.getId());
        System.out.println("   Nombre: " + zona.getNombre());
        System.out.println("   Nivel: " + zona.getNivelUrgencia() + " [" + zona.getNivelUrgencia().name() + "]");
        
        // Establecer descripción
        if (zonaData.containsKey("descripcion")) {
            String desc = (String) zonaData.get("descripcion");
            zona.setDescripcion(desc);
            System.out.println("✅ Descripción: " + desc);
        }
        
        // Establecer coordenadas
        if (zonaData.containsKey("coordenadaX")) {
            double x = ((Number) zonaData.get("coordenadaX")).doubleValue();
            zona.setCoordenadaX(x);
            System.out.println("✅ CoordenadaX: " + x);
        }
        
        if (zonaData.containsKey("coordenadaY")) {
            double y = ((Number) zonaData.get("coordenadaY")).doubleValue();
            zona.setCoordenadaY(y);
            System.out.println("✅ CoordenadaY: " + y);
        }
        
        // Establecer población
        if (zonaData.containsKey("poblacionAfectada")) {
            int poblacion = ((Number) zonaData.get("poblacionAfectada")).intValue();
            zona.setPoblacionAfectada(poblacion);
            System.out.println("✅ Población: " + poblacion);
            System.out.println("   Nivel después de población: " + zona.getNivelUrgencia() + " [NO debe cambiar]");
        }
        
        // CRÍTICO: Establecer el radio
        if (zonaData.containsKey("radio")) {
            Object radioObj = zonaData.get("radio");
            int radio;
            
            if (radioObj instanceof Number) {
                radio = ((Number) radioObj).intValue();
            } else if (radioObj instanceof String) {
                radio = Integer.parseInt((String) radioObj);
            } else {
                System.out.println("⚠️ Radio tiene tipo inesperado: " + radioObj.getClass());
                radio = 500;
            }
            
            zona.setRadio(radio);
            System.out.println("✅ Radio configurado: " + radio + " metros");
            System.out.println("   Radio en zona: " + zona.getRadio() + " metros");
        } else {
            System.out.println("⚠️ Campo 'radio' no encontrado en request, usando default: " + zona.getRadio() + " metros");
        }
        
        // Resumen final
        System.out.println("\n=== RESUMEN ZONA ANTES DE GUARDAR ===");
        System.out.println("ID: " + zona.getId());
        System.out.println("Nombre: " + zona.getNombre());
        System.out.println("Nivel Urgencia: " + zona.getNivelUrgencia().name() + " (" + zona.getNivelUrgencia().getDescripcion() + ")");
        System.out.println("Coordenadas: (" + zona.getCoordenadaX() + ", " + zona.getCoordenadaY() + ")");
        System.out.println("Población: " + zona.getPoblacionAfectada());
        System.out.println("Radio: " + zona.getRadio() + " metros");
        System.out.println("Activa: " + zona.isActiva());
        System.out.println("======================================\n");
        
        // Agregar al sistema
        boolean agregada = sistema.agregarZona(zona);
        
        if (agregada) {
            System.out.println("✅ Zona agregada exitosamente al sistema");
            System.out.println("========================================\n");
            
            // Respuesta con todos los datos
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Zona creada exitosamente",
                "zona", Map.of(
                    "id", zona.getId(),
                    "nombre", zona.getNombre(),
                    "nivelUrgencia", zona.getNivelUrgencia().name(),
                    "nivelUrgenciaDescripcion", zona.getNivelUrgencia().getDescripcion(),
                    "radio", zona.getRadio(),
                    "coordenadaX", zona.getCoordenadaX(),
                    "coordenadaY", zona.getCoordenadaY(),
                    "poblacionAfectada", zona.getPoblacionAfectada(),
                    "descripcion", zona.getDescripcion(),
                    "color", zona.getNivelUrgencia().getColor()
                )
            ));
        } else {
            System.out.println("❌ No se pudo agregar la zona al sistema (posible ID duplicado)");
            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("success", false, "message", "La zona con ID '" + id + "' ya existe en el sistema"));
        }
        
    } catch (Exception e) {
        System.err.println("❌❌❌ ERROR CRÍTICO creando zona ❌❌❌");
        System.err.println("Mensaje: " + e.getMessage());
        System.err.println("Tipo: " + e.getClass().getName());
        e.printStackTrace();
        System.err.println("========================================\n");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "success", false, 
                "message", "Error interno del servidor: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
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

    // MapaRecursos: obtener todos los recursos del mapa
    @GetMapping("/mapa/recursos/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodosRecursosMapa() {
        try {
            List<Recurso> recursos = sistema.obtenerTodosLosRecursosMapa();
            if (recursos == null) recursos = Collections.emptyList();

            List<Map<String, Object>> data = recursos.stream()
                .filter(rec -> rec != null)
                .map(rec -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rec.getId());
                    m.put("nombre", rec.getNombre());
                    m.put("tipo", rec.getTipo() != null ? rec.getTipo().getDescripcion() : null);
                    m.put("cantidadDisponible", rec.getCantidadDisponible());
                    m.put("unidadMedida", rec.getUnidadMedida());
                    m.put("ubicacionId", rec.getUbicacionId());
                    return m;
                }).collect(Collectors.toList());

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("Error en /mapa/recursos/todos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // MapaRecursos: obtener recursos por tipo
    @GetMapping("/mapa/recursos/tipo/{tipo}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRecursosPorTipoMapa(@PathVariable String tipo) {
        try {
            TipoRecurso tipoRecurso = TipoRecurso.valueOf(tipo.toUpperCase());
            List<Recurso> recursos = sistema.obtenerRecursosPorTipoMapa(tipoRecurso);
            if (recursos == null) recursos = Collections.emptyList();

            List<Map<String, Object>> data = recursos.stream().map(rec -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rec.getId());
                m.put("nombre", rec.getNombre());
                m.put("tipo", rec.getTipo() != null ? rec.getTipo().getDescripcion() : null);
                m.put("cantidadDisponible", rec.getCantidadDisponible());
                m.put("unidadMedida", rec.getUnidadMedida());
                m.put("ubicacionId", rec.getUbicacionId());
                return m;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // MapaRecursos: obtener estadísticas
    @GetMapping("/mapa/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasMapa() {
        try {
            MapaRecursos mapa = sistema.getMapaRecursos();
            if (mapa == null) {
                return ResponseEntity.ok(Map.of(
                    "totalRecursos", 0,
                    "totalRutas", 0,
                    "recursosPorTipo", Collections.emptyMap()
                ));
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecursos", mapa.getTotalRecursos());
            stats.put("totalRutas", mapa.getTotalRutas());
            Map<String, Integer> recursosPorTipoMap = new HashMap<>();
            try {
                mapa.calcularTotalPorTipo().forEach((tipo, cantidad) -> {
                    if (tipo != null) {
                        recursosPorTipoMap.put(tipo.getDescripcion(), cantidad);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error calculando total por tipo: " + e.getMessage());
            }
            stats.put("recursosPorTipo", recursosPorTipoMap);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error en /mapa/estadisticas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "totalRecursos", 0,
                "totalRutas", 0,
                "recursosPorTipo", Collections.emptyMap()
            ));
        }
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

    @GetMapping("/cola/todas")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasEvacuacionesCola() {
        try {
            List<Evacuacion> evacuaciones = sistema.obtenerTodasEvacuacionesCola();
            if (evacuaciones == null) evacuaciones = Collections.emptyList();
            
            List<Map<String, Object>> data = evacuaciones.stream().map(ev -> {
                if (ev == null) return null;
                Map<String, Object> m = new HashMap<>();
                m.put("id", ev.getId());
                m.put("nombre", ev.getNombre());
                m.put("nivelUrgencia", ev.getNivelUrgencia() != null ? ev.getNivelUrgencia().getDescripcion() : null);
                m.put("personasAEvacuar", ev.getPersonasAEvacuar());
                m.put("personasEvacuadas", ev.getPersonasEvacuadas());
                m.put("prioridad", ev.calcularPrioridad());
                m.put("estado", ev.getEstado() != null ? ev.getEstado().name() : null);
                m.put("porcentajeCompletado", ev.calcularPorcentajeCompletado());
                return m;
            }).filter(m -> m != null).collect(Collectors.toList());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("Error en /cola/todas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/cola/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCola() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("tamano", sistema.obtenerTamanoCola());
            stats.put("estaVacia", sistema.estaVaciaCola());
            
            List<Evacuacion> historial = sistema.obtenerHistorialEvacuaciones();
            stats.put("historialSize", historial != null ? historial.size() : 0);
            
            Evacuacion siguiente = sistema.verSiguienteEvacuacionCola();
            if (siguiente != null) {
                Map<String, Object> siguienteMap = new HashMap<>();
                siguienteMap.put("id", siguiente.getId());
                siguienteMap.put("nombre", siguiente.getNombre());
                siguienteMap.put("prioridad", siguiente.calcularPrioridad());
                stats.put("siguiente", siguienteMap);
            } else {
                stats.put("siguiente", null);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error en /cola/estadisticas: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("tamano", 0);
            errorStats.put("estaVacia", true);
            errorStats.put("historialSize", 0);
            errorStats.put("siguiente", null);
            return ResponseEntity.ok(errorStats);
        }
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

    // ===== 1. Totales generales (por cantidad de registros) =====
    estadisticas.put("totalZonas", sistema.getZonas().size());
    estadisticas.put("totalEquipos", sistema.getEquipos().size());
    estadisticas.put("totalEvacuaciones", sistema.getEvacuaciones().size());
    estadisticas.put("totalRutas", sistema.getRutas().size());

    // Total de recursos = suma de cantidades (no solo cuántos registros hay)
    int totalRecursos = sistema.getRecursos().stream()
            .mapToInt(Recurso::getCantidad)
            .sum();
    estadisticas.put("totalRecursos", totalRecursos);

    // ===== 2. Porcentaje de recursos disponibles =====
    int total = 0;
    int disponibles = 0;

    for (Recurso r : sistema.getRecursos()) {
        int cantTotal = r.getCantidad();             // int, no hace falta null-check
        int cantDisp  = r.getCantidadDisponible();   // int, no hace falta null-check
        total += cantTotal;
        disponibles += cantDisp;
    }

    if (total > 0) {
        long porc = Math.round(disponibles * 100.0 / total);
        estadisticas.put("porcentajeRecursosDisponibles", porc);
    } else {
        estadisticas.put("porcentajeRecursosDisponibles", null);
    }

    // ===== 3. Porcentaje de evacuaciones completadas =====
    int totalEvac = sistema.getEvacuaciones().size();
    if (totalEvac > 0) {
        long completadas = sistema.getEvacuaciones().stream()
                .filter(ev -> ev.getEstado() != null &&
                        ("COMPLETADA".equals(ev.getEstado().name())
                                || "FINALIZADA".equals(ev.getEstado().name())))
                .count();
        long porcEvac = Math.round(completadas * 100.0 / totalEvac);
        estadisticas.put("porcentajeEvacuacionesCompletadas", porcEvac);
    } else {
        estadisticas.put("porcentajeEvacuacionesCompletadas", null);
    }

    // ===== 4. Zonas por nivel de urgencia =====
    Map<String, Long> zonasPorUrgencia = sistema.getZonas().stream()
            .collect(Collectors.groupingBy(
                    zona -> zona.getNivelUrgencia().getDescripcion(),
                    Collectors.counting()
            ));
    estadisticas.put("zonasPorUrgencia", zonasPorUrgencia);

    // ===== 5. Recursos por tipo =====
    // resumenRecursosPorTipo() devuelve Map<TipoRecurso, Integer>
    Map<String, Integer> recursosPorTipo = sistema.resumenRecursosPorTipo()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                    entry -> entry.getKey().getDescripcion(),
                    Map.Entry::getValue
            ));
    estadisticas.put("recursosPorTipo", recursosPorTipo);

    // ===== 6. Equipos por estado =====
    Map<String, Long> equiposPorEstado = sistema.getEquipos().stream()
            .collect(Collectors.groupingBy(
                    equipo -> equipo.getEstado().getDescripcion(),
                    Collectors.counting()
            ));
    estadisticas.put("equiposPorEstado", equiposPorEstado);

    // ===== 7. Top 5 zonas críticas =====
    List<Map<String, Object>> zonasCriticas = sistema.topZonasCriticas(5).stream()
            .map(zona -> {
                Map<String, Object> mapa = new HashMap<>();
                mapa.put("id", zona.getId());
                mapa.put("nombre", zona.getNombre());
                mapa.put("poblacionAfectada", zona.getPoblacionAfectada());
                mapa.put("nivelUrgencia", zona.getNivelUrgencia().getDescripcion());
                return mapa;
            })
            .collect(Collectors.toList());
    estadisticas.put("zonasCriticas", zonasCriticas);

    return ResponseEntity.ok(estadisticas);
}

    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Object>> obtenerReporteGeneral() {
        String reporte = sistema.generarReporteGeneral();
        return ResponseEntity.ok(Map.of("reporte", reporte));
    }

    // ============ CARGA DE DATOS DESDE ARCHIVO JSON ============

    @PostMapping("/cargar-datos")
    public ResponseEntity<Map<String, Object>> cargarDatosDesdeJSON(@RequestBody(required = false) Map<String, Object> datosJson) {
        try {
            Map<String, Object> datos;
            
            // Si no se envía JSON en el body, cargar desde archivo
            if (datosJson == null || datosJson.isEmpty()) {
                datos = cargarDatosDesdeArchivo();
                // Verificar si el archivo se cargó correctamente
                if (datos == null || datos.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                            "success", false,
                            "message", "No se pudo cargar el archivo JSON o el archivo está vacío"
                        ));
                }
            } else {
                datos = datosJson;
            }
            
            int zonasCreadas = 0;
            int rutasCreadas = 0;
            int recursosCreados = 0;
            int equiposCreados = 0;
            int usuariosCreados = 0;
            
            // Cargar zonas
            if (datos.containsKey("zonas") && datos.get("zonas") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> zonas = (List<Map<String, Object>>) datos.get("zonas");
                for (Map<String, Object> z : zonas) {
                    try {
                        Zona zona = new Zona(
                            (String) z.get("id"),
                            (String) z.get("nombre"),
                            NivelUrgencia.valueOf(((String) z.get("nivelUrgencia")).toUpperCase())
                        );
                        if (z.containsKey("coordenadaX")) {
                            zona.setCoordenadaX(((Number) z.get("coordenadaX")).doubleValue());
                        }
                        if (z.containsKey("coordenadaY")) {
                            zona.setCoordenadaY(((Number) z.get("coordenadaY")).doubleValue());
                        }
                        if (z.containsKey("poblacionAfectada")) {
                            zona.setPoblacionAfectada(((Number) z.get("poblacionAfectada")).intValue());
                        }
                        if (z.containsKey("descripcion")) {
                            zona.setDescripcion((String) z.get("descripcion"));
                        }
                        if (z.containsKey("radio")) {
                            zona.setRadio(((Number) z.get("radio")).intValue());
                        }
                        if (sistema.agregarZona(zona)) {
                            zonasCreadas++;
                        }
                    } catch (Exception e) {
                        System.err.println("Error creando zona: " + e.getMessage());
                    }
                }
            }
            
            // Cargar rutas
            if (datos.containsKey("rutas") && datos.get("rutas") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rutas = (List<Map<String, Object>>) datos.get("rutas");
                for (Map<String, Object> r : rutas) {
                    try {
                        Ruta ruta = sistema.conectarZonas(
                            (String) r.get("id"),
                            (String) r.get("origenId"),
                            (String) r.get("destinoId"),
                            ((Number) r.get("distancia")).doubleValue(),
                            ((Number) r.get("tiempoEstimado")).doubleValue(),
                            TipoRuta.valueOf(((String) r.get("tipo")).toUpperCase())
                        );
                        if (ruta != null) {
                            rutasCreadas++;
                        }
                    } catch (Exception e) {
                        System.err.println("Error creando ruta: " + e.getMessage());
                    }
                }
            }
            
            // Cargar recursos (después de cargar rutas para que se puedan asignar automáticamente)
            if (datos.containsKey("recursos") && datos.get("recursos") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> recursos = (List<Map<String, Object>>) datos.get("recursos");
                for (Map<String, Object> r : recursos) {
                    try {
                        Recurso recurso = new Recurso(
                            (String) r.get("id"),
                            (String) r.get("nombre"),
                            TipoRecurso.valueOf(((String) r.get("tipo")).toUpperCase()),
                            ((Number) r.get("cantidad")).intValue(),
                            (String) r.get("unidadMedida"),
                            (String) r.get("ubicacionId")
                        );
                        if (sistema.agregarRecurso(recurso)) {
                            recursosCreados++;
                            // El método agregarRecurso ahora automáticamente asigna recursos a rutas asociadas
                        }
                    } catch (Exception e) {
                        System.err.println("Error creando recurso: " + e.getMessage());
                    }
                }
            }
            
            // Cargar equipos
            if (datos.containsKey("equipos") && datos.get("equipos") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> equipos = (List<Map<String, Object>>) datos.get("equipos");
                for (Map<String, Object> e : equipos) {
                    try {
                        EquipoRescate equipo = new EquipoRescate(
                            (String) e.get("id"),
                            (String) e.get("nombre"),
                            EquipoRescate.TipoEquipo.valueOf(((String) e.get("tipo")).toUpperCase()),
                            (String) e.get("ubicacionActual"),
                            ((Number) e.get("capacidadMaxima")).intValue(),
                            (String) e.get("liderEquipo")
                        );
                        if (e.containsKey("personalAsignado")) {
                            equipo.setPersonalAsignado(((Number) e.get("personalAsignado")).intValue());
                        }
                        if (e.containsKey("experienciaAnos")) {
                            equipo.setExperienciaAnos(((Number) e.get("experienciaAnos")).intValue());
                        }
                        if (sistema.agregarEquipo(equipo)) {
                            equiposCreados++;
                        }
                    } catch (Exception ex) {
                        System.err.println("Error creando equipo: " + ex.getMessage());
                    }
                }
            }
            
            // Cargar usuarios
            if (datos.containsKey("usuarios") && datos.get("usuarios") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> usuarios = (List<Map<String, Object>>) datos.get("usuarios");
                for (Map<String, Object> u : usuarios) {
                    try {
                        String rol = ((String) u.get("rol")).toUpperCase();
                        Usuario usuario;
                        if ("ADMINISTRADOR".equals(rol)) {
                            usuario = new Administrador(
                                (String) u.get("id"),
                                (String) u.get("nombre"),
                                (String) u.get("apellido"),
                                (String) u.get("email"),
                                (String) u.get("username"),
                                (String) u.get("password"),
                                (String) u.getOrDefault("departamento", "General")
                            );
                        } else {
                            usuario = new OperadorEmergencia(
                                (String) u.get("id"),
                                (String) u.get("nombre"),
                                (String) u.get("apellido"),
                                (String) u.get("email"),
                                (String) u.get("username"),
                                (String) u.get("password"),
                                (String) u.getOrDefault("especialidad", "Emergencias"),
                                (String) u.getOrDefault("ubicacion", "Sin asignar")
                            );
                        }
                        if (sistema.agregarUsuario(usuario)) {
                            usuariosCreados++;
                        }
                    } catch (Exception ex) {
                        System.err.println("Error creando usuario: " + ex.getMessage());
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Datos cargados exitosamente",
                "resumen", Map.of(
                    "zonas", zonasCreadas,
                    "rutas", rutasCreadas,
                    "recursos", recursosCreados,
                    "equipos", equiposCreados,
                    "usuarios", usuariosCreados
                )
            ));
            
        } catch (Exception e) {
            System.err.println("Error cargando datos desde JSON: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error al cargar datos: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Carga datos desde el archivo JSON predefinido en resources
     */
    private Map<String, Object> cargarDatosDesdeArchivo() {
        try {
            ClassPathResource resource = new ClassPathResource("datos-iniciales.json");
            
            if (!resource.exists()) {
                System.err.println("❌ El archivo datos-iniciales.json no existe en resources");
                return new HashMap<>();
            }
            
            InputStream inputStream = resource.getInputStream();
            StringBuilder jsonContent = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line).append("\n");
                }
            }
            
            if (jsonContent.length() == 0) {
                System.err.println("❌ El archivo datos-iniciales.json está vacío");
                return new HashMap<>();
            }
            
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> datos = objectMapper.readValue(jsonContent.toString(), Map.class);
            
            System.out.println("✅ Archivo JSON cargado correctamente desde resources");
            return datos;
        } catch (Exception e) {
            System.err.println("❌ Error leyendo archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Endpoint para cargar datos desde el archivo predefinido
     */
    @PostMapping("/cargar-datos-archivo")
    public ResponseEntity<Map<String, Object>> cargarDatosDesdeArchivoPredefinido() {
        try {
            System.out.println("📥 Iniciando carga de datos desde archivo JSON...");
            return cargarDatosDesdeJSON(null);
        } catch (Exception e) {
            System.err.println("❌ Error en endpoint cargar-datos-archivo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error al procesar la solicitud: " + e.getMessage()
                ));
        }
    }

    // ============ ENDPOINTS DE ASIGNACIONES ============

    @PostMapping("/asignaciones")
    public ResponseEntity<Map<String, Object>> asignarRecursoAZona(@RequestBody Map<String, Object> asignacionData) {
        try {
            String zonaId = (String) asignacionData.get("zonaId");
            String recursoId = (String) asignacionData.get("recursoId");
            int cantidad = ((Number) asignacionData.get("cantidad")).intValue();
            
            // Validar que la zona y el recurso existan
            Zona zona = sistema.buscarZona(zonaId);
            if (zona == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Zona no encontrada"));
            }
            
            Recurso recurso = sistema.buscarRecurso(recursoId);
            if (recurso == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Recurso no encontrado"));
            }
            
            // Verificar que hay suficiente cantidad disponible
            if (recurso.getCantidadDisponible() < cantidad) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "No hay suficiente cantidad disponible"));
            }
            
            // Reservar el recurso
            if (!recurso.reservar(cantidad)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "No se pudo reservar el recurso"));
            }
            
            // Actualizar la ubicación del recurso
            recurso.setUbicacionId(zonaId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recurso asignado exitosamente a la zona",
                "recursoId", recursoId,
                "zonaId", zonaId,
                "cantidad", cantidad
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error al asignar recurso: " + e.getMessage()));
        }
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerAsignaciones() {
        try {
            // Retornar recursos agrupados por zona
            List<Map<String, Object>> asignaciones = new ArrayList<>();
            
            for (Recurso recurso : sistema.getRecursos()) {
                if (recurso.getUbicacionId() != null && !recurso.getUbicacionId().isEmpty()) {
                    Map<String, Object> asignacion = new HashMap<>();
                    asignacion.put("recursoId", recurso.getId());
                    asignacion.put("zonaId", recurso.getUbicacionId());
                    asignacion.put("cantidad", recurso.getCantidad() - recurso.getCantidadDisponible());
                    asignacion.put("recursoNombre", recurso.getNombre());
                    asignaciones.add(asignacion);
                }
            }
            
            return ResponseEntity.ok(asignaciones);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
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