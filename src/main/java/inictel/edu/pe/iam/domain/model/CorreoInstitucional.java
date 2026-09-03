package inictel.edu.pe.iam.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.regex.Pattern;

/** Correo institucional del usuario, unico en el sistema (RF-07, RF-10). */
public record CorreoInstitucional(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int LONGITUD_MAXIMA = 150;

    public CorreoInstitucional {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("correo", "Ingrese el correo institucional.");
        }
        valor = valor.trim();
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new DatosInvalidosException("correo", "El correo no puede superar los 150 caracteres.");
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new DatosInvalidosException("correo", "El correo institucional no tiene un formato valido.");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
