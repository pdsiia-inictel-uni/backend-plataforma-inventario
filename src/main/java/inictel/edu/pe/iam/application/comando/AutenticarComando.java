package inictel.edu.pe.iam.application.comando;

/**
 * Credenciales de inicio de sesion (RF-01).
 *
 * @param identificador nombre de usuario o correo institucional
 * @param password      contrasena en texto plano (viaja sobre HTTPS, RNF-01)
 */
public record AutenticarComando(String identificador, String password) {
}
