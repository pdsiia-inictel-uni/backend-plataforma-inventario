package inictel.edu.pe.compartido.domain.seguridad;

/**
 * Roles del sistema (ERS v2.0, seccion 2.3).
 *
 * <p>Vive en el nucleo compartido y no en {@code iam} porque la autorizacion es
 * transversal: inventario, prestamos y reportes necesitan razonar sobre el rol
 * del usuario en curso sin depender del contexto de identidad.</p>
 */
public enum Rol {

    /**
     * Organiza la institucion: crea Direcciones, Coordinaciones, Laboratorios y
     * usuarios. No tiene ninguna operacion de escritura sobre bienes ni
     * prestamos (RN-22); sobre ellos solo lee, y de todas las Coordinaciones.
     */
    ADMIN("Administrador"),

    /**
     * Duenno del inventario de una Coordinacion. Control total dentro de ella
     * y ninguno fuera (RN-23). Exactamente uno por Coordinacion (RN-06).
     */
    RESPONSABLE("Responsable"),

    /**
     * Registra bienes y mueve prestamos dentro de su Coordinacion. No edita,
     * no da de baja y no gestiona usuarios (RN-24).
     */
    OPERADOR("Operador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Authority usada por Spring Security. */
    public String authority() {
        return "ROLE_" + name();
    }

    /** RN-05: solo el Administrador queda fuera de la estructura. */
    public boolean requiereCoordinacion() {
        return this != ADMIN;
    }
}
