package inictel.edu.pe.iam.application.comando;

/** Cambio de la propia contrasena (RF-06, RNF-05). */
public record CambiarPasswordComando(
        Long usuarioId,
        String passwordActual,
        String passwordNueva,
        String confirmacion) {
}
