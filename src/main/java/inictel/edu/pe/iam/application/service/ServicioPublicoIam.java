package inictel.edu.pe.iam.application.service;

import inictel.edu.pe.iam.domain.model.Usuario;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de host abierto del contexto {@code iam}: lo minimo que otros
 * contextos pueden preguntar sobre las personas del sistema.
 *
 * <p>Devuelve datos planos, nunca el agregado Usuario, para que ningun contexto
 * pueda modificar identidades por la puerta de atras (RNF-39).</p>
 */
@Service
public class ServicioPublicoIam {

    private final UsuarioRepositorio usuarios;

    public ServicioPublicoIam(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    /** RF-75: usuarios con acceso al sistema. */
    @Transactional(readOnly = true)
    public long contarUsuariosActivos() {
        return usuarios.contarActivos();
    }

    @Transactional(readOnly = true)
    public long contarUsuariosActivosEn(Long coordinacionId) {
        return usuarios.contarActivosEn(coordinacionId);
    }

    @Transactional(readOnly = true)
    public long contarOperadoresActivosEn(Long coordinacionId) {
        return usuarios.contarOperadoresActivosEn(coordinacionId);
    }

    /** Nombre completo de un usuario, para mostrar autorias. */
    @Transactional(readOnly = true)
    public Optional<String> nombreDe(Long usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        return usuarios.buscarPorId(usuarioId).map(Usuario::nombreCompleto);
    }

    /**
     * RF-36: Responsable vigente de una Coordinacion.
     *
     * <p>Lo consume {@code inventario} para sellar cada bien nuevo con el
     * responsable en funciones, y {@code organizacion} para mostrar quien esta
     * a cargo de cada coordinacion.</p>
     */
    @Transactional(readOnly = true)
    public Optional<ResponsableDto> responsableDe(Long coordinacionId) {
        return usuarios.buscarResponsableDe(coordinacionId)
                .map(u -> new ResponsableDto(u.getId(), u.nombreCompleto(), u.getDni().valor()));
    }

    /**
     * RF-83: la persona es un Operador activo de esa Coordinacion.
     *
     * <p>Lo consulta {@code inventario} antes de poner un bien a nombre de
     * alguien: solo un Operador de la propia Coordinacion puede tener un
     * equipo a su cargo (RN-37).</p>
     */
    @Transactional(readOnly = true)
    public boolean esOperadorActivoDe(Long usuarioId, Long coordinacionId) {
        if (usuarioId == null || coordinacionId == null) {
            return false;
        }
        return usuarios.buscarPorId(usuarioId)
                .filter(Usuario::esOperador)
                .filter(Usuario::estaActiva)
                .filter(u -> u.getCoordinaciones().contains(coordinacionId))
                .isPresent();
    }

    /** Datos publicos del Responsable de una Coordinacion. */
    public record ResponsableDto(Long id, String nombreCompleto, String dni) {
    }
}
