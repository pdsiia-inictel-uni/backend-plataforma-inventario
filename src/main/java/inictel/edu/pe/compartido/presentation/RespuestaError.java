package inictel.edu.pe.compartido.presentation;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cuerpo uniforme de error de la API (RNF-12).
 *
 * @param fechaHora momento en que se genero el error
 * @param estado    codigo HTTP
 * @param codigo    codigo interno para que el frontend reaccione (p. ej. CAMBIO_PASSWORD_REQUERIDO)
 * @param mensaje   mensaje en espanol apto para mostrar al usuario
 * @param ruta      endpoint solicitado
 * @param errores   detalle por campo cuando se trata de errores de validacion
 */
public record RespuestaError(
        LocalDateTime fechaHora,
        int estado,
        String codigo,
        String mensaje,
        String ruta,
        Map<String, String> errores) {

    public static RespuestaError de(int estado, String codigo, String mensaje, String ruta) {
        return new RespuestaError(LocalDateTime.now(), estado, codigo, mensaje, ruta, null);
    }

    public static RespuestaError de(int estado, String codigo, String mensaje, String ruta,
                                    Map<String, String> errores) {
        return new RespuestaError(LocalDateTime.now(), estado, codigo, mensaje, ruta, errores);
    }
}
