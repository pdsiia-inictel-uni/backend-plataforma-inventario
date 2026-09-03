package inictel.edu.pe.iam.domain.service;

/**
 * Puerto de cifrado de contrasenas (RF-05, RNF-03).
 *
 * <p>El dominio exige que las contrasenas se guarden cifradas, pero no decide
 * el algoritmo: el adaptador de infraestructura implementa BCrypt.</p>
 */
public interface CifradorPassword {

    /** Devuelve el hash de una contrasena en texto plano. */
    String cifrar(String passwordPlana);

    /** Comprueba una contrasena en texto plano contra un hash almacenado. */
    boolean coincide(String passwordPlana, String hash);
}
