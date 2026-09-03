package inictel.edu.pe.compartido.domain.seguridad;

import java.util.Optional;

/**
 * Puerto que expone el usuario autenticado a la capa de aplicacion de
 * cualquier contexto, sin acoplarla a Spring Security.
 *
 * <p>El adaptador vive en {@code iam.infrastructure.seguridad}, que es el
 * contexto duenno de la identidad.</p>
 */
public interface ContextoUsuario {

    Optional<UsuarioAutenticado> actual();

    /** Usuario autenticado; falla si no hay sesion (no ocurre tras el filtro JWT). */
    default UsuarioAutenticado requerido() {
        return actual().orElseThrow(() ->
                new IllegalStateException("No hay un usuario autenticado en el contexto de la peticion."));
    }

    default Optional<Long> id() {
        return actual().map(UsuarioAutenticado::id);
    }
}
