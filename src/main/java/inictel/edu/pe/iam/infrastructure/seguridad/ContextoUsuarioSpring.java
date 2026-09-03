package inictel.edu.pe.iam.infrastructure.seguridad;

import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador del puerto {@link ContextoUsuario} sobre Spring Security.
 *
 * <p>Es el unico punto del sistema que lee el {@code SecurityContextHolder};
 * el resto de contextos recibe la identidad ya traducida a un objeto de
 * dominio.</p>
 *
 * <p>La Coordinacion en la que se trabaja no la elige el cliente: cada persona
 * tiene exactamente una (RN-05) y el servidor la relee de la base en cada
 * peticion, de modo que una asignacion o una baja surten efecto en la
 * siguiente sin esperar a que el token expire (RF-02, RNF-10).</p>
 */
@Component
public class ContextoUsuarioSpring implements ContextoUsuario {

    @Override
    public Optional<UsuarioAutenticado> actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetalle detalle) {
            return Optional.of(new UsuarioAutenticado(
                    detalle.getId(),
                    detalle.getUsername(),
                    detalle.getNombreCompleto(),
                    detalle.getRol(),
                    detalle.getCoordinacion()));
        }
        return Optional.empty();
    }
}
