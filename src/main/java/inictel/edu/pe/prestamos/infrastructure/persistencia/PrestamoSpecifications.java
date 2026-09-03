package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Traduccion de los filtros de prestamos a predicados JPA (RF-28, RF-29).
 */
final class PrestamoSpecifications {

    private PrestamoSpecifications() {
    }

    /** RN-23: la llave del aislamiento. Nula solo para el Administrador. */
    static Specification<PrestamoJpaEntity> deCoordinacion(Long coordinacionId) {
        if (coordinacionId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("coordinacionId"), coordinacionId);
    }

    static Specification<PrestamoJpaEntity> conEstado(EstadoPrestamo estado) {
        if (estado == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    static Specification<PrestamoJpaEntity> deEquipo(Long equipoId) {
        if (equipoId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("equipo").get("id"), equipoId);
    }

    static Specification<PrestamoJpaEntity> conDni(String dni) {
        if (dni == null || dni.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("dniPersona"), dni.trim());
    }

    static Specification<PrestamoJpaEntity> textoLibre(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        final String patron = "%" + texto.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombrePersona")), patron),
                cb.like(cb.lower(root.get("dniPersona")), patron),
                cb.like(cb.lower(root.get("equipo").get("nombre")), patron),
                cb.like(cb.lower(root.get("equipo").get("codigoInventario")), patron),
                cb.like(cb.lower(root.get("equipo").get("numeroSerie")), patron));
    }

    static Specification<PrestamoJpaEntity> desde(LocalDate desde) {
        if (desde == null) {
            return null;
        }
        LocalDateTime inicio = desde.atStartOfDay();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaPrestamo"), inicio);
    }

    static Specification<PrestamoJpaEntity> hasta(LocalDate hasta) {
        if (hasta == null) {
            return null;
        }
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaPrestamo"), fin);
    }

    static Specification<PrestamoJpaEntity> soloVencidos(Boolean vencidos) {
        if (vencidos == null || !vencidos) {
            return null;
        }
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("estado"), EstadoPrestamo.ACTIVO),
                cb.isNotNull(root.get("fechaEstimadaDevolucion")),
                cb.lessThan(root.get("fechaEstimadaDevolucion"), LocalDate.now()));
    }
}
