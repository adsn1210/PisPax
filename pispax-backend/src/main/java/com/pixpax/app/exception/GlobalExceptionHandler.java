package com.pixpax.app.exception;

// Manejador global de excepciones.
// Captura TODAS las excepciones que se lanzan en cualquier parte del backend
// y las convierte en respuestas JSON uniformes usando ErrorResponse.
// Sin esto, Spring devolvería páginas HTML de error o JSON con información interna.
// @RestControllerAdvice = este componente intercepta las excepciones de todos los controllers.

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Error 400: algún campo del JSON enviado por el cliente no pasa la validación (@NotBlank, @Email, etc.)
    // Recorre todos los errores y los junta en un solo mensaje separado por comas
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(mensaje, 400));
    }

    // Error variable: excepciones lanzadas con un código HTTP explícito (404, 401, 403, 409...)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getReason(), code));
    }

    // Error 403: el usuario está autenticado pero no tiene permiso (@PreAuthorize falló)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("No tienes permiso para realizar esta acción", 403));
    }

    // Error 400: argumento ilegal lanzado desde los servicios (ej: email duplicado, vehículo ajeno)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage(), 400));
    }

    // Error 409 Conflict: transición de estado inválida o viaje ya aceptado
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage(), 409));
    }

    // Error 409: violación de restricción única en BD (ej: matrícula duplicada que se coló sin validar)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Ya existe un registro con esos datos", 409));
    }

    // Error 500: cualquier otra excepción inesperada que no encaje en los casos anteriores
    // Logueamos el error completo para poder depurarlo, pero al cliente solo le damos un mensaje genérico
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        log.error("Error interno no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", 500));
    }
}
