package inictel.edu.pe.iam.infrastructure.seguridad;

import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga del usuario para Spring Security a partir del nombre de usuario
 * o del correo institucional (RF-01).
 */
@Service
public class DetalleUsuarioService implements UserDetailsService {

    private final UsuarioRepositorio usuarios;

    public DetalleUsuarioService(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        return usuarios.buscarPorUsernameOCorreo(identificador)
                .map(UsuarioDetalle::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + identificador));
    }
}
