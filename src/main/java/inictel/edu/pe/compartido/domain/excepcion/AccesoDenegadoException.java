package inictel.edu.pe.compartido.domain.excepcion;

/**
 * El usuario esta autenticado pero la operacion no le corresponde: por su rol
 * o por pertenecer los datos a otra Coordinacion (RN-23).
 *
 * <p>Se traduce a HTTP 403. El mensaje nunca revela si el recurso existe.</p>
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
