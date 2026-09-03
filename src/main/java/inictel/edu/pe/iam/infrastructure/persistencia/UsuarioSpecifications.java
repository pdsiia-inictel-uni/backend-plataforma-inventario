package inictel.edu.pe.iam.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduccion de los filtros del dominio a predicados JPA (RF-28, RF-29).
 */
final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    static Specification<UsuarioJpaEntity> textoLibre(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        final String patron = "%" + texto.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombres")), patron),
                cb.like(cb.lower(root.get("primerApellido")), patron),
                cb.like(cb.lower(cb.coalesce(root.get("segundoApellido"), "")), patron),
                cb.like(cb.lower(root.get("username")), patron),
                cb.like(cb.lower(root.get("correo")), patron),
                cb.like(root.get("dni"), patron));
    }

    static Specification<UsuarioJpaEntity> conRol(Rol rol) {
        if (rol == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("rol"), rol);
    }

    /**
     * RN-23: aislamiento por coordinacion.
     *
     * <p>Desde la v3.3 la adscripcion vive en la tabla de asignaciones, asi
     * que el filtro es una union con ella. Se marca distinta porque una
     * persona puede tener varias y no debe aparecer repetida en la lista.</p>
     */
    static Specification<UsuarioJpaEntity> deCoordinacion(Long coordinacionId) {
        if (coordinacionId == null) {
            return null;
        }
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            return cb.equal(root.join("asignaciones").get("coordinacionId"), coordinacionId);
        };
    }

    /** RF-28e: personas registradas que aun no tienen puesto. */
    static Specification<UsuarioJpaEntity> sinAsignar(Boolean sinAsignar) {
        if (sinAsignar == null) {
            return null;
        }
        return (root, query, cb) -> sinAsignar
                ? cb.isNull(root.get("rol"))
                : cb.isNotNull(root.get("rol"));
    }

    /** RF-22b: activa, suspendida o de baja. */
    static Specification<UsuarioJpaEntity> conEstado(EstadoCuenta estado) {
        if (estado == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }
}
