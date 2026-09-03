package inictel.edu.pe.iam.application.comando;

/**
 * Cambio del propio nombre de usuario y correo institucional. Se exige la
 * contrasena vigente como confirmacion de identidad.
 */
public record CambiarCredencialesComando(
        Long usuarioId,
        String username,
        String correo,
        String passwordActual) {
}
