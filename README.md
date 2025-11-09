# 🛡️ Sistema de Gestión de Desastres Naturales

Sistema web completo para la gestión y monitoreo de desastres naturales, desarrollado con Spring Boot y tecnologías web modernas. Incluye gestión de zonas, recursos, equipos de rescate, evacuaciones y cálculo de rutas en tiempo real.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Requisitos del Sistema](#requisitos-del-sistema)
- [Instalación y Configuración](#instalación-y-configuración)
- [Ejecución del Proyecto](#ejecución-del-proyecto)
- [Cambios y Mejoras Implementadas](#cambios-y-mejoras-implementadas)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [API REST - Endpoints](#api-rest---endpoints)
- [Credenciales de Prueba](#credenciales-de-prueba)
- [Uso de la Aplicación](#uso-de-la-aplicación)

## ✨ Características

- **Gestión de Zonas**: Creación y visualización de zonas afectadas con niveles de urgencia
- **Gestión de Recursos**: Control de recursos disponibles (alimentos, medicinas, equipos)
- **Gestión de Equipos**: Administración de equipos de rescate con estados y eficiencia
- **Planificación de Evacuaciones**: Sistema de cola de prioridad para gestionar evacuaciones
- **Cálculo de Rutas**: Integración con OpenStreetMap para rutas reales por calles
- **Mapa Interactivo**: Visualización de zonas y rutas en mapa interactivo con Leaflet
- **Autenticación**: Sistema de login y registro de usuarios
- **Dashboard Moderno**: Interfaz de usuario moderna y responsiva

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 17+** (compatible con Java 21)
- **Spring Boot 3.5.7**
- **Maven** - Gestor de dependencias
- **Spring Web** - Framework REST

### Frontend
- **HTML5, CSS3, JavaScript**
- **Leaflet.js** - Mapas interactivos
- **Leaflet Routing Machine** - Cálculo de rutas
- **Font Awesome 6.4.0** - Iconos web
- **Chart.js** - Gráficos (preparado para futuras implementaciones)

### Estructuras de Datos
- **Grafo Dirigido** - Para representar conexiones entre zonas
- **Cola de Prioridad** - Para gestionar evacuaciones
- **Árbol de Distribución** - Para distribución de recursos
- **Mapa de Recursos** - Para asociar recursos con rutas

## 📦 Requisitos del Sistema

- **Java**: JDK 17 o superior (probado con Java 21)
- **Maven**: 3.6.0 o superior
- **Sistema Operativo**: Windows, Linux o macOS
- **Navegador**: Chrome, Firefox, Edge (versiones recientes)
- **Memoria**: Mínimo 2GB RAM disponible
- **Puerto**: 8082 (configurable)

## 🚀 Instalación y Configuración

### 1. Clonar o Descargar el Proyecto

```bash
# Si tienes el proyecto en un repositorio
git clone <url-del-repositorio>

# O navega al directorio del proyecto
cd "PROYECTO-GESTION-DE-RIESGOS\Gestion-de-Riesgos"
```

### 2. Verificar Requisitos

```powershell
# Verificar Java
java -version
# Debe mostrar Java 17 o superior

# Verificar Maven
mvn -version
# Debe mostrar Maven 3.6.0 o superior
```

### 3. Configuración del Puerto

El proyecto está configurado para ejecutarse en el **puerto 8082** por defecto. Si necesitas cambiarlo:

1. Edita el archivo `src/main/resources/application.properties`
2. Cambia la línea `server.port=8082` al puerto deseado
3. Actualiza también `API_URL` en `src/main/resources/static/index.html`

## ▶️ Ejecución del Proyecto

### Opción 1: Usando el Script de PowerShell (Recomendado)

```powershell
# Navega al directorio del proyecto
cd "PROYECTO-GESTION-DE-RIESGOS\Gestion-de-Riesgos"

# Ejecuta el script
.\iniciar.ps1
```

El script automáticamente:
- Verifica que estés en el directorio correcto
- Libera el puerto 8082 si está ocupado
- Inicia la aplicación Spring Boot

### Opción 2: Comandos Manuales

```powershell
# 1. Navegar al directorio del proyecto
cd "PROYECTO-GESTION-DE-RIESGOS\Gestion-de-Riesgos"

# 2. Limpiar y compilar (opcional, la primera vez)
mvn clean compile

# 3. Ejecutar la aplicación
mvn spring-boot:run
```

### Opción 3: Compilar y Ejecutar JAR

```powershell
# Compilar el proyecto
mvn clean package

# Ejecutar el JAR generado
java -jar target/Gestion-de-Riesgos-0.0.1-SNAPSHOT.jar
```

## ✅ Verificación de la Aplicación

Una vez que veas en la consola:

```
Started GestionDeRiesgosApplication in X.XXX seconds
```

La aplicación estará lista. Accede a:

- **Frontend**: http://localhost:8082/
- **API Health Check**: http://localhost:8082/api/health
- **API Estadísticas**: http://localhost:8082/api/estadisticas

## 🔧 Cambios y Mejoras Implementadas

### 1. Configuración del Proyecto

#### Corrección de Encoding
- **Problema**: Error de codificación UTF-8 en `application.properties`
- **Solución**: 
  - Reescrito el archivo con codificación UTF-8 correcta
  - Agregado `project.build.sourceEncoding=UTF-8` en `pom.xml`
  - Configurado `maven-resources-plugin` para usar UTF-8

#### Cambio de Puerto
- **Problema**: Puerto 8080 estaba ocupado
- **Solución**: 
  - Cambiado a puerto **8082** en `application.properties`
  - Actualizado `API_URL` en el frontend
  - Creado script `iniciar.ps1` para facilitar la ejecución

### 2. Manejo de Errores

#### GlobalExceptionHandler
- **Archivo**: `GlobalExceptionHandler.java`
- **Funcionalidad**: Manejo global de excepciones con respuestas JSON
- **Errores manejados**:
  - **405 (Method Not Allowed)**: Método HTTP incorrecto
  - **404 (Not Found)**: Endpoint no existe
  - **400 (Bad Request)**: Errores de validación
  - **500 (Internal Server Error)**: Errores del servidor
  - **NullPointerException**: Referencias nulas

#### ErrorController Personalizado
- **Archivo**: `ErrorController.java`
- **Funcionalidad**: Reemplaza la página de error de Spring Boot por respuestas JSON
- **Configuración**: 
  - `spring.mvc.throw-exception-if-no-handler-found=true`
  - `spring.web.resources.add-mappings=true`

#### Formato de Respuesta de Error
Todos los errores ahora devuelven JSON estructurado:

```json
{
  "success": false,
  "error": "METHOD_NOT_ALLOWED",
  "status": 405,
  "message": "El método HTTP 'GET' no está permitido para este endpoint.",
  "allowedMethods": ["POST"],
  "suggestion": "Usa uno de los siguientes métodos: POST"
}
```

### 3. Endpoint de Health Check

- **Endpoint**: `GET /api/health`
- **Propósito**: Verificar que la API esté funcionando
- **Respuesta**:
```json
{
  "status": "UP",
  "message": "API funcionando correctamente",
  "timestamp": 1234567890
}
```

### 4. Modernización del Diseño

#### Cambios Visuales Principales

**Fondos**:
- ❌ Antes: Gradiente morado (`#667eea` a `#764ba2`)
- ✅ Ahora: Fondo blanco/gris claro (`#f5f7fa`)

**Bordes**:
- ❌ Antes: `border-radius: 20px` y `15px`
- ✅ Ahora: `border-radius: 6px` y `8px` (más moderno)

**Tarjetas**:
- ✅ Fondo blanco con bordes sutiles (`border: 1px solid #e1e8ed`)
- ✅ Sombras suaves (`box-shadow: 0 2px 8px rgba(0,0,0,0.08)`)
- ✅ Efectos hover con elevación sutil

**Iconos**:
- ✅ Integración de **Font Awesome 6.4.0**
- ✅ Reemplazo de emojis por iconos web profesionales
- ✅ Iconos en todos los botones y labels

**Colores**:
- ✅ Paleta neutra: grises oscuros (`#2d3748`, `#1a202c`)
- ✅ Acentos azules (`#4299e1`) para acciones principales
- ✅ Textos en grises (`#718096`, `#4a5568`)

#### Mejoras en Login y Registro

**Login**:
- Icono grande de escudo en la parte superior
- Iconos en labels (usuario y contraseña)
- Subtítulo descriptivo
- Botones con iconos

**Registro**:
- Icono grande de usuario en la parte superior
- Iconos en todos los campos del formulario
- Mejor organización visual
- Botón "Volver" con icono

#### Indicador de API
- **Posición**: Esquina inferior derecha (antes: superior derecha)
- **Diseño**: Tarjeta blanca con borde y sombra sutil
- **Estados**: 
  - 🔴 Rojo: API Desconectada
  - 🟢 Verde (pulsante): API Conectada

### 5. Mejoras en el Frontend

- **Manejo de errores mejorado**: Detección específica de errores 405
- **CORS explícito**: `mode: 'cors'` en todas las peticiones
- **Validación de respuestas**: Verificación de `response.ok` antes de procesar
- **Logs en consola**: Mejor debugging con `console.error`

## 📁 Estructura del Proyecto

```
PROYECTO-GESTION-DE-RIESGOS/
├── Gestion-de-Riesgos/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── co/edu/uniquindio/Gestion/de/Riesgos/
│   │   │   │       ├── DisasterRestController.java      # Controlador REST principal
│   │   │   │       ├── GlobalExceptionHandler.java      # Manejo global de errores
│   │   │   │       ├── ErrorController.java            # Controlador de errores HTTP
│   │   │   │       ├── GestionDeRiesgosApplication.java # Clase principal
│   │   │   │       ├── Enums/                           # Enumeraciones
│   │   │   │       │   ├── NivelUrgencia.java
│   │   │   │       │   ├── Rol.java
│   │   │   │       │       │   ├── TipoRecurso.java
│   │   │   │       │       └── TipoRuta.java
│   │   │   │       ├── Estructuras/                      # Estructuras de datos
│   │   │   │       │   ├── ArbolDistribucion.java
│   │   │   │       │   ├── ColaPrioridad.java
│   │   │   │       │   ├── GrafoDirigido.java
│   │   │   │       │   ├── MapaRecursos.java
│   │   │   │       │   ├── Nodo.java
│   │   │   │       │   └── Ruta.java
│   │   │   │       ├── Interfaces/
│   │   │   │       │   └── ICalcularRuta.java
│   │   │   │       └── Model/                           # Modelos de datos
│   │   │   │           ├── Administrador.java
│   │   │   │           ├── EquipoRescate.java
│   │   │   │           ├── Evacuacion.java
│   │   │   │           ├── OperadorEmergencia.java
│   │   │   │           ├── Recurso.java
│   │   │   │           ├── SistemaGestionDesastres.java
│   │   │   │           ├── Usuario.java
│   │   │   │           └── Zona.java
│   │   │   └── resources/
│   │   │       ├── application.properties              # Configuración
│   │   │       └── static/
│   │   │           └── index.html                      # Frontend
│   │   └── test/                                        # Pruebas
│   ├── pom.xml                                          # Configuración Maven
│   ├── iniciar.ps1                                      # Script de inicio (Windows)
│   └── ejecutar.ps1                                     # Script alternativo
└── README.md                                            # Este archivo
```

## 🌐 API REST - Endpoints

### Autenticación

#### POST `/api/login`
Iniciar sesión

**Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta exitosa**:
```json
{
  "success": true,
  "user": {
    "id": "U001",
    "nombre": "Admin Sistema",
    "rol": "Administrador",
    "email": "admin@sistema.com"
  }
}
```

#### POST `/api/register`
Registrar nuevo usuario

**Body**:
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "username": "juan123",
  "password": "password123",
  "rol": "OPERADOR_EMERGENCIA",
  "especialidad": "Rescate",
  "ubicacion": "Bogotá"
}
```

### Zonas

- `GET /api/zonas` - Obtener todas las zonas
- `GET /api/zonas/{id}` - Obtener zona por ID
- `POST /api/zonas` - Crear nueva zona

### Recursos

- `GET /api/recursos` - Obtener todos los recursos
- `GET /api/recursos/ubicacion/{ubicacionId}` - Recursos por ubicación
- `POST /api/recursos` - Crear nuevo recurso

### Equipos

- `GET /api/equipos` - Obtener todos los equipos
- `POST /api/equipos` - Crear nuevo equipo

### Evacuaciones

- `GET /api/evacuaciones` - Obtener todas las evacuaciones
- `POST /api/evacuaciones` - Planificar nueva evacuación

### Rutas

- `GET /api/rutas` - Obtener todas las rutas
- `POST /api/rutas` - Crear nueva ruta

### Grafo y Estructuras

- `GET /api/grafo/rutas/desde/{idOrigen}` - Rutas desde un nodo
- `GET /api/grafo/rutas/hasta/{idDestino}` - Rutas hacia un nodo
- `GET /api/grafo/nodo/{id}` - Obtener nodo del grafo
- `GET /api/cola/verSiguiente` - Ver siguiente evacuación en cola
- `POST /api/cola/priorizar` - Priorizar cola de evacuaciones
- `POST /api/cola/procesar` - Procesar siguiente evacuación
- `GET /api/arbol/total` - Cantidad total en árbol de distribución
- `POST /api/arbol/crearRaiz` - Crear raíz del árbol
- `POST /api/arbol/agregarNodo` - Agregar nodo al árbol

### Estadísticas y Reportes

- `GET /api/health` - Health check de la API
- `GET /api/estadisticas` - Estadísticas generales del sistema
- `GET /api/reporte` - Reporte general del sistema

## 🔑 Credenciales de Prueba

El sistema viene con un usuario administrador preconfigurado:

- **Usuario**: `admin`
- **Contraseña**: `admin123`
- **Rol**: Administrador

## 📖 Uso de la Aplicación

### 1. Acceso Inicial

1. Abre tu navegador en `http://localhost:8082/`
2. Verás la pantalla de login
3. El indicador de API en la esquina inferior derecha debe mostrar "API Conectada" (verde)

### 2. Iniciar Sesión

1. Ingresa las credenciales:
   - Usuario: `admin`
   - Contraseña: `admin123`
2. Haz clic en "Ingresa Aquí"
3. Serás redirigido al dashboard

### 3. Registrar Nuevo Usuario

1. En la pantalla de login, haz clic en "Crea tu cuenta aquí"
2. Completa el formulario de registro
3. Selecciona el rol (Operador de Emergencia o Administrador)
4. Haz clic en "Crear Cuenta"
5. Serás redirigido al login para iniciar sesión

### 4. Usar el Mapa Interactivo

1. En el dashboard, haz clic en "Abrir Mapa"
2. Selecciona un modo de operación:
   - **Seleccionar Punto**: Ver información de ubicaciones
   - **Crear Zona con Área**: Crear nuevas zonas con radio ajustable
   - **Calcular Ruta Real**: Calcular rutas por calles reales (OSM)
3. Haz clic en el mapa para interactuar

### 5. Gestionar Zonas

- Las zonas se muestran como círculos en el mapa
- Colores según nivel de urgencia:
  - 🔴 **CRÍTICA**: Rojo
  - 🟠 **ALTA**: Naranja
  - 🟡 **MEDIA**: Amarillo
  - 🟢 **BAJA**: Verde

## 🐛 Solución de Problemas

### Puerto 8082 está ocupado

```powershell
# Ver qué proceso está usando el puerto
netstat -ano | findstr :8082

# Detener el proceso (reemplaza PID con el número que aparezca)
Stop-Process -Id <PID> -Force
```

### Error de compilación

```powershell
# Limpiar y recompilar
mvn clean compile
```

### La API muestra "Desconectada"

1. Verifica que la aplicación esté corriendo
2. Revisa la consola del navegador (F12) para ver errores
3. Verifica que el puerto sea 8082 en `application.properties` y `index.html`

### Error "No plugin found for prefix 'spring-boot'"

Asegúrate de estar en el directorio correcto:
```powershell
cd "PROYECTO-GESTION-DE-RIESGOS\Gestion-de-Riesgos"
```

## 📝 Notas Adicionales

- El sistema inicializa con datos de prueba automáticamente
- Las zonas, recursos y equipos de prueba se crean al iniciar
- El mapa usa OpenStreetMap para visualización
- Las rutas se calculan usando el servicio de routing de OSM
- Todos los datos se almacenan en memoria (se pierden al reiniciar)

## 👥 Contribuidores

Proyecto desarrollado para el curso de Estructuras de Datos.

## 📄 Licencia

Ver archivo LICENSE para más detalles.

---

**Versión**: 1.0.0  
**Última actualización**: Noviembre 2025  
**Puerto**: 8082  
**Estado**: ✅ Funcional
