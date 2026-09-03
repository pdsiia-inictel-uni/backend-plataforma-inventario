package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

/**
 * Codigo identificatorio de un bien: patrimonial o de inventario interno
 * (RF-14). Ambos son unicos en el sistema.
 */
public record CodigoBien(String valor, String campo) {

    private static final int LONGITUD_MAXIMA = 60;

    public CodigoBien {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException(campo, "Este codigo es obligatorio.");
        }
        valor = valor.trim();
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new DatosInvalidosException(campo, "El codigo no puede superar los 60 caracteres.");
        }
    }

    public static CodigoBien patrimonial(String valor) {
        return new CodigoBien(valor, "codigoPatrimonial");
    }

    public static CodigoBien inventario(String valor) {
        return new CodigoBien(valor, "codigoInventario");
    }

    @Override
    public String toString() {
        return valor;
    }
}
