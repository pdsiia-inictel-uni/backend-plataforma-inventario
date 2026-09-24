package inictel.edu.pe.prestamos.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.regex.Pattern;

/**
 * Persona que se lleva el bien (RF-59).
 *
 * <p>Es un Responsable u Operador registrado en el sistema, de la coordinacion
 * de destino: {@code usuarioId} y {@code coordinacionId} lo identifican. El
 * nombre y el DNI se guardan como fotografia del momento de la salida, de modo
 * que el historial no cambie si la persona corrige despues sus datos. Los dos
 * identificadores son nulos solo en prestamos anteriores a la version que
 * exigio personas registradas.</p>
 */
public record PersonaResponsable(String nombre, String dni, Long usuarioId, Long coordinacionId) {

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
                    "El DNI debe tener exactamente 8 dígitos numericos.");
        }
        dni = dni.trim();
    }

    /** Persona sin usuario asociado: solo prestamos historicos. */
    public PersonaResponsable(String nombre, String dni) {
        this(nombre, dni, null, null);
    }
}
