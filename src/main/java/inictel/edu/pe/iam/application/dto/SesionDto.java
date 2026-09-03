package inictel.edu.pe.iam.application.dto;

import java.time.Instant;

/**
 * Resultado del inicio de sesion (RF-02).
 *
 * @param token               access token JWT
 * @param tipo                esquema de autorizacion ("Bearer")
 * @param expiraEn            instante de expiracion del token
 * @param expiracionMinutos   duracion configurada del token
 * @param debeCambiarPassword true si el usuario debe cambiar su contrasena antes de operar
 * @param usuario             datos del usuario autenticado
 */
public record SesionDto(
        String token,
        String tipo,
        Instant expiraEn,
        long expiracionMinutos,
        boolean debeCambiarPassword,
        UsuarioDto usuario) {
}
