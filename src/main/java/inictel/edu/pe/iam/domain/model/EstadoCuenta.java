package inictel.edu.pe.iam.domain.model;

/**
 * Situacion de la cuenta de una persona (RF-22, RF-22b).
 *
 * <p>Hasta la v3.6 habia un interruptor: la cuenta estaba activa o no lo
 * estaba, y con el se resolvian dos cosas que no son la misma. Quien se va de
 * vacaciones y quien se va de la institucion acababan en el mismo sitio, de
 * modo que mirando la lista no habia forma de saber a cual de los dos habia
 * que esperar de vuelta.</p>
 *
 * <ul>
 *   <li>{@link #ACTIVA}: la persona trabaja y entra al sistema.</li>
 *   <li>{@link #SUSPENDIDA}: no puede entrar <b>por ahora</b> —vacaciones, un
 *       permiso, una licencia—. Conserva su puesto, porque va a volver a el.</li>
 *   <li>{@link #BAJA}: ya no pertenece a la institucion. No conserva puesto:
 *       quien se fue no responde por un inventario ni figura en una
 *       coordinacion (RN-34).</li>
 * </ul>
 *
 * <p>Ninguno de los tres borra a la persona: su historial en los bienes y en
 * los prestamos sigue nombrandola, que es justamente por lo que los usuarios
 * no se eliminan (RN-09, RNF-47).</p>
 */
public enum EstadoCuenta {

    ACTIVA("Activa"),
    SUSPENDIDA("Suspendida"),
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

    /** La persona sigue en la institucion, entre o no ahora mismo. */
    public boolean sigueEnLaInstitucion() {
        return this != BAJA;
    }
}
