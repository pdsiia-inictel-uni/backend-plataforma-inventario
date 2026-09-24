package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.regex.Pattern;

/**
 * Codigo identificatorio de un bien: patrimonial o de inventario interno
 * (RF-14). Ambos son unicos en el sistema.
 */
public record CodigoBien(String valor, String campo) {

    private static final int LONGITUD_MAXIMA = 60;

    /** El codigo patrimonial tiene exactamente 12 caracteres: letras y/o numeros. */
    private static final Pattern PATRIMONIAL = Pattern.compile("^[A-Za-z0-9]{12}$");
    private static final String CAMPO_PATRIMONIAL = "codigoPatrimonial";

    public CodigoBien {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException(campo, "Este código es obligatorio.");
        }
        valor = valor.trim();
        if (CAMPO_PATRIMONIAL.equals(campo)) {
            // Se guarda en mayusculas: la unicidad de la base ya compara sin
            // distinguir mayusculas (UPPER), y asi se lee igual en todas partes.
            valor = valor.toUpperCase();
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new DatosInvalidosException(campo, "El código no puede superar los 60 caracteres.");
        }
        if (CAMPO_PATRIMONIAL.equals(campo) && !PATRIMONIAL.matcher(valor).matches()) {
            throw new DatosInvalidosException(campo,
                    "El código patrimonial debe tener exactamente 12 caracteres, solo letras y números.");
        }
    }

    public static CodigoBien patrimonial(String valor) {
        return new CodigoBien(valor, CAMPO_PATRIMONIAL);
    }

    public static CodigoBien inventario(String valor) {
        return new CodigoBien(valor, "codigoInventario");
    }

    @Override
    public String toString() {
        return valor;
    }
}
