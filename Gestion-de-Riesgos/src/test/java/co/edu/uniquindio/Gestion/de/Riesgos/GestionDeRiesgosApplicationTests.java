package co.edu.uniquindio.Gestion.de.Riesgos;

import co.edu.uniquindio.Gestion.de.Riesgos.Enums.NivelUrgencia;
import co.edu.uniquindio.Gestion.de.Riesgos.Enums.TipoRecurso;
import co.edu.uniquindio.Gestion.de.Riesgos.Enums.TipoRuta;
import co.edu.uniquindio.Gestion.de.Riesgos.Estructuras.*;
import co.edu.uniquindio.Gestion.de.Riesgos.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas unitarias para el Sistema de Gestión de Desastres
 * Cubre las principales funcionalidades y estructuras de datos
 */
@SpringBootTest
class SistemaGestionDesastresTests {

    private SistemaGestionDesastres sistema;
    private Zona zonaOrigen;
    private Zona zonaDestino;
    private Recurso recurso;
    private EquipoRescate equipo;

    @BeforeEach
    void setUp() {
        sistema = new SistemaGestionDesastres();
        sistema.inicializarSistema();
        
        // Crear zonas de prueba
        zonaOrigen = new Zona("Z001", "Zona Test Origen", NivelUrgencia.ALTA);
        zonaOrigen.setCoordenadaX(4.5389);
        zonaOrigen.setCoordenadaY(-75.6678);
        zonaOrigen.setPoblacionAfectada(1000);
        
        zonaDestino = new Zona("Z002", "Zona Test Destino", NivelUrgencia.MEDIA);
        zonaDestino.setCoordenadaX(4.5489);
        zonaDestino.setCoordenadaY(-75.6578);
        zonaDestino.setPoblacionAfectada(500);
        
        // Crear recurso de prueba
        recurso = new Recurso("R001", "Agua Potable", TipoRecurso.ALIMENTOS, 1000, "litros", "Z001");
        
        // Crear equipo de prueba
        equipo = new EquipoRescate("EQ001", "Equipo Médico Alpha", 
                                   EquipoRescate.TipoEquipo.MEDICO, 
                                   "Z001", 10, "Dr. García");
    }

    /**
     * PRUEBA 1: Verificar creación y gestión de zonas
     */
    @Test
    void testCrearYGestionarZonas() {
        assertTrue(sistema.agregarZona(zonaOrigen), 
                  "Debe poder agregar una zona nueva");
        assertTrue(sistema.agregarZona(zonaDestino), 
                  "Debe poder agregar una segunda zona");
        assertFalse(sistema.agregarZona(zonaOrigen), 
                   "No debe permitir duplicar zonas");
        
        assertEquals(2, sistema.getZonas().size(), 
                    "El sistema debe tener 2 zonas");
        
        Zona zonaBuscada = sistema.buscarZona("Z001");
        assertNotNull(zonaBuscada, "Debe encontrar la zona por ID");
        assertEquals("Zona Test Origen", zonaBuscada.getNombre(), 
                    "El nombre de la zona debe coincidir");
    }

    /**
     * PRUEBA 2: Verificar niveles de urgencia y priorización de zonas
     */
    @Test
    void testNivelesUrgenciaYPriorizacion() {
        zonaOrigen.setNivelUrgencia(NivelUrgencia.CRITICA);
        assertEquals(NivelUrgencia.CRITICA, zonaOrigen.getNivelUrgencia(), 
                    "Debe poder cambiar el nivel de urgencia");
        
        assertTrue(zonaOrigen.estaEnEstadoCritico(), 
                  "Una zona CRÍTICA debe estar en estado crítico");
        
        int prioridadAlta = zonaOrigen.calcularPrioridadEvacuacion();
        int prioridadMedia = zonaDestino.calcularPrioridadEvacuacion();
        
        assertTrue(prioridadAlta > prioridadMedia, 
                  "Una zona CRÍTICA debe tener mayor prioridad que una MEDIA");
    }

