package inictel.edu.pe.prestamos.domain.service;

/**
 * Puerto hacia {@code iam}: lo unico que los prestamos necesitan saber de las
 * personas (RNF-39).
 *
 * <p>RN-07: un equipo sale de la institucion a nombre de quien responde por el
 * inventario. Si la Coordinacion no tiene responsable —porque se le dio de baja
 * y aun no se ha nombrado a su sucesor (RF-26)— no hay quien responda, y la
 * salida no puede registrarse.</p>
 */
public interface DirectorioResponsables {

    /** true si la Coordinacion tiene Responsable vigente. */
    boolean tieneResponsableVigente(Long coordinacionId);
}
