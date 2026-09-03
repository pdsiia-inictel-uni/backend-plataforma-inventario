package inictel.edu.pe.organizacion.infrastructure.persistencia;

import inictel.edu.pe.organizacion.domain.model.Coordinacion;
import inictel.edu.pe.organizacion.domain.model.Direccion;
import inictel.edu.pe.organizacion.domain.model.Laboratorio;

/**
 * Traduccion entre los agregados de dominio y sus representaciones
 * persistentes. Mantiene el dominio libre de anotaciones JPA (RNF-38).
 */
final class OrganizacionMapper {

    private OrganizacionMapper() {
    }

    // ------------------------------------------------------------------
    // Direccion
    // ------------------------------------------------------------------

    static Direccion aDominio(DireccionJpaEntity entidad) {
        return Direccion.reconstituir(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getSigla(),
                entidad.getDescripcion(),
                entidad.isActiva(),
                entidad.getFechaCreacion(),
                entidad.getFechaActualizacion());
    }

    static DireccionJpaEntity aEntidad(Direccion direccion, DireccionJpaEntity existente) {
        DireccionJpaEntity entidad = existente != null ? existente : new DireccionJpaEntity();
        entidad.setNombre(direccion.getNombre());
        entidad.setSigla(direccion.getSigla());
        entidad.setDescripcion(direccion.getDescripcion());
        entidad.setActiva(direccion.isActiva());
        entidad.setFechaCreacion(direccion.getFechaCreacion());
        entidad.setFechaActualizacion(direccion.getFechaActualizacion());
        return entidad;
    }

    // ------------------------------------------------------------------
    // Coordinacion
    // ------------------------------------------------------------------

    static Coordinacion aDominio(CoordinacionJpaEntity entidad) {
        return Coordinacion.reconstituir(
                entidad.getId(),
                entidad.getDireccionId(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.isActiva(),
                entidad.getFechaCreacion(),
                entidad.getFechaActualizacion());
    }

    static CoordinacionJpaEntity aEntidad(Coordinacion coordinacion, CoordinacionJpaEntity existente) {
        CoordinacionJpaEntity entidad = existente != null ? existente : new CoordinacionJpaEntity();
        entidad.setDireccionId(coordinacion.getDireccionId());
        entidad.setNombre(coordinacion.getNombre());
        entidad.setDescripcion(coordinacion.getDescripcion());
        entidad.setActiva(coordinacion.isActiva());
        entidad.setFechaCreacion(coordinacion.getFechaCreacion());
        entidad.setFechaActualizacion(coordinacion.getFechaActualizacion());
        return entidad;
    }

    // ------------------------------------------------------------------
    // Laboratorio
    // ------------------------------------------------------------------

    static Laboratorio aDominio(LaboratorioJpaEntity entidad) {
        return Laboratorio.reconstituir(
                entidad.getId(),
                entidad.getCoordinacionId(),
                entidad.getNombre(),
                entidad.getUbicacion(),
                entidad.isActivo(),
                entidad.getFechaCreacion(),
                entidad.getFechaActualizacion());
    }

    static LaboratorioJpaEntity aEntidad(Laboratorio laboratorio, LaboratorioJpaEntity existente) {
        LaboratorioJpaEntity entidad = existente != null ? existente : new LaboratorioJpaEntity();
        entidad.setCoordinacionId(laboratorio.getCoordinacionId());
        entidad.setNombre(laboratorio.getNombre());
        entidad.setUbicacion(laboratorio.getUbicacion());
        entidad.setActivo(laboratorio.isActivo());
        entidad.setFechaCreacion(laboratorio.getFechaCreacion());
        entidad.setFechaActualizacion(laboratorio.getFechaActualizacion());
        return entidad;
    }
}
