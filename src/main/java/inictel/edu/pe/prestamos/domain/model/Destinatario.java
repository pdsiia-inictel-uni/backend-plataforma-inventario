package inictel.edu.pe.prestamos.domain.model;

/**
 * Persona registrada que puede llevarse un equipo en prestamo (RF-59).
 *
 * <p>Un Responsable u Operador activo de alguna coordinacion. Es la vista que
 * este contexto tiene de las personas de {@code iam}: datos planos, nunca el
 * agregado Usuario (RNF-39).</p>
 */
public record Destinatario(Long id,
                           String nombreCompleto,
                           String dni,
                           String rolEtiqueta,
                           Long coordinacionId) {
}
