package inictel.edu.pe.iam.domain.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Genera contrasenas temporales que cumplen la politica RNF-05.
 *
 * <p>Se omiten los caracteres ambiguos (l, I, 1, O, 0) porque la contrasena se
 * dicta o se transcribe al usuario.</p>
 */
public final class GeneradorPasswordTemporal {

    private static final String LETRAS = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String NUMEROS = "23456789";
    private static final String ESPECIALES = "@#$%&*!?";
    private static final int LONGITUD = 12;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeneradorPasswordTemporal() {
    }

    public static String generar() {
        List<Character> caracteres = new ArrayList<>(LONGITUD);
        caracteres.add(aleatorio(LETRAS));
        caracteres.add(Character.toUpperCase(aleatorio(LETRAS)));
        caracteres.add(aleatorio(NUMEROS));
        caracteres.add(aleatorio(ESPECIALES));

        String alfabeto = LETRAS + NUMEROS + ESPECIALES;
        while (caracteres.size() < LONGITUD) {
            caracteres.add(aleatorio(alfabeto));
        }
        Collections.shuffle(caracteres, ALEATORIO);

        StringBuilder sb = new StringBuilder(LONGITUD);
        caracteres.forEach(sb::append);
        return sb.toString();
    }

    private static char aleatorio(String origen) {
        return origen.charAt(ALEATORIO.nextInt(origen.length()));
    }
}
