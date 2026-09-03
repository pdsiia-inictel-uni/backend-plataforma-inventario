package inictel.edu.pe.prestamos.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.regex.Pattern;

/**
 * Persona que se lleva el bien (RF-24).
 *
 * <p>Es un concepto propio de este contexto: no tiene por que ser un usuario
 * del sistema, y de ella solo interesan su nombre y su DNI.</p>
 */
public record PersonaResponsable(String nombre, String dni) {

    private static final Pattern DNI = Pattern.compile("^[0-9]{8}$");
    private static final int MAX_NOMBRE = 200;

    public PersonaResponsable {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("nombrePersona",
                    "Ingrese el nombre completo de la persona que lleva el bien.");
        }
        nombre = nombre.trim();
        if (nombre.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("nombrePersona",
                    "El nombre no puede superar los 200 caracteres.");
        }
        if (dni == null || !DNI.matcher(dni.trim()).matches()) {
            throw new DatosInvalidosException("dniPersona",
                    "El DNI debe tener exactamente 8 digitos numericos.");
        }
        dni = dni.trim();
    }
}
