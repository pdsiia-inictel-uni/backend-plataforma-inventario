package inictel.edu.pe.iam.infrastructure.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import inictel.edu.pe.compartido.presentation.RespuestaError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Mientras el usuario tenga pendiente el cambio obligatorio de contrasena
 * (cuenta inicial o restablecimiento del Administrador, RF-06), solo puede
 * consultar su perfil, cambiar su contrasena y cerrar sesion.
 */
public class ForzarCambioPasswordFilter extends OncePerRequestFilter {

    public static final String CODIGO = "CAMBIO_PASSWORD_REQUERIDO";

    /**
     * Rutas permitidas mientras el cambio esta pendiente.
     *
     * <p>Debe incluir la propia ruta de cambio de contrasena: de lo contrario
     * el usuario queda encerrado, obligado a cambiarla y sin poder hacerlo.</p>
     */
    private static final Set<String> RUTAS_PERMITIDAS = Set.of(
            "/api/auth/me",
            "/api/auth/cambiar-password",
            "/api/auth/primer-ingreso",
            "/api/auth/logout",
            "/api/auth/login");

    private final ObjectMapper objectMapper;

    public ForzarCambioPasswordFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetalle detalle
                && detalle.debeCambiarPassword()
                && !esRutaPermitida(request)) {

            responderBloqueo(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean esRutaPermitida(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        return RUTAS_PERMITIDAS.contains(ruta) || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void responderBloqueo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RespuestaError error = RespuestaError.de(
                HttpStatus.FORBIDDEN.value(),
                CODIGO,
                "Debe cambiar su contrasena antes de continuar usando el sistema.",
                request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
