package inictel.edu.pe.iam.application.comando;

/**
 * RF-16b: registro previo de una persona.
 *
 * <p>No lleva rol, ni coordinacion, ni contrasena: el alta responde a quien es
 * la persona. Que hace y donde se decide despues, al asignarla (RF-28d), que
 * es tambien el momento en que nacen sus credenciales.</p>
 */
public record RegistrarUsuarioComando(
        String username,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String dni,
        String cargo,
        String correo) {
}
