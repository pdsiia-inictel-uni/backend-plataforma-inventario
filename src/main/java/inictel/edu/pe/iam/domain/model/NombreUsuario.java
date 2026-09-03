package inictel.edu.pe.iam.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.regex.Pattern;

/** Identificador de acceso del usuario (RF-01). Su dueno puede modificarlo. */
public record NombreUsuario(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final int LONGITUD_MINIMA = 4;
    private static final int LONGITUD_MAXIMA = 50;

    public NombreUsuario {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("username", "Ingrese el nombre de usuario.");
        }
        valor = valor.trim();
        if (valor.length() < LONGITUD_MINIMA || valor.length() > LONGITUD_MAXIMA) {
            throw new DatosInvalidosException("username",
                    "El nombre de usuario debe tener entre 4 y 50 caracteres.");
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new DatosInvalidosException("username",
                    "El nombre de usuario solo admite letras, numeros, punto, guion y guion bajo.");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
