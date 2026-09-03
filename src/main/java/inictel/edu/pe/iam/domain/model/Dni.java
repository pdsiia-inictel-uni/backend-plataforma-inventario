package inictel.edu.pe.iam.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.regex.Pattern;

/**
 * Documento Nacional de Identidad peruano: exactamente 8 digitos (RF-10).
 *
 * <p>Objeto de valor: la regla de formato vive en el dominio y no puede
 * eludirse creando un usuario por otra via.</p>
 */
public record Dni(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[0-9]{8}$");

    public Dni {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            throw new DatosInvalidosException("dni", "El DNI debe tener exactamente 8 digitos numericos.");
        }
        valor = valor.trim();
    }

    @Override
    public String toString() {
        return valor;
    }
}