    /**
     * PRUEBA 3: Verificar gestión de recursos y disponibilidad
     */
    @Test
    void testGestionRecursos() {
        assertTrue(sistema.agregarRecurso(recurso), 
                  "Debe poder agregar un recurso");
        assertEquals(1, sistema.getRecursos().size(), 
                    "Debe haber 1 recurso en el sistema");
        
        assertTrue(recurso.estaDisponible(), 
                  "El recurso debe estar disponible inicialmente");
        
        assertTrue(recurso.reservar(500), 
                  "Debe poder reservar 500 unidades");
        assertEquals(500, recurso.getCantidadDisponible(), 
                    "Deben quedar 500 unidades disponibles");
        
        assertFalse(recurso.reservar(600), 
                   "No debe permitir reservar más de lo disponible");
        
        recurso.liberar(200);
        assertEquals(700, recurso.getCantidadDisponible(), 
                    "Después de liberar 200, deben haber 700 disponibles");
    }

    /**
     * PRUEBA 4: Verificar gestión de equipos de rescate
     */
    @Test
    void testGestionEquiposRescate() {
        assertTrue(sistema.agregarEquipo(equipo), 
                  "Debe poder agregar un equipo");
        
        equipo.setPersonalAsignado(8);
        equipo.setExperienciaAnos(5);
        
        assertTrue(equipo.puedeSerAsignado(), 
                  "Un equipo disponible con personal debe poder ser asignado");
        
        double eficiencia = equipo.calcularEficiencia();
        assertTrue(eficiencia > 0 && eficiencia <= 1, 
                  "La eficiencia debe estar entre 0 y 1");
        
        assertTrue(equipo.asignarAEmergencia("EMG001"), 
                  "Debe poder asignar el equipo a una emergencia");
        assertEquals(EquipoRescate.EstadoEquipo.EN_OPERACION, equipo.getEstado(), 
                    "El estado debe cambiar a EN_OPERACION");
        assertFalse(equipo.isDisponible(), 
                   "El equipo no debe estar disponible después de asignarlo");
    }

    /**
     * PRUEBA 5: Verificar creación y gestión de rutas
     */
    @Test
    void testCreacionYGestionRutas() {
        sistema.agregarZona(zonaOrigen);
        sistema.agregarZona(zonaDestino);
        
        Ruta ruta = sistema.conectarZonas("R001", "Z001", "Z002", 
                                          15.5, 0.8, TipoRuta.TERRESTRE);
        
        assertNotNull(ruta, "Debe crear una ruta válida");
        assertEquals(1, sistema.getRutas().size(), 
                    "Debe haber 1 ruta en el sistema");
        assertEquals(15.5, ruta.getDistancia(), 
                    "La distancia debe coincidir");
        
        assertTrue(ruta.isActiva(), "La ruta debe estar activa por defecto");
        
        ruta.setCapacidadMaxima(100);
        ruta.setCapacidadActual(50);
        assertEquals(50.0, ruta.calcularPorcentajeOcupacion(), 
                    "El porcentaje de ocupación debe ser 50%");
    }

    /**
     * PRUEBA 6: Verificar Cola de Prioridad para evacuaciones
     */
    @Test
    void testColaPrioridadEvacuaciones() {
        sistema.agregarZona(zonaOrigen);
        sistema.agregarZona(zonaDestino);
        
        Ruta ruta = sistema.conectarZonas("R001", "Z001", "Z002", 
                                          10.0, 0.5, TipoRuta.TERRESTRE);
        
        // Crear evacuaciones con diferentes prioridades
        Evacuacion evac1 = new Evacuacion("EV001", "Evacuación Baja", 
                                          ruta, NivelUrgencia.BAJA);
        evac1.setPersonasAEvacuar(100);
        
        Evacuacion evac2 = new Evacuacion("EV002", "Evacuación Crítica", 
                                          ruta, NivelUrgencia.CRITICA);
        evac2.setPersonasAEvacuar(500);
        
        sistema.agregarEvacuacion(evac1);
        sistema.agregarEvacuacion(evac2);
        
        // Verificar que la cola prioriza correctamente
        Evacuacion siguiente = sistema.verSiguienteEvacuacionCola();
        assertNotNull(siguiente, "Debe haber una evacuación siguiente");
        
        int prioridad1 = evac1.calcularPrioridad();
        int prioridad2 = evac2.calcularPrioridad();
        assertTrue(prioridad2 > prioridad1, 
                  "La evacuación CRÍTICA debe tener mayor prioridad");
    }

