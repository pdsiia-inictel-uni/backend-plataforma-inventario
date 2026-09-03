package inictel.edu.pe.iam.domain.excepcion;

/**
 * El usuario autenticado tiene pendiente el cambio obligatorio de contrasena
 * (cuenta inicial o restablecimiento hecho por el Administrador, RF-06).
 * Se traduce a HTTP 403 con codigo CAMBIO_PASSWORD_REQUERIDO.
 */
public class CambioPasswordRequeridoException extends RuntimeException {

    public CambioPasswordRequeridoException() {
        super("Debe cambiar su contrasena antes de continuar usando el sistema.");
    }
}
