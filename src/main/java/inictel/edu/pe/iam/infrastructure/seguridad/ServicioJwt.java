package inictel.edu.pe.iam.infrastructure.seguridad;

import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import inictel.edu.pe.iam.application.port.GeneradorTokens;
import inictel.edu.pe.iam.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Emision y verificacion de los JWT de acceso (RF-02, RNF-02).
 *
 * <p>Adaptador del puerto {@link GeneradorTokens}: la capa de aplicacion pide
 * un token y desconoce que se firma con HMAC-SHA512.</p>
 */
@Service
public class ServicioJwt implements GeneradorTokens {

    private static final Logger log = LoggerFactory.getLogger(ServicioJwt.class);

    public static final String CLAIM_USUARIO_ID = "uid";
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_COORDINACION = "coord";
    public static final String CLAIM_NOMBRE = "nombre";
    public static final String CLAIM_CAMBIO_PENDIENTE = "cambioPassword";

    private final SecretKey clave;
    private final long expiracionMinutos;
    private final String emisor;

    public ServicioJwt(AppProperties propiedades) {
        String secreto = propiedades.jwt().secret();
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 caracteres. Defina la variable de entorno JWT_SECRET.");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = propiedades.jwt().expiracionMinutos();
        this.emisor = propiedades.jwt().emisor();
    }

    @Override
    public TokenAcceso emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(CLAIM_USUARIO_ID, usuario.getId());
        // RF-16b: quien esta solo registrado todavia no tiene rol. No es un
        // estado de error: es una persona sin puesto, y su token no otorga nada.
        claims.put(CLAIM_ROL, usuario.getRol() == null ? null : usuario.getRol().name());
        // RN-05: una sola coordinacion por persona. Viaja en el token para que
        // el cliente sepa donde esta trabajando, pero el servidor la reconfirma
        // contra la base en cada peticion (ver JwtAuthenticationFilter): una
        // baja o una asignacion surten efecto en la siguiente, sin esperar a
        // que el token expire (RF-02).
        claims.put(CLAIM_COORDINACION, usuario.getCoordinacion());
        claims.put(CLAIM_NOMBRE, usuario.nombreCompleto());
        claims.put(CLAIM_CAMBIO_PENDIENTE, usuario.isDebeCambiarPassword());

        String token = Jwts.builder()
                .subject(usuario.getUsername().valor())
                .claims(claims)
                .issuer(emisor)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();

        return new TokenAcceso(token, expira, expiracionMinutos);
    }

    /** Devuelve los claims si el token es valido; null si no lo es (RF-03). */
    public Claims validar(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(emisor)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT rechazado: {}", ex.getMessage());
            return null;
        }
    }
}
