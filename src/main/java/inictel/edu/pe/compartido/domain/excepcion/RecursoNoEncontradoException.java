package inictel.edu.pe.compartido.domain.excepcion;

/** El recurso solicitado no existe. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /** Mensaje uniforme, por ejemplo: "No se encontro el usuario con identificador 5.". */
    public static RecursoNoEncontradoException de(String recurso, Object identificador) {
        return new RecursoNoEncontradoException(
                "No se encontro " + recurso + " con identificador " + identificador + ".");
    }
}
