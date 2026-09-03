package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.application.comando.AutenticarComando;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesion (RF-01).
 *
 * @param usuario  nombre de usuario o correo institucional
 * @param password contrasena en texto plano (viaja sobre HTTPS, RNF-01)
 */
public record LoginRequest(

        @NotBlank(message = "Ingrese su usuario o correo institucional.")
        String usuario,

        @NotBlank(message = "Ingrese su contrasena.")
        String password) {

    public AutenticarComando aComando() {
        return new AutenticarComando(usuario, password);
    }
}
