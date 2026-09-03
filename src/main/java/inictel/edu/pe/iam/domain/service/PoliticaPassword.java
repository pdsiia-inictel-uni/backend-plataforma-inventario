package inictel.edu.pe.iam.domain.service;

/**
 * Politica de contrasenas del sistema (RNF-05): minimo 8 caracteres,
 * combinando letras, numeros y al menos un caracter especial.
 *
 * <p>Servicio de dominio sin estado: es la unica fuente de verdad de la regla,
 * reutilizada por la validacion de los formularios y por los casos de uso.</p>
 */
public final class PoliticaPassword {

    public static final int LONGITUD_MINIMA = 8;
    public static final String MENSAJE =
            "La contrasena debe tener al menos 8 caracteres e incluir letras, numeros y al menos un caracter especial.";

    private PoliticaPassword() {
    }

    public static boolean esValida(String valor) {
        if (valor == null || valor.length() < LONGITUD_MINIMA) {
            return false;
        }
        boolean letra = false;
        boolean numero = false;
        boolean especial = false;
        for (char c : valor.toCharArray()) {
            if (Character.isLetter(c)) {
                letra = true;
            } else if (Character.isDigit(c)) {
                numero = true;
            } else if (!Character.isWhitespace(c)) {
                especial = true;
            }
        }
        return letra && numero && especial;
    }
}
