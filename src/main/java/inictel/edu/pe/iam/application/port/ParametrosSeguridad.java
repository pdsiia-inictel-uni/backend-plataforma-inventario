package inictel.edu.pe.iam.application.port;

/**
 * Parametros de seguridad configurables (RNF-06, RNF-19).
 *
 * <p>La capa de aplicacion los consume a traves de este puerto para no depender
 * de las clases de configuracion de Spring Boot.</p>
 */
public interface ParametrosSeguridad {

    /** Intentos fallidos consecutivos antes de bloquear la cuenta. */
    int maxIntentosFallidos();

    /** Duracion del bloqueo temporal, en minutos. */
    int minutosBloqueo();
}
