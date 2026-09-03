package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.BienPrestado;
import inictel.edu.pe.prestamos.domain.model.OperadorPrestamo;
import inictel.edu.pe.prestamos.domain.model.PersonaResponsable;
import inictel.edu.pe.prestamos.domain.model.Prestamo;

/** Traduccion entre el agregado Prestamo y su entidad persistente. */
final class PrestamoMapper {

    private PrestamoMapper() {
    }

    static Prestamo aDominio(PrestamoJpaEntity entidad) {
        BienReferenciaJpa bien = entidad.getEquipo();
        return Prestamo.reconstituir(
                entidad.getId(),
                new BienPrestado(bien.getId(), entidad.getCoordinacionId(), bien.getNombre(),
                        bien.getCodigoInventario(), bien.getNumeroSerie()),
                new PersonaResponsable(entidad.getNombrePersona(), entidad.getDniPersona()),
                entidad.getDestino(),
                entidad.getFechaPrestamo(),
                entidad.getFechaEstimadaDevolucion(),
                entidad.getObservacionesSalida(),
                entidad.getFechaDevolucion(),
                entidad.getConforme(),
                entidad.isReportaDano(),
                entidad.getObservacionesRetorno(),
                operador(entidad.getUsuarioPresta()),
                operador(entidad.getUsuarioRecibe()),
                entidad.getEstado());
    }

    static PrestamoJpaEntity aEntidad(Prestamo prestamo,
                                      PrestamoJpaEntity destino,
                                      BienReferenciaJpa bien,
                                      UsuarioReferenciaJpa usuarioPresta,
                                      UsuarioReferenciaJpa usuarioRecibe) {
        PrestamoJpaEntity entidad = destino != null ? destino : new PrestamoJpaEntity();
        entidad.setId(prestamo.getId());
        entidad.setEquipo(bien);
        entidad.setCoordinacionId(prestamo.coordinacionId());
        entidad.setNombrePersona(prestamo.getPersona().nombre());
        entidad.setDniPersona(prestamo.getPersona().dni());
        entidad.setDestino(prestamo.getDestino());
        entidad.setFechaPrestamo(prestamo.getFechaPrestamo());
        entidad.setFechaEstimadaDevolucion(prestamo.getFechaEstimadaDevolucion());
        entidad.setObservacionesSalida(prestamo.getObservacionesSalida());
        entidad.setFechaDevolucion(prestamo.getFechaDevolucion());
        entidad.setConforme(prestamo.getConforme());
        entidad.setReportaDano(prestamo.isReportaDano());
        entidad.setObservacionesRetorno(prestamo.getObservacionesRetorno());
        entidad.setUsuarioPresta(usuarioPresta);
        entidad.setUsuarioRecibe(usuarioRecibe);
        entidad.setEstado(prestamo.getEstado());
        return entidad;
    }

    private static OperadorPrestamo operador(UsuarioReferenciaJpa usuario) {
        return usuario == null ? null : new OperadorPrestamo(usuario.getId(), usuario.nombreCompleto());
    }
}
