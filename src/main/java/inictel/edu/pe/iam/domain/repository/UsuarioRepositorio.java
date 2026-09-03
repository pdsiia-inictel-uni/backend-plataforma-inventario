package inictel.edu.pe.iam.domain.repository;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.iam.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia del agregado Usuario.
 *
 * <p>Se declara en el dominio y se implementa en infraestructura (inversion de
 * dependencias): el dominio y la aplicacion no saben que detras hay JPA.</p>
 */
public interface UsuarioRepositorio {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    /** RF-01: el ingreso admite nombre de usuario o correo institucional. */
    Optional<Usuario> buscarPorUsernameOCorreo(String identificador);

    /** RF-27: identificacion de una persona ya existente al designar un relevo. */
    Optional<Usuario> buscarPorDni(String dni);

    Pagina<Usuario> buscar(FiltroUsuarios filtro, CriterioPagina criterio);

    /** RF-29: todos los integrantes de una Coordinacion, sin paginar. */
    List<Usuario> listarPorCoordinacion(Long coordinacionId, boolean soloActivos);

    boolean existeUsername(String username, Long idExcluido);

    boolean existeCorreo(String correo, Long idExcluido);

    boolean existeDni(String dni, Long idExcluido);

    /** RN-06: Responsable vigente de una Coordinacion, si lo hay. */
    Optional<Usuario> buscarResponsableDe(Long coordinacionId);

    /**
     * RF-26b, RN-35: quienes pueden tomar el puesto de Responsable de una
     * Coordinacion.
     *
     * <p>Personas activas que no son ya Responsables: las que no tienen puesto,
     * los Operadores de esa misma Coordinacion y los Administradores.</p>
     */
    List<Usuario> candidatosAResponsableDe(Long coordinacionId);

    long contarOperadoresActivosEn(Long coordinacionId);

    /** RF-25: nunca puede quedar el sistema sin al menos un administrador activo. */
    long contarAdministradoresActivos();

    long contarActivos();

    long contarActivosEn(Long coordinacionId);

    /** true cuando no existe ningun usuario: dispara la creacion de la cuenta inicial. */
    boolean estaVacio();
}
