package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.application.comando.CompletarPrimerIngresoComando;
import inictel.edu.pe.iam.presentation.validacion.PasswordSeguro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Primer ingreso: datos personales propios y contrasena definitiva (RF-06b).
 *
 * <p>Lo usa el Administrador de la cuenta inicial, cuyos datos son de relleno.
 * Los roles operativos llegan con sus datos ya cargados por quien los dio de
 * alta y solo necesitan cambiar la contrasena.</p>
 *
 * <p>El nombre se pide completo, segundo apellido incluido, y el cargo no se
 * pide: quien estrena la plataforma es el Administrador del sistema.</p>
 */
public record PrimerIngresoRequest(

        @NotBlank(message = "Ingrese sus nombres.")
        @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres.")
        String nombres,

        @NotBlank(message = "Ingrese su primer apellido.")
        @Size(max = 100, message = "El primer apellido no puede superar los 100 caracteres.")
        String primerApellido,

        @NotBlank(message = "Ingrese su segundo apellido.")
        @Size(max = 100, message = "El segundo apellido no puede superar los 100 caracteres.")
        String segundoApellido,

        @NotBlank(message = "Ingrese su DNI.")
        @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 digitos numericos.")
        String dni,

        @NotBlank(message = "Ingrese la contrasena que le fue asignada.")
        String passwordActual,

        @NotBlank(message = "Ingrese la nueva contrasena.")
        @PasswordSeguro
        String passwordNueva,

        @NotBlank(message = "Confirme la nueva contrasena.")
        String confirmacion) {

    public CompletarPrimerIngresoComando aComando(Long usuarioId) {
        return new CompletarPrimerIngresoComando(usuarioId, nombres, primerApellido, segundoApellido,
                dni, passwordActual, passwordNueva, confirmacion);
    }
}
