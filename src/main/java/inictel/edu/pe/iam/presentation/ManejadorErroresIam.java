package inictel.edu.pe.iam.presentation;

import inictel.edu.pe.compartido.presentation.RespuestaError;
import inictel.edu.pe.iam.domain.excepcion.CambioPasswordRequeridoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Errores propios del contexto de identidad y acceso.
 *
 * <p>El kernel compartido solo conoce sus propias excepciones; cada contexto
 * anade las suyas sin que aquel dependa de ellos.</p>
 */
@RestControllerAdvice
public class ManejadorErroresIam {

    @ExceptionHandler(CambioPasswordRequeridoException.class)
    public ResponseEntity<RespuestaError> cambioRequerido(CambioPasswordRequeridoException ex,
                                                          HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(RespuestaError.de(HttpStatus.FORBIDDEN.value(), "CAMBIO_PASSWORD_REQUERIDO",
                        ex.getMessage(), req.getRequestURI()));
    }
}
