package inictel.edu.pe.iam.application.dto;

/**
 * Resultado del alta o del restablecimiento de contrasena hecho por el
 * Administrador (RF-06). La contrasena temporal se muestra una unica vez para
 * que el Administrador se la entregue al usuario.
 */
public record PasswordTemporalDto(
        Long usuarioId,
        String username,
        String passwordTemporal,
        String mensaje) {

    public static PasswordTemporalDto de(Long usuarioId, String username, String passwordTemporal) {
        return new PasswordTemporalDto(usuarioId, username, passwordTemporal,
                "Entregue esta contrasena temporal al usuario. El sistema le exigira cambiarla en su proximo ingreso.");
    }
}
