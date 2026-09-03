package inictel.edu.pe.iam.infrastructure.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import inictel.edu.pe.compartido.presentation.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Respuesta 401 uniforme: ningun endpoint entrega datos sin un JWT valido
 * (criterio de aceptacion, seccion 7).
 */
@Component
public class ManejadorNoAutenticado implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ManejadorNoAutenticado(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        RespuestaError error = RespuestaError.de(
                HttpStatus.UNAUTHORIZED.value(),
                "NO_AUTENTICADO",
                "Su sesion no es valida o ha expirado. Vuelva a iniciar sesion.",
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
