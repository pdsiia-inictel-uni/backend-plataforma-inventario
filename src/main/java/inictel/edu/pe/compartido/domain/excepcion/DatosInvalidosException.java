package inictel.edu.pe.compartido.domain.excepcion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos rechazados por el dominio, con el detalle por campo para que el
 * formulario pueda senalarlos en linea (RNF-12).
 */
public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> errores = new LinkedHashMap<>();

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        errores.put(campo, mensaje);
    }

    public DatosInvalidosException agregar(String campo, String mensaje) {
        errores.put(campo, mensaje);
        return this;
    }

    public Map<String, String> getErrores() {
        return Map.copyOf(errores);
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }
}
