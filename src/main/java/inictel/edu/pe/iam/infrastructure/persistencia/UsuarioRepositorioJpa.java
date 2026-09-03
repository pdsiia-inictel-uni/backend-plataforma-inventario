package inictel.edu.pe.iam.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.compartido.infrastructure.persistencia.PaginacionJpa;
import inictel.edu.pe.compartido.infrastructure.persistencia.Specs;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import inictel.edu.pe.iam.domain.model.Usuario;
import inictel.edu.pe.iam.domain.repository.FiltroUsuarios;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia del agregado Usuario: implementa el puerto del
 * dominio apoyandose en Spring Data JPA.
 */
@Repository
public class UsuarioRepositorioJpa implements UsuarioRepositorio {

    private static final String ORDEN_POR_DEFECTO = "primerApellido";

    private final UsuarioJpaRepository jpa;

    public UsuarioRepositorioJpa(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity existente = usuario.getId() != null
                ? jpa.findById(usuario.getId()).orElse(null)
                : null;
        UsuarioJpaEntity guardada = jpa.save(UsuarioMapper.aEntidad(usuario, existente));
        usuario.asignarId(guardada.getId());
        return UsuarioMapper.aDominio(guardada);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(UsuarioMapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorUsernameOCorreo(String identificador) {
        return jpa.buscarPorUsernameOCorreo(identificador).map(UsuarioMapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorDni(String dni) {
        return jpa.findByDni(dni).map(UsuarioMapper::aDominio);
    }

    @Override
    public Pagina<Usuario> buscar(FiltroUsuarios filtro, CriterioPagina criterio) {
        Specification<UsuarioJpaEntity> spec = Specs.combinar(
                UsuarioSpecifications.textoLibre(filtro.texto()),
                UsuarioSpecifications.conRol(filtro.rol()),
                UsuarioSpecifications.deCoordinacion(filtro.coordinacionId()),
                UsuarioSpecifications.sinAsignar(filtro.sinAsignar()),
                UsuarioSpecifications.conEstado(filtro.estado()));

        return PaginacionJpa.aPagina(
                jpa.findAll(spec, PaginacionJpa.aPageable(criterio, ORDEN_POR_DEFECTO)),
                UsuarioMapper::aDominio);
    }

    @Override
    public List<Usuario> listarPorCoordinacion(Long coordinacionId, boolean soloActivos) {
        if (coordinacionId == null) {
            return List.of();
        }
        return jpa.listarPorCoordinacion(coordinacionId, soloActivos).stream()
                .map(UsuarioMapper::aDominio)
                .toList();
    }

    @Override
    public boolean existeUsername(String username, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByUsernameIgnoreCase(username)
                : jpa.existsByUsernameIgnoreCaseAndIdNot(username, idExcluido);
    }

    @Override
    public boolean existeCorreo(String correo, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByCorreoIgnoreCase(correo)
                : jpa.existsByCorreoIgnoreCaseAndIdNot(correo, idExcluido);
    }

    @Override
    public boolean existeDni(String dni, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByDni(dni)
                : jpa.existsByDniAndIdNot(dni, idExcluido);
    }

    @Override
    public Optional<Usuario> buscarResponsableDe(Long coordinacionId) {
        if (coordinacionId == null) {
            return Optional.empty();
        }
        return jpa.buscarPorCoordinacionYRol(coordinacionId, Rol.RESPONSABLE)
                .map(UsuarioMapper::aDominio);
    }

    @Override
    public List<Usuario> candidatosAResponsableDe(Long coordinacionId) {
        if (coordinacionId == null) {
            return List.of();
        }
        return jpa.candidatosAResponsable(coordinacionId).stream()
                .map(UsuarioMapper::aDominio)
                .toList();
    }

    @Override
    public long contarOperadoresActivosEn(Long coordinacionId) {
        return coordinacionId == null ? 0
                : jpa.contarPorCoordinacionYRol(coordinacionId, Rol.OPERADOR);
    }

    @Override
    public long contarAdministradoresActivos() {
        return jpa.countByRolAndEstado(Rol.ADMIN, EstadoCuenta.ACTIVA);
    }

    @Override
    public long contarActivos() {
        return jpa.countByEstado(EstadoCuenta.ACTIVA);
    }

    @Override
    public long contarActivosEn(Long coordinacionId) {
        return coordinacionId == null ? 0 : jpa.contarActivosEnCoordinacion(coordinacionId);
    }

    @Override
    public boolean estaVacio() {
        return jpa.count() == 0;
    }
}
