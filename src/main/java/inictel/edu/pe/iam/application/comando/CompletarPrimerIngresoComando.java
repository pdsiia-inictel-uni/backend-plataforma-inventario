package inictel.edu.pe.iam.application.comando;

/**
 * Primer ingreso al sistema (RF-06, RF-06b).
 *
 * <p>Ademas de la contrasena, el Administrador completa su identidad real: la
 * cuenta inicial nace con datos de relleno ("Administrador del Sistema") y un
 * DNI de ejemplo, que no pueden quedarse asi en un sistema que identifica a las
 * personas por su DNI.</p>
 *
 * <p>El cargo no viaja en el comando: la primera persona de la plataforma es
 * el Administrador del sistema y el dominio se lo asigna.</p>
 */
public record CompletarPrimerIngresoComando(
        Long usuarioId,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String dni,
        String passwordActual,
        String passwordNueva,
        String confirmacion) {
}
