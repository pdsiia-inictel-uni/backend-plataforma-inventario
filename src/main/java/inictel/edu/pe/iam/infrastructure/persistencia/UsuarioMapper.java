package inictel.edu.pe.iam.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.CorreoInstitucional;
import inictel.edu.pe.iam.domain.model.Dni;
import inictel.edu.pe.iam.domain.model.NombrePersona;
import inictel.edu.pe.iam.domain.model.NombreUsuario;
import inictel.edu.pe.iam.domain.model.Usuario;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Traduce entre el agregado de dominio y su representacion persistente.
 *
 * <p>Es el precio de mantener el dominio libre de anotaciones: toda la
 * conversion queda aislada aqui.</p>
 */
final class UsuarioMapper {

    private UsuarioMapper() {
    }

    static Usuario aDominio(UsuarioJpaEntity entidad) {
        return Usuario.reconstituir(
                entidad.getId(),
                new NombreUsuario(entidad.getUsername()),
                new NombrePersona(entidad.getNombres(), entidad.getPrimerApellido(), entidad.getSegundoApellido()),
                new Dni(entidad.getDni()),
                entidad.getCargo(),
                new CorreoInstitucional(entidad.getCorreo()),
                entidad.getPasswordHash(),
                entidad.getRol(),
                entidad.getAsignaciones().stream().map(AsignacionEmbebida::getCoordinacionId).toList(),
                entidad.getEstado(),
                entidad.isDebeCambiarPassword(),
                entidad.getIntentosFallidos(),
                entidad.getBloqueadoHasta(),
                entidad.getUltimoAcceso(),
                entidad.getFechaCreacion(),
                entidad.getFechaActualizacion());
    }

    /** Vuelca el estado del agregado sobre la entidad indicada (nueva o existente). */
    static UsuarioJpaEntity aEntidad(Usuario usuario, UsuarioJpaEntity destino) {
        UsuarioJpaEntity entidad = destino != null ? destino : new UsuarioJpaEntity();
        entidad.setId(usuario.getId());
        entidad.setUsername(usuario.getUsername().valor());
        entidad.setNombres(usuario.getNombre().nombres());
        entidad.setPrimerApellido(usuario.getNombre().primerApellido());
        entidad.setSegundoApellido(usuario.getNombre().segundoApellido());
        entidad.setDni(usuario.getDni().valor());
        entidad.setCargo(usuario.getCargo());
        entidad.setCorreo(usuario.getCorreo().valor());
        entidad.setPasswordHash(usuario.getPasswordHash());
        entidad.setRol(usuario.getRol());
        volcarAsignaciones(usuario, entidad);
        entidad.setEstado(usuario.getEstado());
        entidad.setDebeCambiarPassword(usuario.isDebeCambiarPassword());
        entidad.setIntentosFallidos(usuario.getIntentosFallidos());
        entidad.setBloqueadoHasta(usuario.getBloqueadoHasta());
        entidad.setUltimoAcceso(usuario.getUltimoAcceso());
        entidad.setFechaCreacion(usuario.getFechaCreacion());
        entidad.setFechaActualizacion(usuario.getFechaActualizacion());
        return entidad;
    }

    /**
     * Sincroniza las filas de {@code usuario_coordinacion} con el estado del
     * agregado, conservando la coleccion que Hibernate vigila: reemplazarla
     * por otra instancia rompe el seguimiento de cambios de la entidad.
     */
    private static void volcarAsignaciones(Usuario usuario, UsuarioJpaEntity entidad) {
        Rol rol = usuario.getRol();
        Set<AsignacionEmbebida> destino = entidad.getAsignaciones();
        List<AsignacionEmbebida> nuevas = usuario.getCoordinaciones().stream()
                .map(coordinacion -> new AsignacionEmbebida(coordinacion, rol))
                .toList();
        if (destino == null) {
            entidad.setAsignaciones(new LinkedHashSet<>(nuevas));
            return;
        }
        destino.clear();
        destino.addAll(nuevas);
    }
}
