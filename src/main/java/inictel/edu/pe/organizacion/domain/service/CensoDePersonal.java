package inictel.edu.pe.organizacion.domain.service;

import java.util.Optional;

/**
 * Puerto hacia {@code iam} para mostrar en la estructura quien esta a cargo de
 * cada Coordinacion y cuanta gente tiene (RF-15).
 *
 * <p>Capa anticorrupcion: {@code organizacion} recibe un dato plano y nunca el
 * agregado Usuario (RNF-39).</p>
 */
public interface CensoDePersonal {

    /**
     * Responsable vigente de la Coordinacion. Vacio cuando no hay ninguno, que
     * es la alerta mas importante de la vista de estructura (RF-15).
     */
    Optional<ResponsableVigente> responsableDe(Long coordinacionId);

    long operadoresActivosEn(Long coordinacionId);

    record ResponsableVigente(Long id, String nombreCompleto, String dni) {
    }
}
