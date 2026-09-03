package inictel.edu.pe.iam.application.comando;

/**
 * Edicion de los datos personales de un usuario (RF-21).
 *
 * <p>El rol y las coordinaciones no viajan aqui: se cambian asignando y
 * retirando asignaciones (RF-28d), que es donde viven sus reglas.</p>
 */
public record ActualizarUsuarioComando(
        Long id,
        String username,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String dni,
        String cargo,
        String correo) {
}
