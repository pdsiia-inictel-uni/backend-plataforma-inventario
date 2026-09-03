package inictel.edu.pe.compartido.domain.excepcion;

/**
 * Violacion de una regla de negocio del dominio (por ejemplo, prestar un bien
 * que no esta disponible).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
