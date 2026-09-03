package inictel.edu.pe.compartido.presentation;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones del dominio y de la infraestructura a respuestas
 * HTTP uniformes y en espanol (RNF-12).
 *
 * <p>Solo conoce las excepciones del kernel compartido; cada contexto puede
 * anadir su propio {@code @RestControllerAdvice} para sus excepciones
 * especificas.</p>
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "REGLA_NEGOCIO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<RespuestaError> datosInvalidos(DatosInvalidosException ex, HttpServletRequest req) {
        Map<String, String> errores = ex.getErrores().isEmpty() ? null : ex.getErrores();
        return construir(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage(), req, errores);
    }

    /** Errores de validacion de las anotaciones jakarta.validation en los DTO. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errores.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "Revise los datos ingresados.", req, errores);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RespuestaError> credenciales(BadCredentialsException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", ex.getMessage(), req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No cuenta con permisos para realizar esta operacion.", req, null);
    }

    /**
     * RN-23: el usuario esta autenticado pero la operacion no le corresponde,
     * por su rol o por pertenecer los datos a otra coordinacion.
     *
     * <p>El mensaje del dominio si se propaga: explica al usuario por que no
     * puede hacerlo sin revelar nada sobre el recurso ajeno.</p>
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<RespuestaError> ambitoDenegado(AccesoDenegadoException ex, HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", ex.getMessage(), req, null);
    }

    /**
     * RNF-34: ruta de API inexistente. Devuelve 404 en el mismo formato que el
     * resto de errores, para que el frontend lo trate igual que cualquier otro.
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<RespuestaError> rutaInexistente(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA",
                "La direccion solicitada no existe.", req, null);
    }

    /**
     * RNF-25: metodo no admitido en una ruta que si existe.
     *
     * <p>Es el caso de las operaciones retiradas por la ERS —el alta de una
     * Direccion, por ejemplo— cuya ruta sigue viva para las lecturas. Sin este
     * manejador acababan en el 500 generico, que es una respuesta enganosa:
     * el servidor no fallo, es que ese verbo no existe ahi.</p>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaError> metodoNoAdmitido(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_ADMITIDO",
                "Esa operacion no existe en esta direccion.", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacion de integridad en {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT, "CONFLICTO_DATOS",
                "La operacion no pudo completarse porque genera datos duplicados o inconsistentes.", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "El valor del parametro '" + ex.getName() + "' no es valido.", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespuestaError> parametroFaltante(MissingServletRequestParameterException ex,
                                                           HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_REQUERIDO",
                "Falta el parametro obligatorio '" + ex.getParameterName() + "'.", req, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO",
                "El cuerpo de la peticion no tiene un formato valido.", req, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RespuestaError> archivoGrande(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return construir(HttpStatus.PAYLOAD_TOO_LARGE, "ARCHIVO_MUY_GRANDE",
                "El archivo supera el tamano maximo permitido (5 MB).", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> generico(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrio un error inesperado. Comuniquese con el administrador del sistema.", req, null);
    }

    private ResponseEntity<RespuestaError> construir(HttpStatus estado, String codigo, String mensaje,
                                                    HttpServletRequest req, Map<String, String> errores) {
        return ResponseEntity.status(estado)
                .body(RespuestaError.de(estado.value(), codigo, mensaje, req.getRequestURI(), errores));
    }
}
