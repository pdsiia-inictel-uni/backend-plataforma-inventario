package inictel.edu.pe.compartido.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuracion externalizada de la aplicacion (RNF-19: sin credenciales
 * ni secretos en el codigo fuente).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Seguridad seguridad,
        CuentaInicial cuentaInicial,
        Cors cors,
        Archivos archivos) {

    /** RF-02: JWT firmado con expiracion configurable. */
    public record Jwt(String secret, long expiracionMinutos, String emisor) {
    }

    /** RNF-06: bloqueo temporal tras intentos fallidos. */
    public record Seguridad(int maxIntentosFallidos, int minutosBloqueo) {
    }

    /**
     * Cuenta administradora creada automaticamente cuando la base de datos esta
     * vacia. Obliga a cambiar la contrasena en el primer ingreso.
     */
    public record CuentaInicial(boolean habilitada, String username, String password, String correo, String dni) {
    }

    public record Cors(List<String> origenesPermitidos) {
    }

    public record Archivos(String directorio, String urlPublica) {
    }
}
