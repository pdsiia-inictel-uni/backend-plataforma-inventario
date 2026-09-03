package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduccion de los filtros del dominio a predicados JPA (RF-47, RF-48).
 */
final class EquipoSpecifications {

    private EquipoSpecifications() {
    }

    /** RN-23: la llave del aislamiento. Nula solo para el Administrador. */
    static Specification<EquipoJpaEntity> deCoordinacion(Long coordinacionId) {
        if (coordinacionId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("coordinacionId"), coordinacionId);
    }

    /** RF-48: busqueda unica sobre los campos que el usuario reconoce del bien. */
    static Specification<EquipoJpaEntity> textoLibre(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        final String patron = "%" + texto.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("marca")), patron),
                cb.like(cb.lower(root.get("modelo")), patron),
                cb.like(cb.lower(root.get("numeroSerie")), patron),
                cb.like(cb.lower(root.get("codigoInventario")), patron),
                cb.like(cb.lower(root.get("codigoPatrimonial")), patron));
    }

    static Specification<EquipoJpaEntity> deCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    static Specification<EquipoJpaEntity> enLaboratorio(Long laboratorioId) {
        if (laboratorioId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("laboratorioId"), laboratorioId);
    }

    /**
     * RF-84: los bienes a nombre de una persona, o los que no tienen asignacion.
     *
     * <p>Un bien sin {@code responsableEquipoId} no es un bien del que nadie
     * responda: es uno que lleva el propio Responsable de la Coordinacion, que
     * es justamente lo que significa no estar asignado a un Operador (RN-37).
     * Por eso las dos preguntas —"los de fulano" y "los del responsable"— caen
     * en el mismo sitio y se resuelven aqui.</p>
     */
    static Specification<EquipoJpaEntity> aCargoDe(Long responsableEquipoId, boolean sinAsignar) {
        if (responsableEquipoId != null) {
            return (root, query, cb) -> cb.equal(root.get("responsableEquipoId"), responsableEquipoId);
        }
        if (sinAsignar) {
            return (root, query, cb) -> cb.isNull(root.get("responsableEquipoId"));
        }
        return null;
    }

    /**
     * RF-47: el filtro de condicion del listado.
     *
     * <p>Sin condicion explicita y sin "todas", la vista muestra unicamente los
     * operativos, que es el comportamiento por defecto exigido.</p>
     */
    static Specification<EquipoJpaEntity> conCondicion(CondicionEquipo condicion, boolean todas) {
        if (todas && condicion == null) {
            return null;
        }
        CondicionEquipo objetivo = condicion == null ? CondicionEquipo.OPERATIVO : condicion;
        return (root, query, cb) -> cb.equal(root.get("condicion"), objetivo);
    }
}
