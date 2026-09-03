package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.application.comando.ActualizarUsuarioComando;
import inictel.edu.pe.iam.application.comando.RegistrarUsuarioComando;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de alta y edicion de una persona (RF-16, RF-16b, RF-21).
 *
 * <p>Responde a una sola pregunta: <b>quien es</b>. Ni el rol ni la
 * coordinacion viajan aqui —eso es el puesto, y se decide al asignar
 * (RF-28d)—, ni tampoco la contrasena, que nace con la asignacion. Es el
 * registro previo: quien queda dado de alta existe en el sistema, aparece en la
 * lista de personas y todavia no puede entrar.</p>
 *
 * <p>Las anotaciones repiten en el borde HTTP las reglas que el dominio ya
 * garantiza, para devolver el detalle por campo en el formulario (RNF-25).</p>
 *
 * <p>{@code activo} no se acepta en el alta ni en la edicion: el estado de una
 * cuenta se cambia por su propio endpoint, que aplica las reglas de responsable
 * vigente y de ultimo administrador (RF-22, RF-25).</p>
 */
public record UsuarioRequest(

        @NotBlank(message = "Ingrese el nombre de usuario.")
        @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de usuario solo admite letras, numeros, punto, guion y guion bajo.")
        String username,

        @NotBlank(message = "Ingrese los nombres.")
        @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres.")
        String nombres,

        @NotBlank(message = "Ingrese el primer apellido.")
        @Size(max = 100, message = "El primer apellido no puede superar los 100 caracteres.")
        String primerApellido,

        @NotBlank(message = "Ingrese el segundo apellido.")
        @Size(max = 100, message = "El segundo apellido no puede superar los 100 caracteres.")
        String segundoApellido,

        @NotBlank(message = "Ingrese el DNI.")
        @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 digitos numericos.")
        String dni,

        @Size(max = 150, message = "El cargo no puede superar los 150 caracteres.")
        String cargo,

        @NotBlank(message = "Ingrese el correo institucional.")
        @Email(message = "El correo institucional no tiene un formato valido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correo) {

    public RegistrarUsuarioComando aComandoAlta() {
        return new RegistrarUsuarioComando(username, nombres, primerApellido, segundoApellido,
                dni, cargo, correo);
    }

    public ActualizarUsuarioComando aComandoEdicion(Long id) {
        return new ActualizarUsuarioComando(id, username, nombres, primerApellido, segundoApellido,
                dni, cargo, correo);
    }
}
