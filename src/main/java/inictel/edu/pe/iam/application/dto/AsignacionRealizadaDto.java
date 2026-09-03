package inictel.edu.pe.iam.application.dto;

/**
 * Resultado de asignarle un puesto a una persona (RF-28d).
 *
 * <p>No hay ningun campo de relevo: desde la v3.4 asignar no sustituye a
 * nadie. Si el puesto de Responsable esta ocupado la operacion se rechaza, y
 * la baja del vigente es un acto aparte con su propia confirmacion
 * (RF-26).</p>
 *
 * @param usuario         como queda la persona tras la asignacion
 * @param passwordTemporal contrasena recien generada, o {@code null} si la
 *                        persona ya tenia credenciales de antes. Solo viaja
 *                        esta vez: no vuelve a mostrarse (RF-06)
 * @param mensaje         frase lista para mostrar, que describe lo que acaba
 *                        de ocurrir (RNF-31)
 */
public record AsignacionRealizadaDto(
        UsuarioDto usuario,
        String passwordTemporal,
        String mensaje) {
}
