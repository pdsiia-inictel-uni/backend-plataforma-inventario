package inictel.edu.pe.iam.infrastructure.seguridad;

import inictel.edu.pe.iam.domain.model.Usuario;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Valida el JWT de cada peticion y establece el contexto de seguridad
 * (RNF-02). El estado del usuario se relee en cada peticion para que una
 * desactivacion o un bloqueo surtan efecto de inmediato.
 *
 * <p>Se instancia desde {@code SeguridadConfig} y no como bean, para que el
 * contenedor no lo registre tambien en la cadena de filtros del servlet.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final ServicioJwt servicioJwt;
    private final UsuarioRepositorio usuarios;

    public JwtAuthenticationFilter(ServicioJwt servicioJwt, UsuarioRepositorio usuarios) {
        this.servicioJwt = servicioJwt;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = servicioJwt.validar(token);
            if (claims != null) {
                autenticar(request, claims);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(HttpServletRequest request, Claims claims) {
        Object uid = claims.get(ServicioJwt.CLAIM_USUARIO_ID);
        if (uid == null) {
            return;
        }
        Optional<Usuario> encontrado = usuarios.buscarPorId(Long.valueOf(uid.toString()));
        if (encontrado.isEmpty()) {
            return;
        }
        Usuario usuario = encontrado.get();
        // RF-07: un usuario desactivado o bloqueado pierde el acceso aunque su
        // token siga vigente. Igual de importante, el rol y la coordinacion se
        // releen de la base y no del token: un relevo o un cambio de adscripcion
        // surten efecto en la siguiente peticion, sin esperar a que expire (RN-23).
        if (!usuario.estaActiva() || usuario.estaBloqueado()) {
            return;
        }
        UsuarioDetalle detalle = new UsuarioDetalle(usuario);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(detalle, null, detalle.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader(CABECERA);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            String valor = cabecera.substring(PREFIJO.length()).trim();
            return valor.isEmpty() ? null : valor;
        }
        return null;
    }
}
