package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.application.comando.CambiarPasswordComando;
import inictel.edu.pe.iam.presentation.validacion.PasswordSeguro;
import jakarta.validation.constraints.NotBlank;

/**
 * Cambio de la propia contrasena.
 *
 * <p>Aplica a Administradores y Operadores: siempre debe indicarse la
 * contrasena actual (la que asigno el Administrador la primera vez).</p>
 */
public record CambioPasswordRequest(

        @NotBlank(message = "Ingrese su contrasena actual.")
        String passwordActual,

        @NotBlank(message = "Ingrese la nueva contrasena.")
        @PasswordSeguro
        String passwordNueva,

        @NotBlank(message = "Confirme la nueva contrasena.")
        String confirmacion) {

    public CambiarPasswordComando aComando(Long usuarioId) {
        return new CambiarPasswordComando(usuarioId, passwordActual, passwordNueva, confirmacion);
    }
}
