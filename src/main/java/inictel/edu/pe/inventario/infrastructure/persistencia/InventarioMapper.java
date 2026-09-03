package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.inventario.domain.model.Categoria;
import inictel.edu.pe.inventario.domain.model.CodigoBien;
import inictel.edu.pe.inventario.domain.model.Equipo;
import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.model.NumeroSerie;
import inictel.edu.pe.inventario.domain.model.ReferenciaCategoria;

/**
 * Traduccion entre los agregados de dominio y sus representaciones
 * persistentes. Mantiene el dominio libre de anotaciones JPA (RNF-38).
 */
final class InventarioMapper {

    private InventarioMapper() {
    }

    // ------------------------------------------------------------------
    // Categoria
    // ------------------------------------------------------------------

    static Categoria aDominio(CategoriaJpaEntity entidad) {
        return Categoria.reconstituir(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.isActiva(),
                entidad.getFechaCreacion());
    }

    static CategoriaJpaEntity aEntidad(Categoria categoria, CategoriaJpaEntity existente) {
        CategoriaJpaEntity entidad = existente != null ? existente : new CategoriaJpaEntity();
        entidad.setNombre(categoria.getNombre());
        entidad.setDescripcion(categoria.getDescripcion());
        entidad.setActiva(categoria.isActiva());
        entidad.setFechaCreacion(categoria.getFechaCreacion());
        return entidad;
    }

    // ------------------------------------------------------------------
    // Equipo
    // ------------------------------------------------------------------

    static Equipo aDominio(EquipoJpaEntity entidad) {
        CategoriaJpaEntity categoria = entidad.getCategoria();
        ReferenciaCategoria referencia = categoria == null
                ? null
                : new ReferenciaCategoria(categoria.getId(), categoria.getNombre());

        return Equipo.reconstituir(
                entidad.getId(),
                entidad.getCoordinacionId(),
                entidad.getLaboratorioId(),
                entidad.getNombre(),
                entidad.getMarca(),
                entidad.getModelo(),
                new NumeroSerie(entidad.getNumeroSerie()),
                CodigoBien.inventario(entidad.getCodigoInventario()),
                CodigoBien.patrimonial(entidad.getCodigoPatrimonial()),
                referencia,
                entidad.getCondicion(),
                entidad.getFechaAdquisicion(),
                entidad.getCosto(),
                entidad.getObservaciones(),
                entidad.getFotoUrl(),
                entidad.isRevisionPendiente(),
                entidad.getMotivoBaja(),
                entidad.getFechaBaja(),
                entidad.getResponsableId(),
                entidad.getResponsableEquipoId(),
                entidad.getUsuarioRegistroId(),
                entidad.getFechaRegistro(),
                entidad.getFechaActualizacion(),
                entidad.isActivo());
    }

    /**
     * Vuelca el estado del agregado sobre la entidad indicada.
     *
     * @param categoria referencia gestionada de la categoria, obtenida por el
     *                  adaptador para no romper la asociacion perezosa
     */
    static EquipoJpaEntity aEntidad(Equipo equipo, EquipoJpaEntity existente, CategoriaJpaEntity categoria) {
        EquipoJpaEntity entidad = existente != null ? existente : new EquipoJpaEntity();
        entidad.setCoordinacionId(equipo.getCoordinacionId());
        entidad.setLaboratorioId(equipo.getLaboratorioId());
        entidad.setNombre(equipo.getNombre());
        entidad.setMarca(equipo.getMarca());
        entidad.setModelo(equipo.getModelo());
        entidad.setNumeroSerie(equipo.getNumeroSerie().valor());
        entidad.setCodigoInventario(equipo.getCodigoInventario().valor());
        entidad.setCodigoPatrimonial(equipo.getCodigoPatrimonial().valor());
        entidad.setCategoria(categoria);
        entidad.setCondicion(equipo.getCondicion());
        entidad.setFechaAdquisicion(equipo.getFechaAdquisicion());
        entidad.setCosto(equipo.getCosto());
        entidad.setObservaciones(equipo.getObservaciones());
        entidad.setFotoUrl(equipo.getFotoUrl());
        entidad.setRevisionPendiente(equipo.isRevisionPendiente());
        entidad.setMotivoBaja(equipo.getMotivoBaja());
        entidad.setFechaBaja(equipo.getFechaBaja());
        entidad.setResponsableId(equipo.getResponsableId());
        entidad.setResponsableEquipoId(equipo.getResponsableEquipoId());
        entidad.setUsuarioRegistroId(equipo.getUsuarioRegistroId());
        entidad.setFechaRegistro(equipo.getFechaRegistro());
        entidad.setFechaActualizacion(equipo.getFechaActualizacion());
        entidad.setActivo(equipo.isActivo());
        return entidad;
    }

    // ------------------------------------------------------------------
    // Movimiento (solo escritura; nunca se actualiza)
    // ------------------------------------------------------------------

    static MovimientoEquipo aDominio(MovimientoJpaEntity entidad) {
        return MovimientoEquipo.reconstituir(
                entidad.getId(),
                entidad.getEquipoId(),
                entidad.getTipo(),
                entidad.getCondicionAnterior(),
                entidad.getCondicionNueva(),
                entidad.getDetalle(),
                entidad.getUsuarioId(),
                entidad.getUsuarioNombre(),
                entidad.getRolUsuario(),
                entidad.getFechaHora());
    }

    static MovimientoJpaEntity aEntidad(MovimientoEquipo movimiento) {
        MovimientoJpaEntity entidad = new MovimientoJpaEntity();
        entidad.setEquipoId(movimiento.equipoId());
        entidad.setTipo(movimiento.tipo());
        entidad.setCondicionAnterior(movimiento.condicionAnterior());
        entidad.setCondicionNueva(movimiento.condicionNueva());
        entidad.setDetalle(movimiento.detalle());
        entidad.setUsuarioId(movimiento.usuarioId());
        entidad.setUsuarioNombre(movimiento.usuarioNombre());
        entidad.setRolUsuario(movimiento.rolUsuario());
        entidad.setFechaHora(movimiento.fechaHora());
        return entidad;
    }
}
