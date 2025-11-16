package co.edu.uniquindio.Gestion.de.Riesgos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador personalizado para manejar errores HTTP
 * Reemplaza la página de error por defecto de Spring Boot
 */
@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // Ignorar errores de archivos estáticos comunes (favicon, etc.)
        if (requestUri != null) {
            String uri = requestUri.toString();
            if (uri.equals("/favicon.ico") || 
                uri.startsWith("/.well-known/") ||
                uri.endsWith(".html") || uri.endsWith(".css") || uri.endsWith(".js") || 
                uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".ico") ||
                uri.equals("/robots.txt") || uri.equals("/sitemap.xml")) {
                // Retornar 204 No Content para estos recursos
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        }

        Map<String, Object> error = new HashMap<>();
        error.put("success", false);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            error.put("status", statusCode);
            error.put("error", getErrorType(statusCode));

            switch (statusCode) {
                case 404:
                    error.put("message", "El endpoint solicitado no existe.");
                    error.put("path", requestUri != null ? requestUri.toString() : "Desconocido");
                    error.put("suggestion", "Verifica la URL del endpoint. Los endpoints disponibles están bajo /api/");
                    break;
                case 405:
                    error.put("message", "Método HTTP no permitido para este endpoint.");
                    error.put("path", requestUri != null ? requestUri.toString() : "Desconocido");
                    error.put("suggestion", "Verifica que estés usando el método HTTP correcto (GET, POST, etc.)");
                    break;
                case 400:
                    error.put("message", message != null ? message.toString() : "Solicitud incorrecta.");
                    break;
                case 500:
                    error.put("message", "Error interno del servidor.");
                    if (exception != null && exception instanceof Exception) {
                        Exception ex = (Exception) exception;
                        error.put("details", ex.getMessage());
                    }
                    break;
                default:
                    error.put("message", message != null ? message.toString() : "Ha ocurrido un error.");
            }
        } else {
            error.put("status", 500);
            error.put("error", "UNKNOWN_ERROR");
            error.put("message", "Error desconocido");
        }

        HttpStatus httpStatus = status != null 
            ? HttpStatus.valueOf(Integer.parseInt(status.toString()))
            : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(httpStatus).body(error);
    }

    private String getErrorType(int statusCode) {
        switch (statusCode) {
            case 400: return "BAD_REQUEST";
            case 401: return "UNAUTHORIZED";
            case 403: return "FORBIDDEN";
            case 404: return "NOT_FOUND";
            case 405: return "METHOD_NOT_ALLOWED";
            case 500: return "INTERNAL_SERVER_ERROR";
            default: return "HTTP_ERROR";
        }
    }
}

