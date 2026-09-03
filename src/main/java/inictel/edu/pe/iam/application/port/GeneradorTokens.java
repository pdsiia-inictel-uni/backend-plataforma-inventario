package inictel.edu.pe.iam.application.port;

import inictel.edu.pe.iam.domain.model.Usuario;

import java.time.Instant;

/**
 * Puerto de emision de tokens de acceso (RF-02).
 *
 * <p>La capa de aplicacion pide "un token para este usuario"; que sea JWT
 * firmado con HMAC es una decision de infraestructura.</p>
 */
public interface GeneradorTokens {

    TokenAcceso emitir(Usuario usuario);

    /** Token emitido junto con sus datos de vigencia. */
    record TokenAcceso(String token, Instant expiraEn, long expiracionMinutos) {
    }
}
