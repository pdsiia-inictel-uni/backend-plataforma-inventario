package inictel.edu.pe.iam.domain.model;

/**
 * Situacion de la cuenta de una persona (RF-22, RF-22b).
 *
 * <ul>
 *   <li>{@link #ACTIVA}: la persona trabaja y entra al sistema.</li>
 *   <li>{@link #BAJA}: ya no pertenece a la institucion. No conserva puesto:
 *       quien se fue no responde por un inventario ni figura en una
 *       coordinacion (RN-34).</li>
 * </ul>
 *
 * <p><b>La suspension temporal ya no existe (v3.12).</b> Hubo un tercer estado
 * —SUSPENDIDA— para las ausencias con retorno: vacaciones, un permiso, una
 * licencia. Se retira porque una ausencia de dias no es una decision sobre el
 * acceso al sistema, y mantenerla obligaba a cada pantalla a explicar la
 * diferencia entre dos formas de no entrar. Lo que la institucion decide sobre
 * una cuenta es si la persona sigue aqui o ya no: la baja, y la reincorporacion
 * de quien vuelve.</p>
 *
 * <p>Ninguno de los dos borra a la persona: su historial en los bienes y en los
 * prestamos sigue nombrandola, que es justamente por lo que los usuarios no se
 * eliminan (RN-09, RNF-47).</p>
 */
public enum EstadoCuenta {

    ACTIVA("Activa"),
    BAJA("De baja");

    private final String etiqueta;

    EstadoCuenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** RNF-29: como se nombra el estado en la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }

    /** RF-07, RN-10: solo la cuenta activa autentica. */
    public boolean permiteEntrar() {
        return this == ACTIVA;
    }

    /** La persona sigue en la institucion. */
    public boolean sigueEnLaInstitucion() {
        return this != BAJA;
    }
}
