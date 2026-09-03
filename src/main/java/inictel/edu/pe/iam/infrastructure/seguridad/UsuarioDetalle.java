package inictel.edu.pe.iam.infrastructure.seguridad;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal de Spring Security respaldado por el agregado de dominio
 * {@link Usuario}.
 */
public class UsuarioDetalle implements UserDetails {

    private final transient Usuario usuario;

    public UsuarioDetalle(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getId() {
        return usuario.getId();
    }

    public Rol getRol() {
        return usuario.getRol();
    }

    /** RN-05, RN-23: su coordinacion; null para ADMIN y para quien no tiene puesto. */
    public Long getCoordinacion() {
        return usuario.getCoordinacion();
    }

    public String getNombreCompleto() {
        return usuario.nombreCompleto();
    }

    /** RF-06: mientras sea true el usuario solo puede cambiar su contrasena. */
    public boolean debeCambiarPassword() {
        return usuario.isDebeCambiarPassword();
    }

    /**
     * RF-16b: quien esta solo registrado no tiene rol y, por tanto, ningun
     * permiso. No es un error: es una persona a la que todavia no se le ha
     * dado un puesto.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Rol rol = usuario.getRol();
        return rol == null ? List.of() : List.of(new SimpleGrantedAuthority(rol.authority()));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername().valor();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !usuario.estaBloqueado();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** RF-09: un usuario inactivo no puede iniciar sesion. */
    @Override
    public boolean isEnabled() {
        return usuario.estaActiva();
    }
}
