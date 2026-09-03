package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.application.comando.CambiarCredencialesComando;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cambio del propio nombre de usuario y correo institucional. Se exige la
 * contrasena actual como confirmacion de identidad.
 */
public record CambioUsuarioRequest(

        @NotBlank(message = "Ingrese el nombre de usuario.")
        @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de usuario solo admite letras, numeros, punto, guion y guion bajo.")
        String username,

        @NotBlank(message = "Ingrese el correo institucional.")
        @Email(message = "El correo institucional no tiene un formato valido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correo,

        @NotBlank(message = "Ingrese su contrasena actual para confirmar el cambio.")
        String passwordActual) {

    public CambiarCredencialesComando aComando(Long usuarioId) {
        return new CambiarCredencialesComando(usuarioId, username, correo, passwordActual);
    }
}
