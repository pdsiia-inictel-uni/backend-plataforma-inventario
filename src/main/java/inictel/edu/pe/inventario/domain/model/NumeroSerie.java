package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

/**
 * Numero de serie del bien (RF-14).
 *
 * <p>Los bienes sin serie —mobiliario, principalmente— se registran con el
 * valor convencional {@code S/N}, que queda exento de la regla de unicidad.</p>
 */
public record NumeroSerie(String valor) {

    public static final String SIN_SERIE = "S/N";
    private static final int LONGITUD_MAXIMA = 100;

    public NumeroSerie {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("numeroSerie",
                    "Ingrese el numero de serie (use S/N si el bien no tiene).");
        }
        String limpio = valor.trim();
        if (limpio.length() > LONGITUD_MAXIMA) {
            throw new DatosInvalidosException("numeroSerie",
                    "El numero de serie no puede superar los 100 caracteres.");
        }
        valor = SIN_SERIE.equalsIgnoreCase(limpio) ? SIN_SERIE : limpio;
    }

    /** true cuando el bien carece de numero de serie propio. */
    public boolean esSinSerie() {
        return SIN_SERIE.equals(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