    /**
     * PRUEBA 7: Verificar Grafo Dirigido y cálculo de rutas
     */
    @Test
    void testGrafoDirigidoYCalculoRutas() {
        sistema.agregarZona(zonaOrigen);
        sistema.agregarZona(zonaDestino);
        
        Zona zonaIntermedia = new Zona("Z003", "Zona Intermedia", NivelUrgencia.MEDIA);
        zonaIntermedia.setCoordenadaX(4.5289);
        zonaIntermedia.setCoordenadaY(-75.6778);
        sistema.agregarZona(zonaIntermedia);
        
        // Crear rutas
        sistema.conectarZonas("R001", "Z001", "Z003", 8.0, 0.4, TipoRuta.TERRESTRE);
        sistema.conectarZonas("R002", "Z003", "Z002", 7.5, 0.4, TipoRuta.TERRESTRE);
        sistema.conectarZonas("R003", "Z001", "Z002", 20.0, 1.0, TipoRuta.TERRESTRE);
        
        // Verificar existencia de rutas
        assertTrue(sistema.existeRutaEnGrafo("Z001", "Z003"), 
                  "Debe existir ruta de Z001 a Z003");
        assertTrue(sistema.existeRutaEnGrafo("Z003", "Z002"), 
                  "Debe existir ruta de Z003 a Z002");
        
        // Calcular ruta más corta
        var rutaMasCorta = sistema.calcularRutaMasCorta("Z001", "Z002");
        assertNotNull(rutaMasCorta, "Debe encontrar una ruta");
        assertTrue(rutaMasCorta.size() > 0, "La ruta debe tener nodos");
    }

    /**
     * PRUEBA 8 (BONUS): Verificar Árbol de Distribución de recursos
     */
    @Test
    void testArbolDistribucionRecursos() {
        sistema.agregarRecurso(recurso);
        
        // Crear nodo raíz en el árbol
        sistema.crearNodoRaizArbol(recurso, 500);
        
        // Agregar nodos hijos
        Recurso recurso2 = new Recurso("R002", "Medicinas", 
                                      TipoRecurso.MEDICINAS, 
                                      200, "unidades", "Z002");
        sistema.agregarRecurso(recurso2);
        
        sistema.agregarNodoArbol("nodo1", recurso2, 200, "raiz");
        
        int totalEnArbol = sistema.calcularCantidadTotalEnArbol();
        assertTrue(totalEnArbol > 0, 
                  "El árbol debe tener recursos distribuidos");
        
        assertTrue(sistema.arbolTieneRecursosSuficientes(300), 
                  "Debe tener recursos suficientes para 300 unidades");
    }

    /**
     * PRUEBA 9 (BONUS): Verificar estadísticas del sistema
     */
    @Test
    void testEstadisticasDelSistema() {
        // Agregar datos de prueba
        sistema.agregarZona(zonaOrigen);
        sistema.agregarZona(zonaDestino);
        sistema.agregarRecurso(recurso);
        sistema.agregarEquipo(equipo);
        
        // Verificar contadores
        assertEquals(2, sistema.getZonas().size(), 
                    "Debe haber 2 zonas");
        assertEquals(1, sistema.getRecursos().size(), 
                    "Debe haber 1 recurso");
        assertEquals(1, sistema.getEquipos().size(), 
                    "Debe haber 1 equipo");
        
        // Generar reporte
        String reporte = sistema.generarReporteGeneral();
        assertNotNull(reporte, "El reporte no debe ser nulo");
        assertTrue(reporte.contains("REPORTE GENERAL"), 
                  "El reporte debe contener el encabezado");
    }

    /**
     * PRUEBA 10 (BONUS): Verificar planificación de evacuaciones
     */
    @Test
    void testPlanificacionEvacuaciones() {
        sistema.agregarZona(zonaOrigen);
        sistema.agregarZona(zonaDestino);
        sistema.conectarZonas("R001", "Z001", "Z002", 
                             10.0, 0.5, TipoRuta.TERRESTRE);
        
        Evacuacion evacuacion = sistema.planificarEvacuacionEntreZonas(
            "EV001", "Z001", "Z002", 800, 
            NivelUrgencia.ALTA, "Coordinador 1"
        );
        
        assertNotNull(evacuacion, "Debe crear la evacuación");
        assertEquals(800, evacuacion.getPersonasAEvacuar(), 
                    "Debe planificar evacuar 800 personas");
        assertEquals(0, evacuacion.getPersonasEvacuadas(), 
                    "Inicialmente no hay personas evacuadas");
        
        evacuacion.actualizarProgreso(400);
        assertEquals(50.0, evacuacion.calcularPorcentajeCompletado(), 
                    "Debe haber completado el 50%");
    }
}